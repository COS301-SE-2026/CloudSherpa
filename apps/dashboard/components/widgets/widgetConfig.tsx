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
    
}