"use client";

import { useDashboardStore } from "@/features/dashboard/stores/dashboard-store";
import {
    ResourceNameStore,
    useResourceNameStore,
} from "@/features/dashboard/stores/resource-store";
import { MetricType, MetricStore } from "@/features/dashboard/types/metric";
import { useMetricStore } from "@/features/dashboard/stores/metric-store";
import { useState } from "react";
import { ChartType, ChartWidgetConfig } from "@/features/dashboard/types/widgets";
import { Check, ChevronsUpDown } from "lucide-react";
import { cn } from "@/lib/utils";

import {
    Dialog,
    DialogContent,
    DialogHeader,
    DialogTitle,
    DialogFooter,
} from "@/components/atoms/dialog";
import { Input } from "@/components/atoms/input";
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

interface WidgetConfigMenuProps {
    isOpen: boolean;
    existingConfig: ChartWidgetConfig;
}

const CHART_TYPE_OPTIONS: { value: ChartType; label: string }[] = [
    { value: "line_chart", label: "Line Chart" },
    { value: "gauge_chart", label: "Gauge Chart" },
];

//helper function fr nested logic
function getMetricDisplayText(value: MetricType | null, resourceId: string | null) {
    if (value) {
        return value.toUpperCase();
    }
    if (resourceId) {
        return "Select metric type...";
    }
    return "Select resource first";
}

