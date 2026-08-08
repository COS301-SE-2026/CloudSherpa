import { create } from "zustand";
import { usageForecastData } from "@/features/intelligence/types/metrics";

interface UsageIntelligenceStore {
    //forecast format will look like {resourceid {CPU,  data}, {DISK,  data}}
    forecasts: Record<string, Record<string, usageForecastData>>;

    setUsageForecast: (resourceId: string, metricType: string, data: usageForecastData) => void;
    clearForecasts: () => void;
}

export const useUsageIntelligenceStore = create<UsageIntelligenceStore>((set) => ({
    forecasts: {},

    setUsageForecast: (resourceId, metricType, data) => {
        set((state) => ({
            forecasts: {
                ...state.forecasts,
                [resourceId]: {
                    ...(state.forecasts[resourceId] || {}),
                    [metricType]: data,
                },
            },
        }));
    },

    clearForecasts: () => set({ forecasts: {} }),
}));
