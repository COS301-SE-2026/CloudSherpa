"use client";
import Colours from "@/design-system/colours/components/colours";
import Typography from "@/design-system/typography/components/typography";
import LayoutAndSpacing from "@/design-system/layout-and-spacing/components/layoutAndSpacing";
import { useEffect, useRef, useState } from "react";
import { Search, ChevronsUpDown, Check } from "lucide-react";
import { cn } from "@/lib/utils";
import { Button } from "@/components/atoms/button";
import { Input } from "@/components/atoms/input";
import {
    Command,
    CommandEmpty,
    CommandGroup,
    CommandInput,
    CommandItem,
    CommandList,
} from "@/components/atoms/command";
import { Popover, PopoverContent, PopoverTrigger } from "@/components/atoms/popover";

const sections = [
    { value: "colours", label: "1. Colour Palette" },
    { value: "typography", label: "2. Typography System" },
    { value: "layout", label: "3. Layout & Spacing" },
];

export default function DesignSystem() {
    const [open, setOpen] = useState(false);
    const [comboboxValue, setComboboxValue] = useState("");

    const scrollToSection = (id: string) => {
        const element = document.getElementById(id);
        if (element) {
            const y = element.getBoundingClientRect().top + window.scrollY - 80;
            window.scrollTo({ top: y, behavior: "smooth" });
        }
    };

    return (
        <main className="min-h-screen">
            <div className="sticky top-0 z-50 w-full border-b bg-background/95 backdrop-blur supports-backdrop-filter:bg-background/60 shadow-sm">
                <div className="mx-auto flex h-16 max-w-7xl items-center justify-between px-4 sm:px-8 gap-4">
                    <Popover open={open} onOpenChange={setOpen}>
                        <PopoverTrigger asChild>
                            <Button
                                variant="outline"
                                role="combobox"
                                aria-expanded={open}
                                className="w-62.5 justify-between"
                            >
                                {comboboxValue
                                    ? sections.find((sec) => sec.value === comboboxValue)?.label
                                    : "Quick Navigate..."}
                                <ChevronsUpDown className="ml-2 h-4 w-4 shrink-0 opacity-50" />
                            </Button>
                        </PopoverTrigger>
                        <PopoverContent className="w-62.5 p-0">
                            <Command>
                                <CommandList>
                                    <CommandEmpty>No section found.</CommandEmpty>
                                    <CommandGroup>
                                        {sections.map((section) => (
                                            <CommandItem
                                                key={section.value}
                                                value={section.value}
                                                onSelect={(currentValue) => {
                                                    setComboboxValue(
                                                        currentValue === comboboxValue
                                                            ? ""
                                                            : currentValue
                                                    );
                                                    setOpen(false);
                                                    scrollToSection(currentValue);
                                                }}
                                            >
                                                <Check
                                                    className={cn(
                                                        "mr-2 h-4 w-4",
                                                        comboboxValue === section.value
                                                            ? "opacity-100"
                                                            : "opacity-0"
                                                    )}
                                                />
                                                {section.label}
                                            </CommandItem>
                                        ))}
                                    </CommandGroup>
                                </CommandList>
                            </Command>
                        </PopoverContent>
                    </Popover>
                </div>
            </div>
            <div className="mx-auto w-full max-w-7xl px-4 sm:px-8 py-12 md:py-16">
                <header className="mb-12 md:mb-16">
                    <h1 className="text-4xl md:text-5xl font-black tracking-tight mb-4">
                        CloudSherpa Design System
                    </h1>
                    <p className="text-lg md:text-xl text-muted-foreground max-w-3xl leading-relaxed">
                        The Golden Thread connecting our brand values to our shipped code. This
                        living document serves as the single source of truth for our visual
                        identity, ensuring a cohesive, accessible, and scalable experience across
                        the entire platform.
                    </p>
                </header>

                <div className="flex flex-col gap-16">
                    <section id="colours">
                        <div className="border-b pb-4 mb-8">
                            <h2 className="text-2xl md:text-3xl font-bold text-foreground">
                                1. Colour Palette
                            </h2>
                            <p className="text-muted-foreground">
                                Our refined colour system, built for WCAG 2.2 AA compliance and
                                semantic clarity.
                            </p>
                        </div>
                        <Colours />
                    </section>

                    <section id="typography">
                        <div className="border-b pb-4 mb-8">
                            <h2 className="text-2xl md:text-3xl font-bold text-foreground">
                                2. Typography
                            </h2>
                            <p className="text-muted-foreground">
                                Our Typography system is geared toward data representation by using
                                compact fonts while keeping visibility and clarity in mind
                            </p>
                        </div>
                        <Typography />
                    </section>

                    <section id="layout">
                        <div className="border-b pb-4 mb-8">
                            <h2 className="text-2xl md:text-3xl font-bold text-foreground">
                                3. Layout and Spacing
                            </h2>
                            <p className="text-muted-foreground">
                                Once again our layout and spacing is geared toward compact design to
                                allow space for efficient data representation without sacrificing
                                clarity.
                            </p>
                        </div>
                        <LayoutAndSpacing />
                    </section>
                </div>
            </div>
        </main>
    );
}
