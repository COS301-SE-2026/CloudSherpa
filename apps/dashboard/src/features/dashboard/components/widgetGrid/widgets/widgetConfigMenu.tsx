'use client';

import { useDashboardStore, DashboardStore } from '@/features/dashboard/stores/dashboard-store';
import { useResourceNameStore, ResourceNameStore } from '@/features/dashboard/stores/resource-store';
import { MetricType, MetricStore } from '@/features/dashboard/types/metric';
import { useMetricStore } from '@/features/dashboard/stores/metric-store';
import { useState, useEffect, useRef } from 'react';
import { WidgetConfig } from '@/features/dashboard/types/widgets';

interface WidgetConfigMenuProps{
    isOpen: boolean;

    onClose: () => void;
    onSave: (config: WidgetConfig) => void;

    existingConfig: WidgetConfig;
}

// This is shared (used for dashboard store), perhaps we can move it to types
// Also need to confirm that interface is more appropriate than type
export function WidgetConfigMenu({ 
    isOpen, 

    onClose, 
    onSave, 

    existingConfig,

}: Readonly<WidgetConfigMenuProps>) {
    const [configuration, setConfiguration] = useState<WidgetConfig>(existingConfig);
    const registerWidgetConfigUpdate = useDashboardStore(
        (state: DashboardStore) => state.actions.updateWidgetConfig
        );
        const resourceNamesById = useResourceNameStore(
        (state: ResourceNameStore) => state.resourcesById
        );
        const allAvailableMetrics = useMetricStore(
        (state: MetricStore) => state.getMetricList
        );

    const availableMetrics = configuration.resourceId
    ? allAvailableMetrics()[configuration.resourceId] ?? []
    : [];
    const metricsByResource = allAvailableMetrics();
    const metricResourceIds = Object.keys(metricsByResource);
    const availableResources =
    metricResourceIds.length > 0 ? metricResourceIds : Object.keys(resourceNamesById);

    const isFirstRender = useRef(true);

    useEffect(() => {
        if(!isFirstRender.current){
            setConfiguration(existingConfig);
        }

        isFirstRender.current = false;
    }, [existingConfig]);

    const handleSave = () => {
        onSave(configuration);
        onClose();
    };

    function setConfigAndRegisterUpdate(newConfig: WidgetConfig) {
        setConfiguration(newConfig);
        registerWidgetConfigUpdate(newConfig);
    }

    if(!isOpen){
        return null;
    }

    return (
        <>
            <button
                className="fixed inset-0 z-40"
                onClick={onClose}
            />
            
            <div className="fixed top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2 w-full max-w-md bg-card rounded-xl border border-border shadow-xl z-50">
                <div className="flex items-center justify-between border-b border-border px-6 py-4">
                    <h3 className="text-lg font-semibold text-foreground">Widget Configuration</h3>
                </div>

                <div className="p-6 space-y-4">
                    {/*this is for the widget title*/}
                    <div>
                        <label className="block text-sm font-medium text-foreground mb-2" htmlFor="title">
                            Title
                        </label>

                        <input
                            id="title"
                            type="text"
                            value={configuration.title}
                            onChange={(e) => setConfigAndRegisterUpdate({ ...configuration, title: e.target.value })}
                            className="w-full bg-background border border-border rounded-lg px-4 py-2 text-foreground focus:outline-none focus:ring-2 focus:ring-ring"
                            placeholder="Enter widget title"
                        />
                    </div>

                    {/*this is for the resource id*/}
                    <div>
                        <label className="block text-sm font-medium text-foreground mb-2" htmlFor="resource-id">
                            Resource ID
                        </label>

                        <select
                            id="resource-id"
                            value={configuration.resourceId || ""}
                            onChange={(e) => {
                                const resourceId = e.target.value;
                                const nextMetricOptions = allAvailableMetrics()[resourceId] ?? [];
                                const metricType = nextMetricOptions[0] ?? "anon";

                                setConfigAndRegisterUpdate({ ...configuration, resourceId, metricType });
                            }}
                            className="w-full bg-background border border-border rounded-lg px-4 py-2 text-foreground focus:outline-none focus:ring-2 focus:ring-ring"
                            >
                            <option value="" disabled>Select a resource</option>
                            {availableResources.map((resource) => (
                                <option key={resource} value={resource}>
                                    {resourceNamesById[resource] ?? resource}
                                </option>
                            ))}
                            </select>
                    </div>

                    {/*this is for the metric type*/}
                    <div>
                        {configuration.resourceId ? (
                            <>
                                <label className="block text-sm font-medium text-foreground mb-2" htmlFor="metric-type">
                                    Metric Type
                                </label>

                                <select
                                    id="metric-type"
                                    value={configuration.metricType}
                                    onChange={(e) => setConfigAndRegisterUpdate({ ...configuration, metricType: e.target.value as MetricType })}
                                    className="w-full bg-background border border-border rounded-lg px-4 py-2 text-foreground focus:outline-none focus:ring-2 focus:ring-ring"
                                >
                                    {availableMetrics.map((type: MetricType) => (
                                        <option key={type} value={type}>
                                            {type.toUpperCase()}
                                        </option>
                                    ))}
                                </select>
                            </>
                        ) : (
                            <p className="text-sm text-muted-foreground">
                                Select resource first
                            </p>
                        )}
                    </div>

                    {/*this is to chnange bet. the line chart or the gauge chart*/}
                    <div>
                        <label className="block text-sm font-medium text-foreground mb-2" htmlFor="chart-type">
                            Chart Type
                        </label>

                        <div className="flex gap-4" id="chart-type">
                            <label className="flex items-center gap-2 cursor-pointer">
                                <input
                                    type="radio"
                                    value="line"
                                    checked={configuration.chartType === 'line'}
                                    onChange={(e) => setConfiguration({ ...configuration, chartType: e.target.value as 'line' | 'gauge' })}
                                    className="w-4 h-4 text-primary focus:ring-ring"
                                />
                                <span className="text-foreground">Line Chart</span>
                            </label>

                            <label className="flex items-center gap-2 cursor-pointer ">
                                <input
                                    type="radio"
                                    value="gauge"
                                    checked={configuration.chartType === 'gauge'}
                                    onChange={(e) => setConfiguration({ ...configuration, chartType: e.target.value as 'line' | 'gauge' })}
                                    className="w-4 h-4 text-primary focus:ring-ring"
                                />
                                <span className="text-foreground">Gauge Chart</span>
                            </label>

                        </div>
                    </div>
                </div>

                <div className="flex justify-end gap-3 border-t border-border px-6 py-4">
                    <button
                        onClick={onClose}
                        className="px-4 py-2 rounded-lg border border-border text-foreground hover:bg-muted transition-colors"
                    >
                        Cancel
                    </button>

                    <button
                        onClick={handleSave}
                        className="px-4 py-2 rounded-lg bg-primary text-primary-foreground hover:bg-primary/90 transition-colors"
                    >
                        Save Changes
                    </button>

                </div>
            </div>
        </>
    );
}
