import httpx
from langchain.tools import tool
from config import settings


@tool
async def get_weather(location: str) -> str:
    """
    Get current weather data for a location.
    Use this when you need to know the current weather conditions 
    (temperature, humidity, rainfall) to make irrigation recommendations.
    
    Args:
        location: City name (e.g., "Mumbai", "Delhi", "New York")
    
    Returns:
        Weather information including temperature, humidity, and conditions
    """
    if not settings.OPENWEATHERMAP_API_KEY:
        return "Weather API key not configured. Unable to fetch weather data."
    
    try:
        async with httpx.AsyncClient() as client:
            response = await client.get(
                "https://api.openweathermap.org/data/2.5/weather",
                params={
                    "q": location,
                    "appid": settings.OPENWEATHERMAP_API_KEY,
                    "units": "metric"
                },
                timeout=10.0
            )
            
            if response.status_code == 404:
                return f"Location '{location}' not found. Please check the city name."
            
            response.raise_for_status()
            data = response.json()
            
            weather_info = {
                "location": data.get("name", location),
                "temperature_celsius": data["main"]["temp"],
                "humidity_percent": data["main"]["humidity"],
                "conditions": data["weather"][0]["description"],
                "wind_speed_mps": data["wind"]["speed"],
            }
            
            # Check for rain
            if "rain" in data:
                weather_info["rain_1h_mm"] = data["rain"].get("1h", 0)
            else:
                weather_info["rain_1h_mm"] = 0
            
            return (
                f"Weather in {weather_info['location']}: "
                f"Temperature: {weather_info['temperature_celsius']}°C, "
                f"Humidity: {weather_info['humidity_percent']}%, "
                f"Conditions: {weather_info['conditions']}, "
                f"Wind: {weather_info['wind_speed_mps']} m/s, "
                f"Rain (last 1h): {weather_info['rain_1h_mm']} mm"
            )
            
    except httpx.TimeoutException:
        return "Weather service timed out. Please try again."
    except httpx.HTTPError as e:
        return f"Error fetching weather data: {str(e)}"
    except Exception as e:
        return f"Unexpected error getting weather: {str(e)}"
