"use client";
import Colours from "@/design-system/colours/components/colours";
import Typography from "@/design-system/typography/components/typography";
import LayoutAndSpacing from "@/design-system/layout-and-spacing/components/layoutAndSpacing";
import Components from "@/design-system/components/components/components";
import Logo from "@/design-system/logo/components/Logo";
import Iconography from "@/design-system/icons/components/Iconography";
import VoiceAndTone from "@/design-system/voice-and-tone/components/voice-and-tone";
import Accessibility from "@/design-system/accessibility/components/accessibility";
import { useState } from "react";
import { ChevronsUpDown, Check } from "lucide-react";
import { cn } from "@/lib/utils";
import { Button } from "@/components/atoms/button";
import {
    Command,
    CommandEmpty,
    CommandGroup,
    CommandItem,
    CommandList,
} from "@/components/atoms/command";
import { Popover, PopoverContent, PopoverTrigger } from "@/components/atoms/popover";
import Changelog from "@/design-system/changelog/components/changelog";

interface HeaderProps {
    title: string;
    description: string;
}

function Header({ title, description }: Readonly<HeaderProps>) {
    return (
        <div className="border-b pb-4 mb-8 gap-2 flex flex-col items-start justify-between">
            <h2 className="text-2xl md:text-3xl font-bold text-foreground">{title}</h2>
            <p className="text-muted-foreground">{description}</p>
        </div>
    );
}

const sections = [
    { value: "voice-and-tone", label: "1 Voice & Tone" },
    { value: "colours", label: "2. Colour Palette" },
    { value: "typography", label: "3. Typography System" },
    { value: "layout", label: "4. Layout & Spacing" },
    { value: "components", label: "5. Components" },
    { value: "logo", label: "6. Logo" },
    { value: "iconography", label: "7. Iconography" },
    { value: "accessibility", label: "8. Accessibility" },
    { value: "changelog", label: "9. Changelog" },
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
            <div className="pointer-events-none fixed inset-0 -z-10 h-full w-full bg-background">
                <div className="absolute inset-0 bg-[radial-gradient(#00000022_1px,transparent_1px)] dark:bg-[radial-gradient(#ffffff22_1px,transparent_1px)] bg-size-[24px_24px] mask-[radial-gradient(ellipse_100%_100%_at_50%_0%,#000_40%,transparent_100%)"></div>
            </div>
            <div className="sticky top-0 z-50 w-full bg-popover border-b backdrop-blur supports-backdrop-filter:bg-background/60 shadow-sm">
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
                <header className="mb-12 md:mb-16 overflow-hidden">
                    <h1 className="text-4xl md:text-5xl font-black tracking-tight mb-4">
                        CloudSherpa Design System
                    </h1>
                    <p className="text-lg md:text-xl text-muted-foreground eading-relaxed">
                        The Golden Thread connecting our brand values to our shipped code. This
                        living document serves as the single source of truth for our visual
                        identity, ensuring a cohesive, accessible, and scalable experience across
                        the entire platform.
                        <br />
                        <br />
                        CloudSherpa makes multi-cloud spending visible and actionable for any
                        organization. Without it, teams face bill shock, stranded resources, and
                        fragmented visibility. With it, they get real-time alerts, automated
                        optimization, and clear cost accountability across AWS, Azure, and GCP. Our
                        purpose: trusted guardrails for the journey to cloud-native. It is built on
                        five values: safety (preventing bill shock), reliability, trust (no hidden
                        met- rics), transparency (explainable costs), and calm but honest
                        communication (alerts without panic).
                    </p>
                </header>
                <div className="flex flex-col gap-16">
                    <section id="voice-and-tone">
                        <Header
                            title="Voice and tone"
                            description="This category showcases our Logo as well as the icon set used in
                                CloudSherpa."
                        />
                        <VoiceAndTone />
                    </section>

                    <section id="colours">
                        <Header
                            title="Colour Palette"
                            description="Primitive and Semantic colour ranges that can be found in
                                CloudSherpa, representing our brand colours and base ui component
                                backgrounds and foregrounds that defines the feel of the
                                application."
                        />
                        <Colours />
                    </section>

                    <section id="typography">
                        <Header
                            title="Typography"
                            description="Our Typography system is geared toward data representation by using
                                compact fonts while keeping visibility and clarity in mind"
                        />
                        <Typography />
                    </section>

                    <section id="layout">
                        <Header
                            title="Layout and Spacing"
                            description="This category defines the structural geometry of CloudSherpa, and
                                defines the form of the application."
                        />
                        <LayoutAndSpacing />
                    </section>

                    <section id="components">
                        <Header
                            title="Components"
                            description="The components are the culmination of all the primitive building
                                blocks previously defined in the design system."
                        />
                        <Components />
                    </section>

                    <section id="logo">
                        <Header
                            title="Logo"
                            description="A logo is what identifies a brand, thus we tried to make our logo as visually representative of CloudSherpa as possible.
                        This section outlines our core brand identifier and how to properly use it.
                        "
                        />
                        <Logo />
                    </section>

                    <section id="iconography">
                        <Header
                            title="Iconography"
                            description="This category showcases our Logo as well as the icon set used in
                                CloudSherpa."
                        />
                        <Iconography />
                    </section>

                    <section id="accessibility">
                        <Header
                            title="Accessibility"
                            description="Accessibility directly impacts the user experience and ensure people with disabilites, like colour blind people, can use the application without issues."
                        />
                        <Accessibility />
                    </section>

                    <section id="changelog">
                        <Header
                            title="Changelog"
                            description="A log of all changes brought about to our colour palette. Due to contrast issues we had to reimplement our colour palette. We went from a very blue UI to 
                            a more muted palatable colour with a blue hue."
                        />
                        <Changelog />
                    </section>
                </div>
            </div>
        </main>
    );
}
