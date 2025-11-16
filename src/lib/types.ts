/**
 * TypeScript type definitions for WeatherDesk
 * Provides type safety for weather data and component props
 */

/**
 * Current weather conditions
 */
export interface CurrentWeather {
  city: string;
  temperature: number; // in Fahrenheit
  condition: string; // e.g., "Clear", "Cloudy", "Rain"
  humidity: number; // 0-100 percentage
  windSpeed: number; // in mph
  date: string; // formatted date string
}

/**
 * Weather forecast for a single day
 */
export interface ForecastDay {
  day: string; // day name, e.g., "Monday"
  high: number; // high temperature in Fahrenheit
  low: number; // low temperature in Fahrenheit
  condition: string; // weather condition
}

/**
 * Complete weather data for a location
 */
export interface WeatherData {
  current: CurrentWeather;
  forecast: ForecastDay[];
}

/**
 * State for weather form submission
 */
export interface WeatherState {
  weatherData?: WeatherData;
  error?: string;
  message?: string;
}

/**
 * Backend API response from WeatherDesk23
 */
export interface BackendWeatherResponse {
  longitude: number;
  latitude: number;
  current: {
    temperature_2m: number;
    relative_humidity_2m: number;
    is_day: number;
    wind_speed_10m: number;
  };
}

/**
 * City coordinates mapping
 */
export interface CityCoordinates {
  lat: number;
  lng: number;
}

/**
 * Weather dashboard component props
 */
export interface WeatherDashboardProps {
  initialState?: WeatherState;
}

/**
 * Current weather card component props
 */
export interface CurrentWeatherCardProps {
  data: CurrentWeather;
}

/**
 * Forecast card component props
 */
export interface ForecastCardProps {
  data: ForecastDay;
}

/**
 * Error response structure
 */
export interface ErrorResponse {
  error: string;
  timestamp?: string;
  details?: Record<string, unknown>;
}

/**
 * Success response structure
 */
export interface SuccessResponse<T> {
  data: T;
  message?: string;
  timestamp?: string;
}

/**
 * API response type (either success or error)
 */
export type ApiResponse<T> = SuccessResponse<T> | ErrorResponse;

/**
 * Supported weather conditions
 */
export type WeatherCondition =
  | "Clear"
  | "Cloudy"
  | "Cloudy"
  | "Overcast"
  | "Mist"
  | "Drizzle"
  | "Rain"
  | "Snow"
  | "Thunderstorm";

/**
 * Configuration for API calls
 */
export interface ApiConfig {
  baseUrl: string;
  timeout: number;
  retryAttempts: number;
}

/**
 * Rating submission data
 */
export interface RatingData {
  rating: number; // 1-5 stars
  city: string;
  timestamp: Date;
  feedback?: string;
}
