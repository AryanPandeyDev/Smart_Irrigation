import os
from dotenv import load_dotenv

load_dotenv()


class Settings:
    GOOGLE_API_KEY: str = os.getenv("GOOGLE_API_KEY", "")
    OPENWEATHERMAP_API_KEY: str = os.getenv("OPENWEATHERMAP_API_KEY", "")
    
    # Model settings - gemini-2.5-flash (stable, 1M token context)
    MODEL_NAME: str = "gemini-2.5-flash"
    MODEL_TEMPERATURE: float = 0.7


settings = Settings()
