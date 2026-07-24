"use client";

import { Card } from "@/components/atoms/card";
import { Shade, ColorFormat } from "@/design-system/colours/types/colours";
import { useState, useRef } from "react";
import { Check } from "lucide-react";

interface ShadesProps {
    name: string;
    shades: Shade[];
    format: ColorFormat;
}

export default function Shades({ name, shades, format }: Readonly<ShadesProps>) {
    const [copiedShade, setCopiedShade] = useState<number | null>(null);
    const timeoutRef = useRef<NodeJS.Timeout | null>(null);

    const handleCopy = (position: number, value: string) => {
        navigator.clipboard.writeText(value);
        setCopiedShade(position);
        if (timeoutRef.current) {
            clearTimeout(timeoutRef.current);
        }
        timeoutRef.current = setTimeout(() => {
            setCopiedShade(null);
        }, 1500);
    };

    const handleKeyDown = (e: React.KeyboardEvent, position: number, value: string) => {
        if (e.key === "Enter" || e.key === " ") {
            e.preventDefault();
            handleCopy(position, value);
        }
    };

    return (
        <div className="space-y-3">
            <h3 className="text-lg font-bold tracking-tight">{name}</h3>
            <Card className="flex flex-row w-full h-32 md:h-48 overflow-hidden p-0 gap-0">
                {shades.map((shade) => {
                    const isLightShade = shade.position < 500;
                    const isCopied = copiedShade === shade.position;
                    const textColor = isLightShade ? "text-neutral-950" : "text-white";

                    return (
                        <Card
                            key={shade.position}
                            role="button"
                            tabIndex={0}
                            onKeyDown={(e) => handleKeyDown(e, shade.position, shade[format])}
                            onClick={() => handleCopy(shade.position, shade[format])}
                            style={{ backgroundColor: shade.hex }}
                            className={`relative group/swatch flex-1 hover:flex-4 transition-all duration-500 ease-[cubic-bezier(0.25,1,0.5,1)] cursor-pointer overflow-hidden ${textColor} rounded-none`}
                            title={`Copy ${shade[format]} to clipboard`}
                        >
                            <div className="absolute top-3 left-0 w-full flex justify-center transition-opacity duration-300 group-hover/swatch:opacity-0">
                                <span className="font-bold text-xs rotate-90 md:rotate-0 mt-4 md:mt-0">
                                    {shade.position}
                                </span>
                            </div>

                            <div className="absolute bottom-4 left-4 min-w-35 flex flex-col opacity-0 group-hover/swatch:opacity-100 transition-all duration-500 translate-y-4 group-hover/swatch:translate-y-0">
                                <div className="text-lg mb-1">{shade.position}</div>
                                <div className="flex flex-col gap-0.5 text-xs font-mono">
                                    <span className="uppercase font-semibold tracking-wider">
                                        {shade.hex}
                                    </span>
                                    <span>{shade.rgb}</span>
                                    <span>{shade.hsl}</span>
                                </div>
                            </div>

                            <div
                                className={`absolute inset-0 flex flex-col items-center justify-center bg-black/30 backdrop-blur-sm transition-all duration-300 ${
                                    isCopied ? "opacity-100 z-20" : "opacity-0 -z-10"
                                }`}
                            >
                                <Check className="w-6 h-6 text-white drop-shadow-md mb-1" />
                            </div>
                        </Card>
                    );
                })}
            </Card>
        </div>
    );
}
