"use client";

import { Card, CardHeader, CardTitle, CardFooter } from "@/components/atoms/card";
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
    return (
        <div className="">
            <h2 className="capitalize">{name}</h2>
            <div className="grid grid-cols-2 sm:grid-cols-4 md:grid-cols-6 lg:grid-cols-11 gap-4">
                {shades.map((shade) => {
                    const isLightShade = shade.position < 500;
                    const isCopied = copiedShade === shade.position;
                    return (
                        <Card
                            key={shade.position}
                            style={{ backgroundColor: shade.hex }}
                            onClick={() => handleCopy(shade.position, shade[format])}
                            className={`relative overflow-hidden border-0cursor-pointer transition-all duration-200 hover:scale-105 hover:shadow-md active:scale-95 ${
                                isLightShade ? "text-neutral-950" : "text-white"
                            }`}
                            title={`Copy ${shade[format]} to clipboard`}
                        >
                            <CardHeader>
                                <CardTitle>{shade.position}</CardTitle>
                            </CardHeader>
                            <CardFooter>
                                <span className="lowercase truncate">{shade[format]}</span>
                            </CardFooter>
                            <div
                                className={`absolute inset-0 flex flex-col items-center justify-center bg-background transition-all duration-300 ${
                                    isCopied ? "opacity-100 z-10" : "opacity-0 -z-10"
                                }`}
                            >
                                <Check className="w-5 h-5 text-card-foreground!" />
                                <span className="text-card-foreground! font-semibold text-xs">
                                    Copied!
                                </span>
                            </div>
                        </Card>
                    );
                })}
            </div>
        </div>
    );
}
