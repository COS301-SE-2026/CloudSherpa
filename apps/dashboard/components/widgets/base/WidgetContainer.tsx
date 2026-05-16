'use client';
import { ReactNode } from 'react';
import { cn } from '@/lib/utils';

interface WidgetContainerProps{
    childrenComponents: ReactNode;
    forTitle?: string;
    className?: string;
}

export function WidgetContainer({ childrenComponents, forTitle, className = '' }: WidgetContainerProps){
    return(
        <div className={cn("relative rounded-xl border border-border bg-card", className)}>
            {forTitle && (
                <div className="flex items-center justify-between border-b border-border px-4 py-3">
                    <h3 className="text-sm font-medium text-foreground">{forTitle}</h3>
                </div>
            )}
            <div className="p-4">{childrenComponents}</div>
        </div>
    );
}