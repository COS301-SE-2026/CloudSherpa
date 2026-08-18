import { CurrencyEnum } from "@/features/dashboard/types/currency";
import { clsx, type ClassValue } from "clsx";
import { twMerge } from "tailwind-merge";

export function cn(...inputs: ClassValue[]) {
    return twMerge(clsx(inputs));
}

export const CURRENCY_SYMBOLS: Record<CurrencyEnum, string> = {
    USD: "$",
    ZAR: "R",
    EUR: "€",
};

export function getCurrencySymbol(currency: CurrencyEnum): string {
    return CURRENCY_SYMBOLS[currency];
}
