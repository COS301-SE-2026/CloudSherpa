"use client";

import { Card } from "@/components/atoms/card"; // Adjust if you use /ui/card
import { useState, useRef } from "react";
import { Check } from "lucide-react";

// Explicitly mapping the Background (Left) and Foreground (Right) halves
const semanticPairs = [
    {
        name: "Background",
        leftBg: "bg-background",
        leftText: "text-foreground",
        leftLabel: "bg-background",
        rightBg: "bg-foreground",
        rightText: "text-background",
        rightLabel: "text-foreground",
    },
    {
        name: "Card",
        leftBg: "bg-card",
        leftText: "text-card-foreground",
        leftLabel: "bg-card",
        rightBg: "bg-card-foreground",
        rightText: "text-card",
        rightLabel: "text-card-foreground",
    },
    {
        name: "Popover",
        leftBg: "bg-popover",
        leftText: "text-popover-foreground",
        leftLabel: "bg-popover",
        rightBg: "bg-popover-foreground",
        rightText: "text-popover",
        rightLabel: "text-popover-foreground",
    },
    {
        name: "Primary",
        leftBg: "bg-primary",
        leftText: "text-primary-foreground",
        leftLabel: "bg-primary",
        rightBg: "bg-primary-foreground",
        rightText: "text-primary",
        rightLabel: "text-primary-foreground",
    },
    {
        name: "Secondary",
        leftBg: "bg-secondary",
        leftText: "text-secondary-foreground",
        leftLabel: "bg-secondary",
        rightBg: "bg-secondary-foreground",
        rightText: "text-secondary",
        rightLabel: "text-secondary-foreground",
    },
    {
        name: "Muted",
        leftBg: "bg-muted",
        leftText: "text-muted-foreground",
        leftLabel: "bg-muted",
        rightBg: "bg-muted-foreground",
        rightText: "text-muted",
        rightLabel: "text-muted-foreground",
    },
    {
        name: "Accent",
        leftBg: "bg-accent",
        leftText: "text-accent-foreground",
        leftLabel: "bg-accent",
        rightBg: "bg-accent-foreground",
        rightText: "text-accent",
        rightLabel: "text-accent-foreground",
    },
    {
        name: "Destructive",
        leftBg: "bg-destructive",
        leftText: "text-destructive-foreground",
        leftLabel: "bg-destructive",
        rightBg: "bg-destructive-foreground",
        rightText: "text-destructive",
        rightLabel: "text-destructive-foreground",
    },
    {
        name: "Success",
        leftBg: "bg-success",
        leftText: "text-success-foreground",
        leftLabel: "bg-success",
        rightBg: "bg-success-foreground",
        rightText: "text-success",
        rightLabel: "text-success-foreground",
    },
    {
        name: "Warning",
        leftBg: "bg-warning",
        leftText: "text-warning-foreground",
        leftLabel: "bg-warning",
        rightBg: "bg-warning-foreground",
        rightText: "text-warning",
        rightLabel: "text-warning-foreground",
    },
];

export default function SemanticColours() {
    const [copiedClass, setCopiedClass] = useState<string | null>(null);
    const timeoutRef = useRef<NodeJS.Timeout | null>(null);

    const handleCopy = (value: string) => {
        navigator.clipboard.writeText(value);
        setCopiedClass(value);
        if (timeoutRef.current) {
            clearTimeout(timeoutRef.current);
        }
        timeoutRef.current = setTimeout(() => {
            setCopiedClass(null);
        }, 1500);
    };

    const handleKeyDown = (e: React.KeyboardEvent, value: string) => {
        if (e.key === "Enter" || e.key === " ") {
            e.preventDefault();
            handleCopy(value);
        }
    };

    return (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6 md:gap-8">
            {semanticPairs.map((pair) => (
                <div key={pair.name} className="space-y-3">
                    <h3 className="text-lg font-bold tracking-tight">{pair.name}</h3>

                    <Card className="flex flex-row w-full h-32 md:h-40 overflow-hidden rounded-xl border-border shadow-sm p-0 gap-0">
                        <div
                            role="button"
                            tabIndex={0}
                            onClick={() => handleCopy(pair.leftLabel)}
                            onKeyDown={(e) => handleKeyDown(e, pair.leftLabel)}
                            className={`relative group/swatch flex-1 hover:flex-[4] focus-visible:flex-[4] transition-all duration-500 ease-[cubic-bezier(0.25,1,0.5,1)] cursor-pointer overflow-hidden border-r border-black/10 dark:border-white/10 outline-none ${pair.leftBg} ${pair.leftText}`}
                            title={`Copy ${pair.leftLabel}`}
                        >
                            <div className="absolute top-3 w-full flex justify-center transition-opacity duration-300 group-hover/swatch:opacity-0 group-focus-visible/swatch:opacity-0">
                                <span className="font-bold text-xs opacity-70">BG</span>
                            </div>
                            <div className="absolute bottom-4 left-4 min-w-35 flex flex-col opacity-0 group-hover/swatch:opacity-100 group-focus-visible/swatch:opacity-100 transition-all duration-500 translate-y-4 group-hover/swatch:translate-y-0 group-focus-visible/swatch:translate-y-0">
                                <div className="font-bold font-mono text-xs">{pair.leftLabel}</div>
                            </div>
                            {/* Copied Success Overlay */}
                            <div
                                className={`absolute inset-0 flex flex-col items-center justify-center bg-black/30 backdrop-blur-sm transition-all duration-300 ${copiedClass === pair.leftLabel ? "opacity-100 z-20" : "opacity-0 -z-10"}`}
                            >
                                <Check className="w-6 h-6 text-white drop-shadow-md" />
                            </div>
                        </div>

                        <div
                            role="button"
                            tabIndex={0}
                            onClick={() => handleCopy(pair.rightLabel)}
                            onKeyDown={(e) => handleKeyDown(e, pair.rightLabel)}
                            className={`relative group/swatch flex-1 hover:flex-[4] focus-visible:flex-[4] transition-all duration-500 ease-[cubic-bezier(0.25,1,0.5,1)] cursor-pointer overflow-hidden outline-none ${pair.rightBg} ${pair.rightText}`}
                            title={`Copy ${pair.rightLabel}`}
                        >
                            <div className="absolute top-3 w-full flex justify-center transition-opacity duration-300 group-hover/swatch:opacity-0 group-focus-visible/swatch:opacity-0">
                                <span className="font-bold text-xs opacity-70">FG</span>
                            </div>
                            <div className="absolute bottom-4 left-4 min-w-35 flex flex-col opacity-0 group-hover/swatch:opacity-100 group-focus-visible/swatch:opacity-100 transition-all duration-500 translate-y-4 group-hover/swatch:translate-y-0 group-focus-visible/swatch:translate-y-0">
                                <div className="font-bold font-mono text-xs">{pair.rightLabel}</div>
                            </div>
                            <div
                                className={`absolute inset-0 flex flex-col items-center justify-center bg-black/30 backdrop-blur-sm transition-all duration-300 ${copiedClass === pair.rightLabel ? "opacity-100 z-20" : "opacity-0 -z-10"}`}
                            >
                                <Check className="w-6 h-6 text-white drop-shadow-md" />
                            </div>
                        </div>
                    </Card>
                </div>
            ))}
        </div>
    );
}
