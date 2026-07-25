"use client";

import {
    FieldSet,
    FieldLegend,
    FieldDescription,
    FieldGroup,
    Field,
    FieldLabel,
} from "@/components/atoms/field";
import { Input } from "@/components/atoms/input";
import { FormCountCircle } from "@/components/atoms/form-count-circle";
import {
    Select,
    SelectContent,
    SelectGroup,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from "@/components/atoms/select";
import { ChartType, ChartWidgetConfig } from "@/features/dashboard/types/widgets";

interface ChartFormDetailsProps {
    readonly config: ChartWidgetConfig;
    readonly setConfig: (updater: (prev: ChartWidgetConfig) => ChartWidgetConfig) => void;
}

const CHART_TYPE_OPTIONS: { value: ChartType; label: string }[] = [
    { value: "line_chart", label: "Line Chart" },
    { value: "gauge_chart", label: "Gauge Chart" },
];

export function ChartFormDetails({ config, setConfig }: ChartFormDetailsProps) {
    return (
        <FieldSet>
            <div className="flex flex-row items-center gap-3">
                <FormCountCircle count={1} />
                <FieldLegend className="mb-0">Chart Details</FieldLegend>
            </div>
            <FieldDescription>
                Choose the title and visual format that will appear on the dashboard.
            </FieldDescription>
            <FieldGroup>
                <Field>
                    <FieldLabel>Card Title</FieldLabel>
                    <Input
                        placeholder="Enter widget title"
                        value={config.displayName || ""}
                        onChange={(e) => {
                            setConfig((prev) => ({ ...prev, displayName: e.target.value }));
                        }}
                    />
                </Field>

                <Field>
                    <FieldLabel>Chart Type</FieldLabel>
                    <Select
                        value={config.chartType}
                        onValueChange={(value: ChartType) => {
                            setConfig((prev) => ({ ...prev, chartType: value }));
                        }}
                    >
                        <SelectTrigger className="w-full">
                            <SelectValue placeholder="Select chart type..." />
                        </SelectTrigger>
                        <SelectContent>
                            <SelectGroup>
                                {CHART_TYPE_OPTIONS.map((opt) => (
                                    <SelectItem key={opt.value} value={opt.value}>
                                        {opt.label}
                                    </SelectItem>
                                ))}
                            </SelectGroup>
                        </SelectContent>
                    </Select>
                </Field>
            </FieldGroup>
        </FieldSet>
    );
}
