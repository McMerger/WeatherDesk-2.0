"use client";

import { useActionState } from "react";
import { useFormStatus } from "react-dom";
import { useEffect, useRef } from "react";
import { CloudSun, Search } from "lucide-react";

import { getWeather } from "@/app/actions";
import type { WeatherData, WeatherState } from "@/lib/types";
import { Input } from "@/components/ui/input";
import { SubmitButton } from "@/components/submit-button";
import { useToast } from "@/hooks/use-toast";
import { CurrentWeatherCard } from "./current-weather-card";
import { ForecastCard } from "./forecast-card";
import { Card, CardContent } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";

const initialState: WeatherState = {
  weatherData: null,
  error: null,
  message: null
};

function WeatherResults({ data }: { data: WeatherData | null | undefined }) {
  const { pending } = useFormStatus();

  if (pending) {
    return (
      <div className="space-y-6">
        <Skeleton className="h-[230px] w-full rounded-lg" />
        <Skeleton className="h-[280px] w-full rounded-lg" />
      </div>
    );
  }

  if (data) {
    return (
      <div className="space-y-6 animate-in fade-in-50 duration-500">
        <CurrentWeatherCard data={data.current} />
        <ForecastCard forecast={data.forecast} current={data.current} />
      </div>
    );
  }

  return (
    <Card className="border-dashed">
      <CardContent className="p-10 flex flex-col items-center justify-center text-center text-muted-foreground h-96">
        <CloudSun className="w-16 h-16 mb-4 text-primary"/>
        <h3 className="text-lg font-semibold text-foreground">Welcome to WeatherDesk</h3>
        <p>Enter a city to get the latest weather forecast.</p>
      </CardContent>
    </Card>
  );
}


export function WeatherDashboard() {
  const [state, formAction] = useActionState(getWeather, initialState);
  const { toast } = useToast();
  const initialLoadRef = useRef(true);

  useEffect(() => {
    if (initialLoadRef.current) {
      const lastCity = localStorage.getItem('lastCity');
      if (lastCity) {
        const formData = new FormData();
        formData.append('city', lastCity);
        formAction(formData);
      }
      initialLoadRef.current = false;
    }
  }, [formAction]);

  useEffect(() => {
    if (state.error && !initialLoadRef.current) {
      toast({
        variant: "destructive",
        title: "Error",
        description: state.error,
      });
    }
    if (state.weatherData) {
      localStorage.setItem('lastCity', state.weatherData.current.city);
    }
  }, [state, toast]);

  return (
    <form action={formAction} className="space-y-6">
        <Card className="shadow-lg">
          <CardContent className="p-4">
            <div className="flex items-center gap-2">
              <div className="relative flex-grow">
                <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-5 w-5 text-muted-foreground" />
                <Input
                  type="text"
                  name="city"
                  placeholder="E.g., London, New York, Tokyo"
                  className="pl-10 text-base"
                  required
                />
              </div>
              <SubmitButton>
                <Search className="h-5 w-5 md:hidden" />
                <span className="hidden md:inline">Search</span>
              </SubmitButton>
            </div>
          </CardContent>
        </Card>
        
        <WeatherResults data={state.weatherData} />
    </form>
  );
}
