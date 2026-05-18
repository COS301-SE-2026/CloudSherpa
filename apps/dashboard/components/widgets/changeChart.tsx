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
}