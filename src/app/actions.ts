"use server";

import type { WeatherData, WeatherState } from "@/lib/types";
import { z } from "zod";

// Backend API configuration
const BACKEND_URL = process.env.NEXT_PUBLIC_BACKEND_URL || "http://localhost:8080";

// City to coordinates mapping (you can expand this or use a geocoding service)
const CITY_COORDS: Record<string, { lat: number; lng: number }> = {
  "london": { lat: 51.5074, lng: -0.1278 },
  "new york": { lat: 40.7128, lng: -74.0060 },
  "tokyo": { lat: 35.6762, lng: 139.6503 },
  "paris": { lat: 48.8566, lng: 2.3522 },
  "sydney": { lat: -33.8688, lng: 151.2093 },
  "toronto": { lat: 43.6532, lng: -79.3832 },
  "thunder bay": { lat: 48.3809, lng: -89.2477 },
};

// Fallback mock data generator for unknown cities
function generateMockData(city: string): WeatherData {
  const hash = city.split("").reduce((h, c) => ((h << 5) - h) + c.charCodeAt(0), 0);
  const conditions = ["Clear", "Clouds", "Rain", "Snow", "Thunderstorm", "Mist"];
  const now = new Date();
  
  const current = {
    city: city.split(',')[0].split(' ').map(word => word.charAt(0).toUpperCase() + word.slice(1)).join(' '),
    temperature: Math.round(10 + (Math.abs(hash) % 15) + Math.sin(now.getHours()) * 5),
    condition: conditions[Math.abs(hash) % conditions.length],
    humidity: 40 + (Math.abs(hash) % 40),
    windSpeed: 5 + (Math.abs(hash) % 15),
    date: now.toLocaleDateString('en-US', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' }),
  };

  const forecast = Array.from({ length: 5 }, (_, i) => ({
    day: new Date(now.getTime() + (i + 1) * 24 * 60 * 60 * 1000).toLocaleDateString('en-US', { weekday: 'short' }),
    high: current.temperature + 5 - (Math.abs(hash * (i + 1)) % 10),
    low: current.temperature - 5 - (Math.abs(hash * (i + 1)) % 5),
    condition: conditions[Math.abs(hash * (i + 1)) % conditions.length],
  }));

  return { current, forecast };
}

export async function getWeather(
  prevState: WeatherState,
  formData: FormData
): Promise<WeatherState> {
  const schema = z.object({
    city: z.string().min(1, "City name cannot be empty."),
  });
  const validatedFields = schema.safeParse({
    city: formData.get("city"),
  });

  if (!validatedFields.success) {
    return {
      error: validatedFields.error.flatten().fieldErrors.city?.[0] ?? "Invalid city name.",
    };
  }

  const { city } = validatedFields.data;
  const cityLower = city.toLowerCase();

  try {
    // Get coordinates for the city
    const coords = CITY_COORDS[cityLower];
    if (!coords) {
      return {
        error: `City "${city}" not found. Supported cities: ${Object.keys(CITY_COORDS).map(c => c.charAt(0).toUpperCase() + c.slice(1)).join(", ")}.`,
      };
    }

    // Call the backend API
    const response = await fetch(
      `${BACKEND_URL}/weather?latitude=${coords.lat}&longitude=${coords.lng}`,
      { method: "GET" }
    );

    if (!response.ok) {
      return {
        error: `Failed to fetch weather data. Status: ${response.status}`,
      };
    }

    const backendData = await response.json();

    // Transform backend data to match frontend format
    const weatherData: WeatherData = {
      current: {
        city: city.split(',')[0].split(' ').map(word => word.charAt(0).toUpperCase() + word.slice(1)).join(' '),
        temperature: Math.round(backendData.current.temperature),
        condition: getWeatherCondition(backendData.current.weatherCode),
        humidity: backendData.current.relativeHumidity,
        windSpeed: Math.round(backendData.current.windSpeed),
        date: new Date().toLocaleDateString('en-US', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' }),
      },
      forecast: backendData.daily.time.slice(1, 6).map((dateStr: string, i: number) => ({
        day: new Date(dateStr).toLocaleDateString('en-US', { weekday: 'short' }),
        high: Math.round(backendData.daily.temperatureMax[i + 1]),
        low: Math.round(backendData.daily.temperatureMin[i + 1]),
        condition: getWeatherCondition(backendData.daily.weatherCode[i + 1]),
      })),
    };

    return {
      weatherData: weatherData,
      message: `Successfully fetched weather for ${weatherData.current.city}.`,
    };
  } catch (error) {
    console.error("Error fetching weather:", error);
    return {
      error: "Failed to fetch weather data. Please check your backend connection.",
    };
  }
}

// Map Open-Meteo weather codes to readable conditions
// https://open-meteo.com/en/docs
function getWeatherCondition(weatherCode: number): string {
  const weatherMap: Record<number, string> = {
    0: "Clear",
    1: "Mainly Clear",
    2: "Partly Cloudy",
    3: "Overcast",
    45: "Foggy",
    48: "Foggy",
    51: "Light Drizzle",
    53: "Drizzle",
    55: "Heavy Drizzle",
    56: "Freezing Drizzle",
    57: "Freezing Drizzle",
    61: "Light Rain",
    63: "Rain",
    65: "Heavy Rain",
    66: "Freezing Rain",
    67: "Freezing Rain",
    71: "Light Snow",
    73: "Snow",
    75: "Heavy Snow",
    77: "Snow Grains",
    80: "Light Showers",
    81: "Showers",
    82: "Heavy Showers",
    85: "Light Snow Showers",
    86: "Snow Showers",
    95: "Thunderstorm",
    96: "Thunderstorm with Hail",
    99: "Thunderstorm with Hail",
  };
  return weatherMap[weatherCode] || "Clear";
}

export async function rateForecast(rating: number, city: string) {
  console.log(`Rating for ${city}: ${rating} stars`);
  await new Promise((resolve) => setTimeout(resolve, 500));
  return { message: `Thank you for rating the forecast for ${city}!` };
