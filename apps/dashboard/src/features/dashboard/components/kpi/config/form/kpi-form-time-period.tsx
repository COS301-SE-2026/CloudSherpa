"use client";

import {
    FieldDescription,
    FieldGroup,
    FieldLabel,
    FieldLegend,
    FieldSet,
} from "@/components/atoms/field";
import { FormCountCircle } from "@/components/atoms/form-count-circle";
import {
    Select,
    SelectContent,
    SelectGroup,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from "@/components/atoms/select";

const costAggregateQuickSelectOptions = [
    { label: "7 days", value: 7 },
    { label: "14 days", value: 14 },
    { label: "30 days", value: 30 },
    { label: "60 days", value: 60 },
    { label: "90 days", value: 90 },
    { label: "6 months", value: 180 },
    { label: "12 months", value: 365 },
];

interface KpiFormTimePeriodProps {
    readonly aggregationWindowDays: number;
    readonly onAggregationWindowChange: (newWindow: number) => void;
}

export function KpiFormTimePeriod({
    aggregationWindowDays,
    onAggregationWindowChange,
}: KpiFormTimePeriodProps) {
    return (
        <FieldSet>
            <div className="flex flex-row items-center gap-3">
                <FormCountCircle count={3} />
                <FieldLegend className="mb-0">Time Period</FieldLegend>
            </div>

            <FieldGroup>
                <FieldDescription>
                    Choose the time period over which the costs will be aggregated
                </FieldDescription>
                <div className="flex flex-col gap-1">
                    <FieldLabel>Supported Intervals</FieldLabel>
                    <Select
                        value={String(aggregationWindowDays)}
                        onValueChange={(value) => onAggregationWindowChange(Number(value))}
                    >
                        <SelectTrigger className="w-60">
                            <SelectValue />
                        </SelectTrigger>
                        <SelectContent>
                            <SelectGroup>
                                {costAggregateQuickSelectOptions.map((option) => (
                                    <SelectItem key={option.value} value={String(option.value)}>
                                        {option.label}
                                    </SelectItem>
                                ))}
                            </SelectGroup>
                        </SelectContent>
                    </Select>
                </div>
            </FieldGroup>
        </FieldSet>
    );
}
