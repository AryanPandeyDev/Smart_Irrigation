from typing import Optional
from pydantic import BaseModel, Field


class DeviceState(BaseModel):
    """Current state of the irrigation device from ESP8266."""
    soil_moisture: int = Field(..., ge=0, le=1024, description="Raw soil moisture reading (0-1024)")
    threshold: int = Field(..., ge=0, le=1024, description="Current moisture threshold (0-1024)")
    relay_status: bool = Field(..., description="True if pump is ON, False if OFF")
    mode: bool = Field(..., description="True for manual mode, False for auto mode")


class ChatRequest(BaseModel):
    """Request body for chat endpoint."""
    message: str = Field(..., min_length=1, description="User's message/query")
    device_state: DeviceState = Field(..., description="Current device state from ESP8266")
    user_location: Optional[str] = Field(None, description="User's city/location for weather data")
    plant_type: Optional[str] = Field(None, description="Type of plant being grown")


class ActionPayload(BaseModel):
    """Action for the Android app to execute."""
    type: str = Field(..., description="Action type: set_threshold, set_mode, set_pump")
    value: Optional[int] = Field(None, description="Value for threshold (0-1024)")
    mode: Optional[bool] = Field(None, description="Mode value for set_mode action")
    pump_status: Optional[bool] = Field(None, description="Pump status for set_pump action")
    reason: Optional[str] = Field(None, description="Explanation for the action")


class StreamToken(BaseModel):
    """Single token in the streaming response."""
    token: str = Field(..., description="Text token")
    action: Optional[ActionPayload] = Field(None, description="Action to execute (only in final token)")


class ChatResponse(BaseModel):
    """Full response for non-streaming endpoint."""
    response: str = Field(..., description="Complete response text")
    action: Optional[ActionPayload] = Field(None, description="Action for app to execute")
