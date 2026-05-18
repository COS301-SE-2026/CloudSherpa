'use client';

import { useState, useEffect, useRef } from 'react';

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

    const handleSave = () => {
        onSave(forConfiguration);
        onClose();
    };

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

                    {/*this is to chnange bet. the line chart or the gauge chart*/}
                    <div>
                        <label className="block text-sm font-medium text-foreground mb-2">
                            Chart Type
                        </label>

                        <div className="flex gap-4">
                            <label className="flex items-center gap-2 cursor-pointer">
                                <input
                                    type="radio"
                                    value="line"
                                    checked={forConfiguration.forWidgetType === 'line'}
                                    onChange={(e) => setConfig({ ...forConfiguration, forWidgetType: e.target.value as 'line' | 'gauge' })}
                                    className="w-4 h-4 text-primary focus:ring-ring"
                                />
                                <span className="text-foreground">Line Chart</span>
                            </label>

                            <label className="flex items-center gap-2 cursor-pointer">
                                <input
                                    type="radio"
                                    value="gauge"
                                    checked={forConfiguration.forWidgetType === 'gauge'}
                                    onChange={(e) => setConfig({ ...forConfiguration, forWidgetType: e.target.value as 'line' | 'gauge' })}
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