export function WidgetConfigMenu({ isOpen, existingConfig }: Readonly<WidgetConfigMenuProps>) {
    // draft state
    const [configuration, setConfiguration] = useState<ChartWidgetConfig>(existingConfig);
    const [isSaving, setIsSaving] = useState(false);

    //dropdown states
    const [resourceOpen, setResourceOpen] = useState(false);
    const [metricOpen, setMetricOpen] = useState(false);
    const [chartOpen, setChartOpen] = useState(false);

    const resourceNamesById = useResourceNameStore(
        (state: ResourceNameStore) => state.resourcesById
    );
    const resources = useResourceNameStore((state: ResourceNameStore) => state.resources);

    const allAvailableMetrics = useMetricStore((state: MetricStore) => state.getMetricList);

    const availableMetrics = configuration.resourceId
        ? (allAvailableMetrics()[configuration.resourceId] ?? [])
        : [];
    const metricsByResource = allAvailableMetrics();
    const metricResourceIds = Object.keys(metricsByResource);
    const availableResources =
        metricResourceIds.length > 0
            ? metricResourceIds
            : resources.map((resource) => resource.resourceId);

    const [prevIsOpen, setPrevIsOpen] = useState(isOpen);
    const [prevConfig, setPrevConfig] = useState(existingConfig);

    if (isOpen !== prevIsOpen || existingConfig !== prevConfig) {
        setPrevIsOpen(isOpen);
        setPrevConfig(existingConfig);

        if (isOpen) {
            setConfiguration(existingConfig);
            setResourceOpen(false);
            setMetricOpen(false);
            setChartOpen(false);
        }
    }

    const updateChartWidget = useDashboardStore((state) => state.actions.updateChartWidgetConfig);

    const handleSave = async () => {
        setIsSaving(true);
        try {
            updateChartWidget(configuration);
        } catch (error) {
            console.error("Failed to save configuration", error);
        } finally {
            setIsSaving(false);
        }
    };

    if (!isOpen) {
        return null;
    }

    return (
        <Dialog open={isOpen} onOpenChange={(open) => !open}>
            <DialogContent className="sm:max-w-106.25">
                <DialogHeader>
                    <DialogTitle>Widget Configuration</DialogTitle>
                </DialogHeader>

                <div className="grid gap-4 py-4">
                    <div className="grid gap-2">
                        <Label htmlFor="title">Title</Label>
                        <Input
                            id="title"
                            value={configuration.displayName || ""}
                            onChange={(e) =>
                                setConfiguration({ ...configuration, displayName: e.target.value })
                            }
                            placeholder="Enter widget title"
                            disabled={isSaving}
                        />
                    </div>

                    <div className="grid gap-2">
                        <Label>Resource ID</Label>
                        <Popover open={resourceOpen} onOpenChange={setResourceOpen}>
                            <PopoverTrigger asChild>
                                <Button
                                    variant="outline"
                                    role="combobox"
                                    aria-expanded={resourceOpen}
                                    className="justify-between w-full"
                                    disabled={isSaving}
                                >
                                    <span className="truncate">
                                        {configuration.resourceId
                                            ? (resourceNamesById[configuration.resourceId] ??
                                              configuration.resourceId)
                                            : "Select a resource..."}
                                    </span>
                                    <ChevronsUpDown className="ml-2 h-4 w-4 shrink-0 opacity-50" />
                                </Button>
                            </PopoverTrigger>
                            <PopoverContent className="p-0 w-(--radix-popover-trigger-width)">
                                <Command>
                                    <CommandInput placeholder="Search resources..." />
                                    <CommandList>
                                        <CommandEmpty>No resource found</CommandEmpty>
                                        <CommandGroup>
                                            {availableResources.map((resource) => (
                                                <CommandItem
                                                    key={resource}
                                                    value={resource}
                                                    onSelect={(currentValue) => {
                                                        const nextMetricOptions =
                                                            allAvailableMetrics()[currentValue] ??
                                                            [];
                                                        const metricType =
                                                            nextMetricOptions[0] ?? "anon";
                                                        setConfiguration({
                                                            ...configuration,
                                                            resourceId: currentValue,
                                                            metricType: metricType,
                                                        });
                                                        setResourceOpen(false);
                                                    }}
                                                >
                                                    <Check
                                                        className={cn(
                                                            "mr-2 h-4 w-4",
                                                            configuration.resourceId === resource
                                                                ? "opacity-100"
                                                                : "opacity-0"
                                                        )}
                                                    />
                                                    {resourceNamesById[resource] ?? resource}
                                                </CommandItem>
                                            ))}
                                        </CommandGroup>
                                    </CommandList>
                                </Command>
                            </PopoverContent>
                        </Popover>
                    </div>

                    <div className="grid gap-2">
                        <Label>Metric Type</Label>
                        <Popover open={metricOpen} onOpenChange={setMetricOpen}>
                            <PopoverTrigger asChild>
                                <Button
                                    variant="outline"
                                    role="combobox"
                                    aria-expanded={metricOpen}
                                    className="justify-between w-full"
                                    disabled={!configuration.resourceId || isSaving}
                                >
                                    <span className="truncate">
                                        {getMetricDisplayText(
                                            configuration.metricType,
                                            configuration.resourceId
                                        )}
                                    </span>
                                    <ChevronsUpDown className="ml-2 h-4 w-4 shrink-0 opacity-50" />
                                </Button>
                            </PopoverTrigger>
                            <PopoverContent className="p-0 w-(--radix-popover-trigger-width)">
                                <Command>
                                    <CommandInput placeholder="Search metric types..." />
                                    <CommandList>
                                        <CommandEmpty>No metric type found.</CommandEmpty>
                                        <CommandGroup>
                                            {availableMetrics.map((type) => (
                                                <CommandItem
                                                    key={type}
                                                    value={type}
                                                    onSelect={(currentValue) => {
                                                        setConfiguration({
                                                            ...configuration,
                                                            metricType: currentValue as MetricType,
                                                        });
                                                        setMetricOpen(false);
                                                    }}
                                                >
                                                    <Check
                                                        className={cn(
                                                            "mr-2 h-4 w-4",
                                                            configuration.metricType === type
                                                                ? "opacity-100"
                                                                : "opacity-0"
                                                        )}
                                                    />
                                                    {type.toUpperCase()}
                                                </CommandItem>
                                            ))}
                                        </CommandGroup>
                                    </CommandList>
                                </Command>
                            </PopoverContent>
                        </Popover>
                    </div>

                    <div className="grid gap-2">
                        <Label>Chart Type</Label>
                        <Popover open={chartOpen} onOpenChange={setChartOpen}>
                            <PopoverTrigger asChild>
                                <Button
                                    variant="outline"
                                    role="combobox"
                                    aria-expanded={chartOpen}
                                    className="justify-between w-full"
                                    disabled={isSaving}
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
                </div>

                <DialogFooter>
                    <Button variant="outline" disabled={isSaving}>
                        Cancel
                    </Button>
                    <Button
                        onClick={handleSave}
                        disabled={isSaving}
                        aria-label="save changes button"
                    >
                        {isSaving ? "Saving..." : "Save Changes"}
                    </Button>
                </DialogFooter>
            </DialogContent>
        </Dialog>
    );
}
