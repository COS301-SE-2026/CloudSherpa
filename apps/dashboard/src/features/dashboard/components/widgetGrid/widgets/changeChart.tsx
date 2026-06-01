'use client';

import { useState } from 'react';
import { WidgetContainer } from '@/features/dashboard/components/widgetGrid/widgets/base/WidgetContainer'; 
import { LineChartWidget } from './charts/LineChart';
import { GaugeWidget } from './charts/GaugeChart';
import { WidgetConfig, WidgetConfigData } from './widgetConfig';

interface ConfigurableWidgetProps{
    readonly initialConfig: WidgetConfigData;
    readonly metricFetchLoad?: boolean;
}

export function ConfigurableWidget({
    initialConfig,
    metricFetchLoad = false,
}: ConfigurableWidgetProps){
    const [isConfigOpen, setIsConfigOpen] = useState(false);
    const [config, setConfig] = useState<WidgetConfigData>(initialConfig);

    const handleSave = (newConfig: WidgetConfigData) => {
        setConfig(newConfig);
    };

    const renderChart = () => {
        const commonPropsForCharts = {
            resourceId: config.resourceId,
            title: config.title,
            metricType: config.metricType,
            metricFetchLoad,
        };

        if(config.widgetType === 'gauge'){
            return <GaugeWidget {...commonPropsForCharts} />;
        }
        
        return <LineChartWidget {...commonPropsForCharts} />;
    };

    return(
        <>
            <WidgetContainer
                title={config.title}
                className="h-full w-full"
                isResizable={false}
                onSettingsClick={() => setIsConfigOpen(true)}
                showConfig={true}
            >
                {renderChart()}
            </WidgetContainer>

            <WidgetConfig
                isOpen={isConfigOpen}
                onClose={() => setIsConfigOpen(false)}
                onSave={handleSave}
                existingConfig={config}
            />
        </>
    );
}
