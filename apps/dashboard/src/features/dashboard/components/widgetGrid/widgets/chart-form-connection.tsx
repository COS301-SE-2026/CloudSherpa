"use client";
import { useState } from "react";
import { ChartWidgetConfig } from "@/features/dashboard/types/widgets";
import { Check, ChevronsUpDown } from "lucide-react";
import { Label } from "@/components/atoms/label";
import { Button } from "@/components/atoms/button";
import { Popover, PopoverContent, PopoverTrigger } from "@/components/atoms/popover";
import {
    Command,
    CommandEmpty,
    CommandGroup,
    CommandInput,
    CommandItem,
    CommandList,
} from "@/components/atoms/command";
import { FieldSet, FieldLegend, FieldDescription, FieldGroup } from "@/components/atoms/field";
import { FormCountCircle } from "@/components/atoms/form-count-circle";

const PROVIDERS = ["AWS", "AZURE", "GCP"];

interface ChartFormConnectionProps {
    configuration: ChartWidgetConfig;
    setConfiguration: (config: ChartWidgetConfig) => void;
}

export default function ChartFormConnection({
    configuration,
    setConfiguration,
}: Readonly<ChartFormConnectionProps>) {
    const [providerOpen, setProviderOpen] = useState(false);
    const [connectionOpen, setConnectionOpen] = useState(false);
    return (
        <FieldSet>
            <div className="flex flex-row items-center gap-3">
                <FormCountCircle count={2} />
                <FieldLegend className="mb-0">Connection</FieldLegend>
            </div>
            <FieldDescription>
                Choose the connection for the resource to be displayed on the chart.
            </FieldDescription>
            <FieldGroup>
                <div className="grid gap-2">
                    <Label>Provider</Label>
                    <Popover open={providerOpen} onOpenChange={setProviderOpen}>
                        <PopoverTrigger asChild>
                            <Button
                                variant="outline"
                                role="combobox"
                                aria-expanded={providerOpen}
                                className="justify-between w-full"
                            >
                                <span className="truncate"></span>
                                <ChevronsUpDown className="ml-2 h-4 w-4 shrink-0 opacity-50" />
                            </Button>
                        </PopoverTrigger>
                        <PopoverContent className="p-0 w-(--radix-popover-trigger-width)">
                            <Command>
                                <CommandInput placeholder="Search resources..." />
                                <CommandList>
                                    <CommandEmpty>No provider found</CommandEmpty>
                                    <CommandGroup></CommandGroup>
                                </CommandList>
                            </Command>
                        </PopoverContent>
                    </Popover>
                </div>

                <div className="grid gap-2">
                    <Label>Connection</Label>
                    <Popover open={connectionOpen} onOpenChange={setConnectionOpen}>
                        <PopoverTrigger asChild>
                            <Button
                                variant="outline"
                                role="combobox"
                                aria-expanded={connectionOpen}
                                className="justify-between w-full"
                                disabled={!configuration.resourceId}
                            >
                                <span className="truncate"></span>
                                <ChevronsUpDown className="ml-2 h-4 w-4 shrink-0 opacity-50" />
                            </Button>
                        </PopoverTrigger>
                        <PopoverContent className="p-0 w-(--radix-popover-trigger-width)">
                            <Command>
                                <CommandInput placeholder="Search connections..." />
                                <CommandList>
                                    <CommandEmpty>No connections found.</CommandEmpty>
                                    <CommandGroup></CommandGroup>
                                </CommandList>
                            </Command>
                        </PopoverContent>
                    </Popover>
                </div>
            </FieldGroup>
        </FieldSet>
    );
}
