"use client";
import { Input } from "@/components/atoms/input";
import { Label } from "@/components/atoms/label";
import { FieldSet, FieldLegend, FieldDescription, FieldGroup } from "@/components/atoms/field";
import { ChartWidgetConfig, ChartType, ChartColour } from "@/features/dashboard/types/widgets";
import { FormCountCircle } from "@/components/atoms/form-count-circle";
import { Button } from "@/components/atoms/button";
import { ChevronDown } from "lucide-react";
import Dropdown from "@/components/molecules/dropdown";
import { Popover, PopoverContent, PopoverTrigger } from "@/components/atoms/popover";
import { cn } from "@/lib/utils";

interface ChartFormDetailsProps {
    configuration: ChartWidgetConfig;
    setConfiguration: (config: ChartWidgetConfig) => void;
}

const CHART_TYPE_OPTIONS: { value: ChartType; label: string }[] = [
    { value: "line_chart", label: "Line Chart" },
    { value: "gauge_chart", label: "Gauge Chart" },
];

const CHART_COLOURS: { label: string; value: ChartColour }[] = [
    { label: "Blue", value: "chart_1" },
    { label: "Purple", value: "chart_2" },
    { label: "Green", value: "chart_3" },
    { label: "Orange", value: "chart_4" },
    { label: "Pink", value: "chart_5" },
];

export default function ChartFormDetails({
    configuration,
    setConfiguration,
}: Readonly<ChartFormDetailsProps>) {
    const currentColour = configuration.chartColour || "chart_1";
    const currentColourLabel =
        CHART_COLOURS.find((c) => c.value === currentColour)?.label || "Blue";

    return (
        <FieldSet>
            <div className="flex flex-row items-center gap-3">
                <FormCountCircle count={1} />
                <FieldLegend className="mb-0">Chart Details</FieldLegend>
            </div>
            <FieldDescription>
                Choose the widget title that will appear on the widget as well as the chart type and
                colour.
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
                    <Dropdown
                        value={configuration.chartType}
                        options={CHART_TYPE_OPTIONS.map((opt) => ({
                            value: opt.value,
                            label: opt.label,
                        }))}
                        onSelect={(currentValue) => {
                            setConfiguration({
                                ...configuration,
                                chartType: currentValue as ChartType,
                            });
                        }}
                        widthVariant="full"
                        placeholder="Select Chart"
                    />
                </div>
                <div className="flex flex-col gap-2">
                    <Label>Chart Colour</Label>
                    <Popover>
                        <PopoverTrigger asChild>
                            <Button
                                type="button"
                                variant="outline"
                                className="w-fit group justify-between gap-3"
                            >
                                <div className="flex items-center gap-2">
                                    <div
                                        className="h-4 w-4 rounded-sm border border-border shadow-sm"
                                        style={{
                                            backgroundColor: `var(--${currentColour.replace("_", "-")})`,
                                        }}
                                    />
                                    <span className="capitalize">{currentColourLabel}</span>
                                </div>
                                <ChevronDown className="h-4 w-4 opacity-50 transition-transform duration-200 group-data-[state=open]:rotate-180" />
                            </Button>
                        </PopoverTrigger>

                        <PopoverContent className="w-max p-2" align="start">
                            <div className="grid grid-cols-5 gap-2">
                                {CHART_COLOURS.map((colourObj) => (
                                    <button
                                        key={colourObj.value}
                                        type="button"
                                        aria-label={`Select ${colourObj.label}`}
                                        title={colourObj.label}
                                        onClick={() =>
                                            setConfiguration({
                                                ...configuration,
                                                chartColour: colourObj.value,
                                            })
                                        }
                                        className={cn(
                                            "h-6 w-6 rounded-sm transition-all focus:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-1 cursor-pointer",
                                            currentColour === colourObj.value
                                                ? "ring-2 ring-primary ring-offset-1 ring-offset-background scale-110 z-10"
                                                : "border border-border/50 hover:border-foreground hover:scale-110"
                                        )}
                                        style={{
                                            backgroundColor: `var(--${colourObj.value.replace("_", "-")})`,
                                        }}
                                    />
                                ))}
                            </div>
                        </PopoverContent>
                    </Popover>
                </div>
            </FieldGroup>
        </FieldSet>
    );
}
