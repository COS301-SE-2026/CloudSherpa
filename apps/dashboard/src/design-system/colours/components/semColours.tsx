"use client";

import { Card } from "@/components/atoms/card";
import { useState, useRef } from "react";
import { Check } from "lucide-react";

const semanticNames = [
    "background",
    "card",
    "popover",
    "primary",
    "secondary",
    "muted",
    "accent",
    "destructive",
    "success",
    "warning",
];

const generateSemanticPair = (baseName: string) => {
    const title = baseName.charAt(0).toUpperCase() + baseName.slice(1);

    const foregroundName = baseName === "background" ? "foreground" : `${baseName}-foreground`;

    return {
        name: title,
        leftLabel: `bg-${baseName}`,
        rightLabel: `text-${foregroundName}`,
        leftBg: `bg-${baseName}`,
        leftText: `text-${foregroundName}`,
        rightBg: `bg-${foregroundName}`,
        rightText: `text-${baseName}`,
    };
};
interface ExpandingHalfProps {
    label: string;
    text: string;
    bg: string;
    side: string;
}

function ExpandingHalf({ bg, label, text, side }: Readonly<ExpandingHalfProps>) {
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
        <button
            type="button"
            tabIndex={0}
            onClick={() => handleCopy(label)}
            onKeyDown={(e) => handleKeyDown(e, label)}
            className={`h-full rounded-none relative group/swatch flex-1 hover:flex-4 focus-visible:flex-4 transition-all duration-500 ease-[cubic-bezier(0.25,1,0.5,1)] cursor-pointer overflow-hidden border-r border-black/10 dark:border-white/10 outline-none ${bg} ${text}`}
            title={`Copy ${label}`}
        >
            <div className="absolute top-3 w-full flex justify-center transition-opacity duration-300 group-hover/swatch:opacity-0 group-focus-visible/swatch:opacity-0">
                <span className="font-bold text-xs opacity-70">{side}</span>
            </div>
            <div className="absolute bottom-4 left-4 min-w-35 flex flex-col opacity-0 group-hover/swatch:opacity-100 group-focus-visible/swatch:opacity-100 transition-all duration-500 translate-y-4 group-hover/swatch:translate-y-0 group-focus-visible/swatch:translate-y-0">
                <div className="font-bold font-mono text-xs">{label}</div>
            </div>
            {/* overlay */}
            <div
                className={`absolute inset-0 flex flex-col items-center justify-center bg-black/30 backdrop-blur-sm transition-all duration-300 ${copiedClass === label ? "opacity-100 z-20" : "opacity-0 -z-10"}`}
            >
                <Check className="w-6 h-6 text-white drop-shadow-md" />
            </div>
        </button>
    );
}

export default function SemColours() {
    const pairs = semanticNames.map(generateSemanticPair);
    return (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6 md:gap-8">
            {pairs.map((pair) => (
                <div key={pair.name} className="space-y-3">
                    <h3 className="text-lg font-bold tracking-tight">{pair.name}</h3>

                    <Card className="flex flex-row w-full h-32 md:h-40 overflow-hidden rounded-xl border-border shadow-sm p-0 gap-0">
                        <ExpandingHalf
                            label={pair.leftLabel}
                            bg={pair.leftBg}
                            text={pair.leftText}
                            side="BG"
                        />

                        <ExpandingHalf
                            label={pair.rightLabel}
                            bg={pair.rightBg}
                            text={pair.rightText}
                            side="FG"
                        />
                    </Card>
                </div>
            ))}
        </div>
    );
}
