import { clsx, type ClassValue } from "clsx";
import { twMerge } from "tailwind-merge";

export function cn(...inputs: ClassValue[]) {
    return twMerge(clsx(inputs));
}

export const timeMs = {
    minuteMs: 60 * 1000,
    hourMs: 60 * 60 * 1000,
    dayMs: 24 * 60 * 60 * 1000,
} as const;
