import json
import asyncio
from typing import AsyncGenerator

from fastapi import APIRouter, HTTPException
from fastapi.responses import StreamingResponse
from sse_starlette.sse import EventSourceResponse

from api.schemas import ChatRequest, ChatResponse, StreamToken
from agent.agent import irrigation_agent

router = APIRouter()


async def stream_chat_response(request: ChatRequest) -> AsyncGenerator[str, None]:
    """Generate SSE events for streaming chat response."""
    try:
        async for token, action in irrigation_agent.chat_stream(
            message=request.message,
            device_state=request.device_state,
            user_location=request.user_location,
            plant_type=request.plant_type
        ):
            if token:
                stream_token = StreamToken(token=token, action=None)
                yield json.dumps(stream_token.model_dump())
            
            if action:
                # Send final token with action
                stream_token = StreamToken(token="", action=action)
                yield json.dumps(stream_token.model_dump())
        
        # Send done marker
        yield "[DONE]"
        
    except Exception as e:
        error_token = StreamToken(token=f"Error: {str(e)}", action=None)
        yield json.dumps(error_token.model_dump())
        yield "[DONE]"


@router.post("/chat")
async def chat_streaming(request: ChatRequest):
    """
    Streaming chat endpoint using Server-Sent Events.
    
    Sends tokens as they are generated, with optional action in final token.
    """
    async def event_generator():
        async for data in stream_chat_response(request):
            if data == "[DONE]":
                yield {"event": "done", "data": data}
            else:
                yield {"event": "message", "data": data}
    
    return EventSourceResponse(event_generator())


@router.post("/chat/sync", response_model=ChatResponse)
async def chat_sync(request: ChatRequest):
    """
    Non-streaming chat endpoint.
    
    Returns complete response with optional action.
    """
    try:
        response_text, action = await irrigation_agent.chat(
            message=request.message,
            device_state=request.device_state,
            user_location=request.user_location,
            plant_type=request.plant_type
        )
        
        return ChatResponse(response=response_text, action=action)
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@router.get("/health")
async def health_check():
    """Health check endpoint."""
    return {"status": "healthy"}
