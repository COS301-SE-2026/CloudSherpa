'use client';

import { useState } from 'react';
import { WidgetContainer } from './base/WidgetContainer';
import { LineChartWidget } from './charts/LineChart';
import { GaugeWidget } from './charts/GaugeChart';
import { WidgetConfig, WidgetConfigData } from './widgetConfig';
import { MetricType } from '@/types/metric';

interface ConfigurableWidgetProps{
    forInitialConfiguration: WidgetConfigData;

    availableResources: string[];
    availableMetricTypes: Record<string, MetricType[]>;
}

export function ConfigurableWidget({ 
    forInitialConfiguration, 

    availableResources, 
    availableMetricTypes 

}: ConfigurableWidgetProps){
    const [isConfigOpen, setIsConfigOpen] = useState(false);
    const [config, setConfig] = useState<WidgetConfigData>(forInitialConfiguration);

    const handleSave = (newConfig: WidgetConfigData) => {
        setConfig(newConfig);
    };

    const forRenderingChart = () => {
        const commonPropsForCharts = {
            resourceId: config.resourceId,
            title: config.forTitle,
            metricType: config.metricType as MetricType,
        };

        if(config.forWidgetType === 'gauge'){
            return <GaugeWidget {...commonPropsForCharts} />;
        }
        
        return <LineChartWidget {...commonPropsForCharts} />;
    };

    return(
        <>
            <WidgetContainer 
                forTitle={config.forTitle}
                className="h-full w-full"
                isResizable={false}
                onSettingsClick={() => setIsConfigOpen(true)}
                showConfig={true}
            >
                {forRenderingChart()}
            </WidgetContainer>

            <WidgetConfig
                isOpen={isConfigOpen}
                onClose={() => setIsConfigOpen(false)}
                onSave={handleSave}
                forExistingConfig={config}
                forAvailableResources={availableResources}
                forAvailableMetricTypes={availableMetricTypes}
            />
        </>
    );
}
