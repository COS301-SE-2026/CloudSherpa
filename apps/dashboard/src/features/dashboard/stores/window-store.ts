import { create } from "zustand";
import { persist } from "zustand/middleware";
import { TimeWindowPreset } from "@/features/dashboard/types/timewindow";

type TimeWindow = {
    fromMs: number;
    toMs: number;
    selectedPreset: TimeWindowPreset;
    minutes?: number;
    hours?: number;
    days?: number;
    setWindow: (from: Date, to: Date) => void;
    setPreset: (preset: TimeWindowPreset) => void;
};

export const useWindowStore = create<TimeWindow>()(
    persist(
        (set) => ({
            fromMs: Date.now() - 7 * 24 * 60 * 60 * 1000,
            toMs: Date.now(),
            selectedPreset: "7d",
            setWindow: (from, to) => set({ fromMs: from.getTime(), toMs: to.getTime() }),
            setPreset: (preset) => set({ selectedPreset: preset }),
        }),
        {
            name: "dashboard-window",
        }
    )
);
