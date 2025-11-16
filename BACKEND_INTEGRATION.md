# WeatherDesk Backend Integration Guide

## Overview

WeatherDesk is now fully integrated with WeatherDesk23 backend to provide real weather data. This guide explains the architecture, setup, and how to extend the integration.

## Architecture

### Frontend to Backend Flow

```
User Input (City Name)
         |
         v
  Form Submission
         |
         v
  Server Action: getWeather()
         |
         v
  Convert City -> Lat/Lng Coordinates
         |
         v
  Fetch from Backend API
  GET /weather?latitude={lat}&longitude={lng}
         |
         v
  Transform Backend Response
         |
         v
  Display Weather Data
```

## Files Modified

### 1. `src/app/actions.ts` (Server Actions)

**Key Changes:**
- Replaced `generateMockData()` with actual backend API calls
- Added `BACKEND_URL` configuration from environment variables
- Added city-to-coordinates mapping in `CITY_COORDS` object
- Implemented proper error handling and validation
- Added data transformation layer to match frontend expectations

**Features:**
- City name validation
- Automatic coordinate lookup
- Graceful error handling for unsupported cities
- Fallback error messages with helpful guidance

### 2. `.env.example` (New)

**Purpose:** Document required environment variables

**Default Configuration:**
```
NEXT_PUBLIC_BACKEND_URL=http://localhost:8080
```

## Backend API Contract

### Request Format

```
GET /weather?latitude={number}&longitude={number}
```

### Response Format

```json
{
  "longitude": number,
  "latitude": number,
  "current": {
    "temperature_2m": number,
    "relative_humidity_2m": number,
    "is_day": number (0=night, 1=day),
    "wind_speed_10m": number
  }
}
```

### Data Mapping

| Backend Field | Frontend Usage | Unit |
|---|---|---|
| `current.temperature_2m` | Current Temperature | Fahrenheit |
| `current.relative_humidity_2m` | Humidity % | Percentage |
| `current.wind_speed_10m` | Wind Speed | mph |
| `current.is_day` | Weather Condition Logic | 0/1 |

## Supported Cities

Currently integrated cities with their coordinates:

| City | Latitude | Longitude |
|---|---|---|
| London | 51.5074 | -0.1278 |
| New York | 40.7128 | -74.0060 |
| Tokyo | 35.6762 | 139.6503 |
| Paris | 48.8566 | 2.3522 |
| Sydney | -33.8688 | 151.2093 |
| Toronto | 43.6532 | -79.3832 |
| Thunder Bay | 48.3809 | -89.2477 |

## Setup & Installation

### Prerequisites
- Node.js 18+ and npm
- Kotlin/Gradle (for backend)
- Backend running on configured URL

### Step 1: Frontend Setup

```bash
# Navigate to WeatherDesk directory
cd WeatherDesk

# Install dependencies
npm install

# Copy environment template
cp .env.example .env.local

# Update .env.local if backend is on different URL
# NEXT_PUBLIC_BACKEND_URL=http://your-backend-url:8080
```

### Step 2: Backend Setup

```bash
# Navigate to WeatherDesk23 directory
cd ../WeatherDesk23

# Build with Gradle
./gradlew build

# Run the backend
./gradlew run

# Backend will start on http://localhost:8080
```

### Step 3: Start Frontend

```bash
# Back in WeatherDesk directory
npm run dev

# Frontend will start on http://localhost:3000
```

## Usage

1. Open browser to `http://localhost:3000`
2. Enter a supported city name
3. Click "Get Weather"
4. View real weather data from backend

## Extending the Integration

### Adding New Cities

Edit `src/app/actions.ts` and add to `CITY_COORDS`:

```typescript
const CITY_COORDS: Record<string, { lat: number; lng: number }> = {
  "london": { lat: 51.5074, lng: -0.1278 },
  // Add your city here:
  "your city": { lat: latitude_value, lng: longitude_value },
};
```

### Adding Dynamic Geocoding

For production, consider replacing static coordinates with a geocoding service:

```typescript
// Example with external geocoding API
const getCoordinates = async (city: string) => {
  const response = await fetch(
    `https://geocoding-api.open-meteo.com/v1/search?name=${city}&count=1&language=en&format=json`
  );
  const data = await response.json();
  if (data.results?.[0]) {
    return {
      lat: data.results[0].latitude,
      lng: data.results[0].longitude
    };
  }
  throw new Error(`City "${city}" not found`);
};
```

## Error Handling

### Common Errors & Solutions

| Error | Cause | Solution |
|---|---|---|
| "City not found" | City not in supported list | Add city to CITY_COORDS or use dynamic geocoding |
| Backend connection failed | Backend not running | Start backend with `./gradlew run` |
| CORS error | Backend CORS misconfigured | Check backend CORS settings in Ktor config |
| Environmental variable not set | .env.local not configured | Copy .env.example to .env.local |

## Type Safety

### TypeScript Definitions

Key types used in integration:

```typescript
interface WeatherData {
  current: {
    city: string;
    temperature: number;
    condition: string;
    humidity: number;
    windSpeed: number;
    date: string;
  };
  forecast: Array<{
    day: string;
    high: number;
    low: number;
    condition: string;
  }>;
}

interface WeatherState {
  weatherData?: WeatherData;
  error?: string;
  message?: string;
}
```

## Performance Considerations

- **Caching**: Consider adding client-side caching for recent city searches
- **Rate Limiting**: Backend enforces no rate limit, but implement on frontend if needed
- **Timeout**: Default fetch timeout is implicit, consider adding explicit timeout

## Security

- Backend URL is environment-based (can vary per deployment)
- No sensitive data in frontend code
- Server actions prevent direct backend exposure
- Coordinates are public data (no security risk)

## Troubleshooting

### Backend not responding
```bash
# Check if backend is running
curl http://localhost:8080/weather?latitude=51.5074&longitude=-0.1278

# If no response, restart backend
./gradlew run
```

### Frontend shows old data
```bash
# Clear npm cache and rebuild
rm -rf .next node_modules
npm install
npm run dev
```

### Environment variables not loading
```bash
# Restart frontend after updating .env.local
# The development server needs to restart to pick up new env vars
```

## Future Enhancements

1. **Dynamic Geocoding**: Replace static city list with geocoding API
2. **Forecast Integration**: Extend backend to return multi-day forecast
3. **Caching Layer**: Implement Redis or similar for frequently requested cities
4. **Analytics**: Track weather searches for insights
5. **Historical Data**: Store and display weather trends
6. **Multi-language**: Support city names in different languages
7. **Favorites**: Save frequently checked cities

## Related Documentation

- [WeatherDesk23 README](../WeatherDesk23/README.md) - Backend documentation
- [Next.js Server Actions](https://nextjs.org/docs/app/building-your-application/data-fetching/server-actions-and-mutations)
- [Open-Meteo API](https://open-meteo.com/en/docs)
- [Ktor Documentation](https://ktor.io/docs/)
