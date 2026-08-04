"use client";
import { useState } from "react";
import { Button } from "@/components/atoms/button";
import { Input } from "@/components/atoms/input";
import { Label } from "@/components/atoms/label";
import { Popover, PopoverContent, PopoverTrigger } from "@/components/atoms/popover";
import {
    Command,
    CommandEmpty,
    CommandGroup,
    CommandInput,
    CommandItem,
    CommandList,
} from "@/components/atoms/command";
import {
    FieldSet,
    FieldLegend,
    FieldDescription,
    FieldGroup,
    Field,
    FieldLabel,
} from "@/components/atoms/field";
import { Check, ChevronsUpDown } from "lucide-react";
import { cn } from "@/lib/utils";
import { ChartWidgetConfig } from "@/features/dashboard/types/widgets";
import { ChartType } from "@/features/dashboard/types/widgets";
import { FormCountCircle } from "@/components/atoms/form-count-circle";

interface ChartFormDetailsProps {
    configuration: ChartWidgetConfig;
    setConfiguration: (config: ChartWidgetConfig) => void;
}

const CHART_TYPE_OPTIONS: { value: ChartType; label: string }[] = [
    { value: "line_chart", label: "Line Chart" },
    { value: "gauge_chart", label: "Gauge Chart" },
];

export default function ChartFormDetails({
    configuration,
    setConfiguration,
}: Readonly<ChartFormDetailsProps>) {
    const [chartOpen, setChartOpen] = useState(false);

    return (
        <FieldSet>
            <div className="flex flex-row items-center gap-3">
                <FormCountCircle count={1} />
                <FieldLegend className="mb-0">KPI Details</FieldLegend>
            </div>
            <FieldDescription>
                Choose the title that will appear on the dashboard card.
            </FieldDescription>
            <FieldGroup>
                <div className="grid gap-2">
                    <Label htmlFor="title">Title</Label>
                    <Input
                        id="title"
                        value={configuration.displayName || ""}
                        onChange={(e) =>
                            setConfiguration({ ...configuration, displayName: e.target.value })
                        }
                        placeholder="Widget title"
                    />
                </div>
                <div className="flex flex-col gap-2">
                    <Label>Chart Type</Label>
                    <Popover open={chartOpen} onOpenChange={setChartOpen}>
                        <PopoverTrigger asChild>
                            <Button
                                variant="outline"
                                role="combobox"
                                aria-expanded={chartOpen}
                                className="justify-between w-full"
                            >
                                {configuration.chartType
                                    ? CHART_TYPE_OPTIONS.find(
                                          (opt) => opt.value === configuration.chartType
                                      )?.label
                                    : "Select chart type..."}
                                <ChevronsUpDown className="ml-2 h-4 w-4 shrink-0 opacity-50" />
                            </Button>
                        </PopoverTrigger>
                        <PopoverContent className="p-0 w-(--radix-popover-trigger-width)">
                            <Command>
                                <CommandInput placeholder="Search chart types..." />
                                <CommandList>
                                    <CommandEmpty>No chart type found.</CommandEmpty>
                                    <CommandGroup>
                                        {CHART_TYPE_OPTIONS.map((opt) => (
                                            <CommandItem
                                                key={opt.value}
                                                value={opt.value}
                                                onSelect={(currentValue) => {
                                                    setConfiguration({
                                                        ...configuration,
                                                        chartType: currentValue as ChartType,
                                                    });
                                                    setChartOpen(false);
                                                }}
                                            >
                                                <Check
                                                    className={cn(
                                                        "mr-2 h-4 w-4",
                                                        configuration.chartType === opt.value
                                                            ? "opacity-100"
                                                            : "opacity-0"
                                                    )}
                                                />
                                                {opt.label}
                                            </CommandItem>
                                        ))}
                                    </CommandGroup>
                                </CommandList>
                            </Command>
                        </PopoverContent>
                    </Popover>
                </div>
            </FieldGroup>
        </FieldSet>
    );
}
