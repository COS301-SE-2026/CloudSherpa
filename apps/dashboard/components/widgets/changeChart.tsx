'use client';

import { useState } from 'react';
import { WidgetContainer } from './base/WidgetContainer';
import { LineChartWidget } from './charts/LineChart';
import { GaugeWidget } from './charts/GaugeChart';
import { WidgetConfig, WidgetConfigData } from './widgetConfig';

interface ConfigurableWidgetProps{
    forInitialConfiguration: WidgetConfigData;

    availableResources: string[];
    availableMetricTypes: string[];
}

type MetricType = 'cpu'| 'memory' | 'disk' | 'cost' | 'anon';

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
                defaultWidth={config.forWidgetType === 'line' ? 700 : 400}
                defaultHeight={config.forWidgetType === 'line' ? 400 : 350}
                minWidth={300}
                minHeight={250}
                isResizable={true}
                onSettingsClick={() => setIsConfigOpen(true)}
                showConfig={true}
            >
                {forRenderingChart()}
            </WidgetContainer>
        </>
    );
}