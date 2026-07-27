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
    timeoutId?: ReturnType<typeof setTimeout>;
    intervalId?: ReturnType<typeof setInterval>;
    clear: () => void;
};

const tickIntervalMs = 30000;

function getDefaultWindow() {
    const toMs = Date.now();

    return {
        fromMs: toMs - 7 * 24 * 60 * 60 * 1000,
        toMs,
        selectedPreset: "7d" as TimeWindowPreset,
    };
}

export const useWindowStore = create<TimeWindow>()(
    persist(
        (set, get) => ({
            ...getDefaultWindow(),
            setWindow: (from, to) => {
                set({ fromMs: from.getTime(), toMs: to.getTime() });

                clearTimeout(get().timeoutId ?? undefined);
                clearInterval(get().intervalId ?? undefined);

                if (get().selectedPreset == "custom") {
                    return;
                }

                const timeoutId = setTimeout(() => {
                    const intervalId = setInterval(() => {
                        set({
                            fromMs: get().fromMs + tickIntervalMs,
                            toMs: get().toMs + tickIntervalMs,
                        });
                    }, tickIntervalMs);

                    set({ intervalId: intervalId });
                }, tickIntervalMs);

                set({ timeoutId: timeoutId });
            },
            setPreset: (preset) => set({ selectedPreset: preset }),
            clear: () => {
                clearTimeout(get().timeoutId ?? undefined);
                clearInterval(get().intervalId ?? undefined);

                set({
                    ...getDefaultWindow(),
                    timeoutId: undefined,
                    intervalId: undefined,
                });
            },
        }),
        {
            name: "dashboard-window",
            partialize: (state) => ({
                fromMs: state.fromMs,
                toMs: state.toMs,
                selectedPreset: state.selectedPreset,
            }),
        }
    )
);
