"use client";
import { Input } from "@/components/atoms/input";
import { Label } from "@/components/atoms/label";
import { FieldSet, FieldLegend, FieldDescription, FieldGroup } from "@/components/atoms/field";
import { ChartWidgetConfig, ChartType } from "@/features/dashboard/types/widgets";
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
                        placeholder="Select chart..."
                    />
                </div>
            </FieldGroup>
        </FieldSet>
    );
}
