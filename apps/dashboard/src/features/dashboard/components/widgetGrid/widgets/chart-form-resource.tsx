"use client";
import { MetricStore } from "@/features/dashboard/types/metric";
import { useMetricStore } from "@/features/dashboard/stores/metric-store";
import { useState, useEffect } from "react";
import { ChartWidgetConfig } from "@/features/dashboard/types/widgets";
import { Label } from "@/components/atoms/label";
import { FieldSet, FieldLegend, FieldDescription, FieldGroup } from "@/components/atoms/field";
import { FormCountCircle } from "@/components/atoms/form-count-circle";
import Dropdown from "@/components/molecules/dropdown";
import { getAwsAccountResources } from "@/lib/fetch/cloud-account-api";
import { CloudResource, ResourceStatus } from "@/lib/fetch/dto/cloud-resource";

interface ChartFormResourceProps {
    configuration: ChartWidgetConfig;
    setConfiguration: (config: ChartWidgetConfig) => void;
}

export default function ChartFormResource({
    configuration,
    setConfiguration,
}: Readonly<ChartFormResourceProps>) {
    const [activeResources, setActiveResources] = useState<CloudResource[]>([]);

    useEffect(() => {
        if (configuration.accountId) {
            getAwsAccountResources(configuration.accountId)
                .then((resources) => {
                    const activeOnly = resources.filter((r) => r.status === ResourceStatus.ACTIVE);
                    setActiveResources(activeOnly);
                })
                .catch(console.error);
        }
    }, [configuration.accountId]);

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
                    <Label>Resource Name</Label>
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
                                metricName: metricType,
                            });
                        }}
                        widthVariant="full"
                        placeholder="Select Resource"
                        emptyMessage="No resources found"
                    />
                </div>
                <div className="grid gap-2">
                    <Label>Metric Type</Label>
                    <Dropdown
                        value={configuration.metricName}
                        options={availableMetrics.map((type) => ({
                            value: type,
                            label: type,
                        }))}
                        onSelect={(currentValue) => {
                            setConfiguration({
                                ...configuration,
                                metricName: currentValue as string,
                            });
                        }}
                        disabled={!configuration.resourceId}
                        widthVariant="full"
                        placeholder="Select Metric"
                        emptyMessage="No metrics found"
                    />
                </div>
            </FieldGroup>
        </FieldSet>
    );
}
