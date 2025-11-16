export interface CurrentWeather {
  city: string;
  temperature: number;
  condition: string;
  humidity: number;
  windSpeed: number;
  date: string;
}

export interface DailyForecast {
  day: string;
  high: number;
  low: number;
  condition: string;
}

export interface WeatherData {
  current: CurrentWeather;
  forecast: DailyForecast[];
}

export interface WeatherState {
  weatherData?: WeatherData | null;
  error?: string | null;
  message?: string | null;
}
