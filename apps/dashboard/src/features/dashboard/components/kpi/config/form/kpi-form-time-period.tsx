"use client";

import { Button } from "@/components/atoms/button";
import { Calendar } from "@/components/atoms/calendar";
import {
    FieldDescription,
    FieldGroup,
    FieldLabel,
    FieldLegend,
    FieldSet,
} from "@/components/atoms/field";
import { FormCountCircle } from "@/components/atoms/form-count-circle";
import { Popover, PopoverContent, PopoverTrigger } from "@/components/atoms/popover";
import {
    Select,
    SelectContent,
    SelectGroup,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from "@/components/atoms/select";
import { format } from "date-fns";
import { ChevronDownIcon } from "lucide-react";
import type { Dispatch, SetStateAction } from "react";

const costAggregateQuickSelectOptions = [
    { label: "7 days", value: "7" },
    { label: "14 days", value: "14" },
    { label: "30 days", value: "30" },
    { label: "60 days", value: "60" },
    { label: "90 days", value: "90" },
    { label: "6 months", value: "180" },
    { label: "12 months", value: "365" },
];

interface KpiFormTimePeriodProps {
    readonly startDate: Date | undefined;
    readonly setStartDate: Dispatch<SetStateAction<Date | undefined>>;
    readonly endDate: Date | undefined;
    readonly setEndDate: Dispatch<SetStateAction<Date | undefined>>;
}

export function KpiFormTimePeriod({
    startDate,
    setStartDate,
    endDate,
    setEndDate,
}: KpiFormTimePeriodProps) {
    return (
        <FieldSet>
            <div className="flex flex-row items-center gap-3">
                <FormCountCircle count={3} />
                <FieldLegend className="mb-0">Time Period</FieldLegend>
            </div>

            <FieldDescription>
                Choose the time period over which the costs will be aggregated
            </FieldDescription>
            <FieldGroup>
                <div className="grid grid-cols-[1fr_2fr] gap-6">
                    <div>
                        <FieldLabel>Quick Select</FieldLabel>
                        <Select defaultValue="30">
                            <SelectTrigger>
                                <SelectValue />
                            </SelectTrigger>
                            <SelectContent>
                                <SelectGroup>
                                    {costAggregateQuickSelectOptions.map((option) => (
                                        <SelectItem key={option.value} value={option.value}>
                                            {option.label}
                                        </SelectItem>
                                    ))}
                                </SelectGroup>
                            </SelectContent>
                        </Select>
                    </div>
                    <div>
                        <FieldLabel>Custom Range</FieldLabel>
                        <div className="flex flex-row items-center gap-4">
                            <Popover>
                                <PopoverTrigger asChild>
                                    <Button
                                        variant="outline"
                                        data-empty={!startDate}
                                        className="w-[212px] justify-between text-left font-normal data-[empty=true]:text-muted-foreground"
                                    >
                                        {startDate ? (
                                            format(startDate, "PPP")
                                        ) : (
                                            <span>Pick a start date</span>
                                        )}
                                        <ChevronDownIcon data-icon="inline-end" />
                                    </Button>
                                </PopoverTrigger>

                                <PopoverContent className="w-auto p-0" align="start">
                                    <Calendar
                                        mode="single"
                                        selected={startDate}
                                        onSelect={setStartDate}
                                        defaultMonth={startDate}
                                    />
                                </PopoverContent>
                            </Popover>
                            -
                            <Popover>
                                <PopoverTrigger asChild>
                                    <Button
                                        variant="outline"
                                        data-empty={!endDate}
                                        className="w-[212px] justify-between text-left font-normal data-[empty=true]:text-muted-foreground"
                                    >
                                        {endDate ? (
                                            format(endDate, "PPP")
                                        ) : (
                                            <span>Pick an end date</span>
                                        )}
                                        <ChevronDownIcon data-icon="inline-end" />
                                    </Button>
                                </PopoverTrigger>

                                <PopoverContent className="w-auto p-0" align="start">
                                    <Calendar
                                        mode="single"
                                        selected={endDate}
                                        onSelect={setEndDate}
                                        defaultMonth={endDate}
                                    />
                                </PopoverContent>
                            </Popover>
                        </div>
                    </div>
                </div>
            </FieldGroup>
        </FieldSet>
    );
}
