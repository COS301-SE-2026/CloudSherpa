import { TimeWindowPreset } from "@/features/dashboard/types/timewindow";

export const timeMs = {
    minuteMs: 60 * 1000,
    hourMs: 60 * 60 * 1000,
    dayMs: 24 * 60 * 60 * 1000,
} as const;

export const presets: { id: TimeWindowPreset; label: string }[] = [
    { id: "T_5_MIN", label: "5 min" },
    { id: "T_15_MIN", label: "15 min" },
    { id: "T_30_MIN", label: "30 min" },
    { id: "T_1_HOUR", label: "1 hour" },
    { id: "T_6_HOUR", label: "6 hours" },
    { id: "T_12_HOUR", label: "12 hours" },
    { id: "T_24_HOUR", label: "24 hours" },
    { id: "T_7_DAYS", label: "7 days" },
    { id: "T_30_DAYS", label: "30 days" },
];

export const durationByPreset: Record<TimeWindowPreset, number> = {
    T_5_MIN: 5 * timeMs.minuteMs,
    T_15_MIN: 15 * timeMs.minuteMs,
    T_30_MIN: 30 * timeMs.minuteMs,
    T_1_HOUR: timeMs.hourMs,
    T_6_HOUR: 6 * timeMs.hourMs,
    T_12_HOUR: 12 * timeMs.hourMs,
    T_24_HOUR: timeMs.dayMs,
    T_7_DAYS: 7 * timeMs.dayMs,
    T_30_DAYS: 30 * timeMs.dayMs,
    custom: 0,
};
