"use client";
import { MetricType, MetricStore } from "@/features/dashboard/types/metric";
import { useMetricStore } from "@/features/dashboard/stores/metric-store";
import { useState, useEffect } from "react";
import { ChartWidgetConfig } from "@/features/dashboard/types/widgets";
import { Check, ChevronsUpDown } from "lucide-react";
import { cn } from "@/lib/utils";
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
import {
    getAwsAccountResources,
    CloudResource,
    ResourceStatus,
} from "@/lib/fetch/aws-connection-api";
import Dropdown from "@/components/molecules/dropdown";

function getMetricDisplayText(value: MetricType | null, resourceId: string | null) {
    if (value) {
        return value.toUpperCase();
    }
    if (resourceId) {
        return "Select metric type...";
    }
    return "Select resource first";
}

interface ChartFormResourceProps {
    configuration: ChartWidgetConfig;
    setConfiguration: (config: ChartWidgetConfig) => void;
    selectedConnectionId: string | null;
}

export default function ChartFormResource({
    configuration,
    setConfiguration,
    selectedConnectionId,
}: Readonly<ChartFormResourceProps>) {
    const [activeResources, setActiveResources] = useState<CloudResource[]>([]);

    useEffect(() => {
        if (selectedConnectionId) {
            getAwsAccountResources(selectedConnectionId)
                .then((resources) => {
                    const activeOnly = resources.filter((r) => r.status === ResourceStatus.ACTIVE);
                    setActiveResources(activeOnly);
                })
                .catch(console.error);
        }
    }, [selectedConnectionId]);

    const allAvailableMetrics = useMetricStore((state: MetricStore) => state.getMetricList);

    const availableMetrics = configuration.resourceId
        ? (allAvailableMetrics()[configuration.resourceId] ?? [])
        : [];

    return (
        <FieldSet>
            <div className="flex flex-row items-center gap-3">
                <FormCountCircle count={3} />
                <FieldLegend className="mb-0">Resource</FieldLegend>
            </div>
            <FieldDescription>Choose the resource for the chart.</FieldDescription>
            <FieldGroup>
                <div className="grid gap-2">
                    <Label>Resource ID</Label>
                    <Dropdown
                        value={configuration.resourceId}
                        options={activeResources.map((resource) => ({
                            value: resource.id,
                            label: resource.resourceName,
                        }))}
                        onSelect={(currentValue) => {
                            const nextMetricOptions = allAvailableMetrics()[currentValue] ?? [];
                            const metricType = nextMetricOptions[0] ?? "anon";
                            setConfiguration({
                                ...configuration,
                                resourceId: currentValue,
                                metricType: metricType,
                            });
                        }}
                        widthVariant="full"
                        placeholder="select resource..."
                    />
                </div>
                <div className="grid gap-2">
                    <Label>Metric Type</Label>
                    <Dropdown
                        value={configuration.metricType}
                        options={availableMetrics.map((type) => ({
                            value: type,
                            label: type.toUpperCase(),
                        }))}
                        onSelect={(currentValue) => {
                            setConfiguration({
                                ...configuration,
                                metricType: currentValue as MetricType,
                            });
                        }}
                        disabled={!configuration.resourceId}
                        widthVariant="full"
                        placeholder="select metric..."
                    />
                </div>
            </FieldGroup>
        </FieldSet>
    );
}
