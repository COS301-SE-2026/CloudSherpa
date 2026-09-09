"use client";
import { Input } from "@/components/atoms/input";
import { Label } from "@/components/atoms/label";
import { FieldSet, FieldLegend, FieldDescription, FieldGroup } from "@/components/atoms/field";
import { ChartWidgetConfig, ChartType, ChartColour } from "@/features/dashboard/types/widgets";
import { FormCountCircle } from "@/components/atoms/form-count-circle";
import Dropdown from "@/components/molecules/dropdown";

interface ChartFormDetailsProps {
    configuration: ChartWidgetConfig;
    setConfiguration: (config: ChartWidgetConfig) => void;
}

const CHART_TYPE_OPTIONS: { value: ChartType; label: string }[] = [
    { value: "line_chart", label: "Line Chart" },
    { value: "gauge_chart", label: "Gauge Chart" },
];

const CHART_COLOURS: ChartColour[] = ["chart-1", "chart-2", "chart-3", "chart-4", "chart-5"];

export default function ChartFormDetails({
    configuration,
    setConfiguration,
}: Readonly<ChartFormDetailsProps>) {
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
                    <div className="flex flex-row gap-3 pt-1">
                        {CHART_COLOURS.map((colour) => (
                            <button
                                key={colour}
                                type="button"
                                aria-label={`Select ${colour}`}
                                onClick={() =>
                                    setConfiguration({ ...configuration, chartColour: colour })
                                }
                                className={`h-8 w-8 rounded-full transition-all focus:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 ${
                                    configuration.chartColour === colour
                                        ? "ring-2 ring-primary ring-offset-2 ring-offset-primary-foreground scale-105"
                                        : "border-2 border-transparent hover:scale-110"
                                }`}
                                style={{ backgroundColor: `var(--${colour})` }}
                            />
                        ))}
                    </div>
                </div>
            </FieldGroup>
        </FieldSet>
    );
}
