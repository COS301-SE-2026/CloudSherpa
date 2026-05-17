'use client';

import { useState, useEffect } from 'react';

interface WidgetConfigProps{
    isOpen: boolean;

    onClose: () => void;
    onSave: (config: WidgetConfigData) => void;

    forExistingConfig: WidgetConfigData;

    forAvailableResources: string[];
    forAvailableMetricTypes: string[];
}

export interface WidgetConfigData{
    forTitle: string;
    resourceId: string;

    metricType: string;
    forWidgetType: 'line' | 'gauge';
}

export function WidgetConfig({ 
    isOpen, 

    onClose, 
    onSave, 

    forExistingConfig,
    forAvailableResources,
    forAvailableMetricTypes

}: WidgetConfigProps){
    const [forConfiguration, setConfig] = useState<WidgetConfigData>(forExistingConfig);

    const forFirstRender = useRef(true);
    useEffect(() => {
        if(!forFirstRender.current){
            setConfig(forExistingConfig);
        }

        forFirstRender.current = false;
    }, [forExistingConfig]);

    if(!isOpen){
        return null;
    }

    return (
        <>
            <div 
                className="fixed inset-0 bg-black/50 z-40"
                onClick={onClose}
            />
            
            <div className="fixed top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2 w-full max-w-md bg-card rounded-xl border border-border shadow-xl z-50">
                <div className="flex items-center justify-between border-b border-border px-6 py-4">
                    <h3 className="text-lg font-semibold text-foreground">Widget Configuration</h3>
                </div>

                <div className="p-6 space-y-4">
                    {/*this is for the widget title*/}
                    <div>
                        <label className="block text-sm font-medium text-foreground mb-2">
                            Title
                        </label>

                        <input
                            type="text"
                            value={forConfiguration.forTitle}
                            onChange={(e) => setConfig({ ...forConfiguration, forTitle: e.target.value })}
                            className="w-full bg-background border border-border rounded-lg px-4 py-2 text-foreground focus:outline-none focus:ring-2 focus:ring-ring"
                            placeholder="Enter widget title"
                        />
                    </div>

                    {/*this is for the resource id*/}
                    <div>
                        <label className="block text-sm font-medium text-foreground mb-2">
                            Resource ID
                        </label>

                        <select
                            value={forConfiguration.resourceId}
                            onChange={(e) => setConfig({ ...forConfiguration, resourceId: e.target.value })}
                            className="w-full bg-background border border-border rounded-lg px-4 py-2 text-foreground focus:outline-none focus:ring-2 focus:ring-ring"
                        >
                            {forAvailableResources.map((resource) => (
                                <option key={resource} value={resource}>
                                    {resource}
                                </option>
                            ))}
                        </select>
                    </div>

                    {/*this is for the metric type*/}
                    <div>
                        <label className="block text-sm font-medium text-foreground mb-2">
                            Metric Type
                        </label>

                        <select
                            value={forConfiguration.metricType}
                            onChange={(e) => setConfig({ ...forConfiguration, metricType: e.target.value })}
                            className="w-full bg-background border border-border rounded-lg px-4 py-2 text-foreground focus:outline-none focus:ring-2 focus:ring-ring"
                        >
                            {forAvailableMetricTypes.map((type) => (
                                <option key={type} value={type}>
                                    {type.toUpperCase()}
                                </option>
                            ))}
                        </select>
                    </div>
                </div>
            </div>
        </>
    );
}