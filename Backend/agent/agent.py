import json
import re
from typing import AsyncGenerator, Optional, Tuple

from langchain_google_genai import ChatGoogleGenerativeAI
from langchain_core.prompts import ChatPromptTemplate, MessagesPlaceholder
from langchain_core.messages import HumanMessage, AIMessage, SystemMessage
from langchain_core.tools import tool as langchain_tool

from config import settings
from api.schemas import DeviceState, ActionPayload
from agent.tools import get_weather


def raw_to_percent(value: int, max_val: int = 1024) -> int:
    """Convert raw sensor value to percentage."""
    return round((value / max_val) * 100)


def percent_to_raw(percent: int, max_val: int = 1024) -> int:
    """Convert percentage to raw sensor value."""
    return round((percent / 100) * max_val)


def parse_action_from_response(response: str) -> Tuple[str, Optional[ActionPayload]]:
    """
    Parse the response to extract any action block.
    Returns (clean_response, action_payload)
    """
    action_pattern = r'```action\s*\n?(.*?)\n?```'
    match = re.search(action_pattern, response, re.DOTALL)
    
    if not match:
        return response, None
    
    try:
        action_json = match.group(1).strip()
        action_data = json.loads(action_json)
        
        action = ActionPayload(
            type=action_data.get("type", ""),
            value=action_data.get("value"),
            mode=action_data.get("mode"),
            pump_status=action_data.get("pump_status"),
            reason=action_data.get("reason")
        )
        
        # Remove action block from response
        clean_response = re.sub(action_pattern, '', response, flags=re.DOTALL).strip()
        return clean_response, action
        
    except (json.JSONDecodeError, Exception):
        return response, None


SYSTEM_PROMPT = """You are an intelligent irrigation assistant for a smart irrigation system.

## Your Capabilities:
1. Answer questions about the current irrigation status
2. Provide recommendations for optimal moisture thresholds based on plant type and weather
3. Execute actions when the user requests (set threshold, change mode, control pump)

## Current Device State:
- Soil Moisture: {soil_moisture_raw} (raw value 0-1024) = {soil_moisture_percent}%
- Current Threshold: {threshold_raw} (raw value 0-1024) = {threshold_percent}%
- Pump Status: {pump_status}
- Mode: {mode}

## Context:
- Plant Type: {plant_type}
- Location: {user_location}

## Understanding the Values:
- Higher soil moisture value = DRIER soil (sensor reads higher when dry)
- When soil moisture > threshold in AUTO mode, pump turns ON
- Threshold percentage: lower = pump activates at wetter soil, higher = pump activates at drier soil

## Weather Information:
{weather_info}

## Response Guidelines:
1. For QUESTIONS: Analyze the current state and provide helpful information
2. For RECOMMENDATIONS: Consider plant type, current moisture, weather conditions
3. For ACTIONS: When user wants to set/change something, include the action in your response

## Action Format:
When the user asks you to SET or CHANGE something, you MUST include an action block at the END of your response in this exact format:
```action
{{"type": "set_threshold", "value": 512, "reason": "Your reason here"}}
```
OR
```action
{{"type": "set_mode", "mode": true, "reason": "Your reason here"}}
```
OR
```action
{{"type": "set_pump", "pump_status": true, "reason": "Your reason here"}}
```

Action types:
- set_threshold: value is 0-1024 (convert percentage to this range, e.g., 50% = 512)
- set_mode: mode is true for manual, false for auto
- set_pump: pump_status is true for ON, false for OFF (only works in manual mode)

Be conversational, helpful, and proactive in your recommendations."""


class IrrigationAgent:
    def __init__(self):
        self.llm = ChatGoogleGenerativeAI(
            model=settings.MODEL_NAME,
            google_api_key=settings.GOOGLE_API_KEY,
            temperature=settings.MODEL_TEMPERATURE,
        )
    
    def _build_context(
        self,
        device_state: DeviceState,
        user_location: Optional[str],
        plant_type: Optional[str]
    ) -> dict:
        """Build context dictionary for the prompt."""
        return {
            "soil_moisture_raw": device_state.soil_moisture,
            "soil_moisture_percent": raw_to_percent(device_state.soil_moisture),
            "threshold_raw": device_state.threshold,
            "threshold_percent": raw_to_percent(device_state.threshold),
            "pump_status": "ON" if device_state.relay_status else "OFF",
            "mode": "Manual" if device_state.mode else "Automatic",
            "plant_type": plant_type or "Not specified",
            "user_location": user_location or "Not specified"
        }
    
    async def _get_weather_info(self, location: Optional[str]) -> str:
        """Fetch weather info if location is provided."""
        if not location:
            return "Location not provided - weather data unavailable."
        
        try:
            weather_result = await get_weather.ainvoke(location)
            return weather_result
        except Exception as e:
            return f"Could not fetch weather: {str(e)}"
    
    async def chat(
        self,
        message: str,
        device_state: DeviceState,
        user_location: Optional[str] = None,
        plant_type: Optional[str] = None
    ) -> Tuple[str, Optional[ActionPayload]]:
        """
        Process a chat message and return the response with optional action.
        Non-streaming version.
        """
        context = self._build_context(device_state, user_location, plant_type)
        weather_info = await self._get_weather_info(user_location)
        context["weather_info"] = weather_info
        
        system_message = SYSTEM_PROMPT.format(**context)
        
        messages = [
            SystemMessage(content=system_message),
            HumanMessage(content=message)
        ]
        
        response = await self.llm.ainvoke(messages)
        response_text = response.content
        
        clean_response, action = parse_action_from_response(response_text)
        return clean_response, action
    
    async def chat_stream(
        self,
        message: str,
        device_state: DeviceState,
        user_location: Optional[str] = None,
        plant_type: Optional[str] = None
    ) -> AsyncGenerator[Tuple[str, Optional[ActionPayload]], None]:
        """
        Process a chat message and stream the response tokens.
        Yields (token, action) tuples. Action is only set on final token.
        """
        context = self._build_context(device_state, user_location, plant_type)
        weather_info = await self._get_weather_info(user_location)
        context["weather_info"] = weather_info
        
        system_message = SYSTEM_PROMPT.format(**context)
        
        messages = [
            SystemMessage(content=system_message),
            HumanMessage(content=message)
        ]
        
        full_response = ""
        
        async for chunk in self.llm.astream(messages):
            content = chunk.content
            if content:
                full_response += content
                yield content, None
        
        # Parse action from complete response
        _, action = parse_action_from_response(full_response)
        
        # Yield empty token with action if present
        if action:
            yield "", action


# Singleton instance
irrigation_agent = IrrigationAgent()
