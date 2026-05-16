'use client';
import { ReactNode, useState, useEffect, useRef } from 'react';
import { cn } from '@/lib/utils';

interface WidgetContainerProps{
    childrenComponents: ReactNode;
    forTitle?: string;
    className?: string;

    defaultWidth?: number;
    defaultHeight?: number;

    minWidth?: number;
    minHeight?: number;

    isResizable?: boolean;
}

export function WidgetContainer({ 
    childrenComponents, 
    forTitle, 
    className = '',

    defaultWidth = 800,
    defaultHeight = 350,

    minWidth = 300,
    minHeight = 200,
    
    isResizable = true

}: WidgetContainerProps){
    const [width, setWidth] = useState(defaultWidth);
    const [height, setHeight] = useState(defaultHeight);

    return(
        <div 
            className={cn(
                "relative rounded-xl border border-border bg-card transition-shadow duration-200 hover:shadow-lg hover:shadow-primary/5",
                className
            )}
            style={{ width: `${width}px`, height: `${height}px` }}
        >
            {forTitle && (
                <div className="flex items-center justify-between border-b border-border px-4 py-3">
                    <h3 className="text-sm font-medium text-foreground">{forTitle}</h3>
                </div>
            )}

            {/*this is for the widget content*/}
            <div className="p-4" style={{ height: forTitle ? 'calc(100% - 52px)' : 'calc(100% - 32px)' }}>
                {childrenComponents}
            </div>
        </div>
    );
}