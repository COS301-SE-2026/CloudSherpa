import { Popover, PopoverContent, PopoverTrigger } from "@/components/atoms/popover";
import {
    Command,
    CommandEmpty,
    CommandGroup,
    CommandItem,
    CommandList,
} from "@/components/atoms/command";
import { Button } from "@/components/atoms/button";
import { useState } from "react";
import { Check, ChevronsUpDown } from "lucide-react";
import { cn } from "@/lib/utils";
import { ColorFormat } from "@/design-system/colours/types/colours";
import Shades from "@/design-system/colours/components/shades";
import rawTokens from "@/app/tokens/docs/design-tokens.json";
import SemColours from "@/design-system/colours/components/semColours";
import SubSectionHeading from "@/design-system/shared/components/subsectionHeading";

export default function Colours() {
    const [colourFormatOpen, setColourFormatOpen] = useState(false);
    const [selectedFormat, setSelectedFormat] = useState<ColorFormat>("hex");

    return (
        <div className="space-y-6">
            <Popover open={colourFormatOpen} onOpenChange={setColourFormatOpen}>
                <PopoverTrigger asChild>
                    <Button
                        variant="outline"
                        role="combobox"
                        aria-expanded={colourFormatOpen}
                        className="w-30 justify-between uppercase"
                    >
                        {selectedFormat}
                        <ChevronsUpDown className="ml-2 h-4 w-4 shrink-0 opacity-50" />
                    </Button>
                </PopoverTrigger>
                <PopoverContent className="p-0 w-30">
                    <Command>
                        <CommandList>
                            <CommandEmpty>No formats found</CommandEmpty>
                            <CommandGroup>
                                {(["hex", "rgb", "hsl"] as ColorFormat[]).map((format) => (
                                    <CommandItem
                                        key={format}
                                        value={format}
                                        onSelect={() => {
                                            setSelectedFormat(format);
                                            setColourFormatOpen(false);
                                        }}
                                    >
                                        <Check
                                            className={cn(
                                                "mr-2 h-4 w-4",
                                                selectedFormat === format
                                                    ? "opacity-100"
                                                    : "opacity-0"
                                            )}
                                        />
                                        <span className="uppercase">{format}</span>
                                    </CommandItem>
                                ))}
                            </CommandGroup>
                        </CommandList>
                    </Command>
                </PopoverContent>
            </Popover>

            <div className="space-y-6">
                <SubSectionHeading
                    title="Primitive Colours"
                    description="These primitive colour represent the basic building blocks of our colour themes, like a predefined library of predefined colours we can swap out at any time in our semantic colour
                    mapping to fit our needs."
                />
                {rawTokens.colors.map((palette) => (
                    <Shades
                        key={palette.name}
                        name={palette.name}
                        shades={palette.shades}
                        format={selectedFormat}
                    />
                ))}
            </div>

            <div className="space-y-6">
                <SubSectionHeading
                    title="Semantic Colours"
                    description="These semantic colours are based on the token that are predefined by Shadcn/ui when the library is first initialized and are tweaked to map to our primitive colour ranges
                    to fit the CloudSherpa aesthetic perfectly."
                />
                <SemColours />
            </div>
        </div>
    );
}
