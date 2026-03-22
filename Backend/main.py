from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from api.routes import router

app = FastAPI(
    title="Smart Irrigation AI Assistant",
    description="LangChain-powered AI agent for smart irrigation recommendations",
    version="1.0.0"
)

# Configure CORS for Android app
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # Allow all origins for local development
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Include routes
app.include_router(router)


@app.get("/")
async def root():
    """Root endpoint with API information."""
    return {
        "name": "Smart Irrigation AI Assistant",
        "version": "1.0.0",
        "endpoints": {
            "chat_streaming": "POST /chat",
            "chat_sync": "POST /chat/sync",
            "health": "GET /health",
            "docs": "GET /docs"
        }
    }


if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=True)
