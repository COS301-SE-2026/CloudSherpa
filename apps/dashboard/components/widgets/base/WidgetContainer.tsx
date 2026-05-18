'use client';
import { ReactNode, useState, useEffect, useRef } from 'react';
import { cn } from '@/lib/utils';

interface WidgetContainerProps{
    children: ReactNode;
    forTitle?: string;
    className?: string;

    defaultWidth?: number;
    defaultHeight?: number;

    minWidth?: number;
    minHeight?: number;

    isResizable?: boolean;

    onSettingsClick?: () => void;
    showConfig?: boolean;
}

export function WidgetContainer({ 
    children, 
    forTitle, 
    className = '',

    defaultWidth = 800,
    defaultHeight = 350,

    minWidth = 300,
    minHeight = 200,
    
    isResizable = true,

    onSettingsClick,
    showConfig = true

}: WidgetContainerProps){
    const [width, setWidth] = useState(defaultWidth);
    const [height, setHeight] = useState(defaultHeight);

    const [isResizing, setIsResizing] = useState(false);
    const [resizeDirection, setResizeDirection] = useState<string | null>(null);
    
    const startingXRef = useRef(0);
    const startingYRef = useRef(0);

    const startingWidthRef = useRef(0);
    const startingHeightRef = useRef(0);

    const handleResizeStart = (forHandlingResize: React.MouseEvent, direction: string) => {
        if(!isResizable){
            return;
        }

        forHandlingResize.preventDefault();
        forHandlingResize.stopPropagation();
        
        setIsResizing(true);
        setResizeDirection(direction);

        startingXRef.current = forHandlingResize.clientX;
        startingYRef.current = forHandlingResize.clientY;

        startingWidthRef.current = width;
        startingHeightRef.current = height;
    };

    useEffect(() => {
        const handleMouseMove = (e: MouseEvent) => {
            if(!isResizing){
                return;
            }
            
            let forNewWidth = width;
            let forNewHeight = height;

            const deltaX = e.clientX-startingXRef.current;
            const deltaY = e.clientY-startingYRef.current;
            
            switch(resizeDirection){
                case 'bottom':
                    forNewHeight = Math.max(minHeight, startingHeightRef.current + deltaY);
                    break;

                case 'top':
                    forNewHeight = Math.max(minHeight, startingHeightRef.current - deltaY);
                    break;

                case 'right':
                    forNewWidth = Math.max(minWidth, startingWidthRef.current + deltaX);
                    break;

                case 'left':
                    forNewWidth = Math.max(minWidth, startingWidthRef.current - deltaX);
                    break;

                case 'bottom-right':
                    forNewWidth = Math.max(minWidth, startingWidthRef.current + deltaX);
                    forNewHeight = Math.max(minHeight, startingHeightRef.current + deltaY);
                    break;

                case 'bottom-left':
                    forNewWidth = Math.max(minWidth, startingWidthRef.current - deltaX);
                    forNewHeight = Math.max(minHeight, startingHeightRef.current + deltaY);
                    break;

                case 'top-right':
                    forNewWidth = Math.max(minWidth, startingWidthRef.current + deltaX);
                    forNewHeight = Math.max(minHeight, startingHeightRef.current - deltaY);
                    break;

                case 'top-left':
                    forNewWidth = Math.max(minWidth, startingWidthRef.current - deltaX);
                    forNewHeight = Math.max(minHeight, startingHeightRef.current - deltaY);
                    break;
            }
            
            setWidth(forNewWidth);
            setHeight(forNewHeight);
        };

        const handleMouseUp = () => {
            setIsResizing(false);
            setResizeDirection(null);
            
            const resizeEvent = new CustomEvent('widget-resize', {
                detail: { width, height }
            });

            window.dispatchEvent(resizeEvent);
        };

        if(isResizing){
            window.addEventListener('mousemove', handleMouseMove);
            window.addEventListener('mouseup', handleMouseUp);
        }

        return () => {
            window.removeEventListener('mousemove', handleMouseMove);
            window.removeEventListener('mouseup', handleMouseUp);
        };

    }, [isResizing, resizeDirection, width, height, minWidth, minHeight]);

    useEffect(() => {
        const resizeEvent = new CustomEvent('widget-resize', {
            detail: { width, height }
        });

        window.dispatchEvent(resizeEvent);

    }, [width, height]);

    const handleReset = () => {
        setWidth(defaultWidth);
        setHeight(defaultHeight);
    };

    return(
        <div 
            className={cn(
                "relative rounded-xl border border-border bg-card transition-shadow duration-200 hover:shadow-lg hover:shadow-primary/5",
                className
            )}
            style={isResizable ? { width: `${width}px`, height: `${height}px` } : undefined}
        >
            {forTitle && (
                <div className="flex items-center justify-between border-b border-border px-4 py-3">
                    <h3 className="text-sm font-medium text-foreground">{forTitle}</h3>

                <div className="flex items-center gap-2">
                        {showConfig && onSettingsClick && (
                                <button
                                    onClick={onSettingsClick}
                                    className="text-muted-foreground hover:text-foreground transition-colors p-1"
                                    title="Configure Widget"
                                >
                                    <svg className="w-4 h-4" fill="currentColor" viewBox="0 0 24 24">
                                        <circle cx="12" cy="6" r="2" />
                                        <circle cx="12" cy="12" r="2" />
                                        <circle cx="12" cy="18" r="2" />
                                    </svg>
                                </button>
                        )}
                        
                        {isResizable && (
                            <button
                                onClick={handleReset}
                                className="text-muted-foreground hover:text-foreground transition-colors"
                                title="Reset size"
                            >
                                <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 8V4m0 0h4M4 4l5 5m11-1V4m0 0h-4m4 0l-5 5M4 16v4m0 0h4m-4 0l5-5m11 5l-5-5m5 5v-4m0 4h-4" />
                                </svg>
                            </button>
                        )}
                    </div>
                </div>
            )}

            {/*this is for the widget content*/}
            <div className="p-4" style={{ height: forTitle ? 'calc(100% - 52px)' : 'calc(100% - 32px)' }}>
                {children}
            </div>

            {/*this is to handle the resizing from all drections*/}
            {isResizable && (
                <>
                    {/*from the bottom*/}
                    <div
                        className="absolute bottom-0 left-0 right-0 h-2 cursor-ns-resize group z-10"
                        onMouseDown={(e) => handleResizeStart(e, 'bottom')}
                    >
                        <div className="absolute bottom-1 left-1/2 transform -translate-x-1/2 w-8 h-1 rounded-full bg-border group-hover:bg-primary transition-colors" />
                    </div>
                    
                    {/*from the right*/}
                    <div
                        className="absolute top-0 right-0 bottom-0 w-2 cursor-ew-resize group z-10"
                        onMouseDown={(e) => handleResizeStart(e, 'right')}
                    >
                        <div className="absolute right-1 top-1/2 transform -translate-y-1/2 w-1 h-8 rounded-full bg-border group-hover:bg-primary transition-colors" />
                    </div>
                    
                    {/*from the bottom right*/}
                    <div
                        className="absolute bottom-0 right-0 w-3 h-3 cursor-nw-resize group z-10"
                        onMouseDown={(e) => handleResizeStart(e, 'bottom-right')}
                    >
                        <div className="absolute bottom-1 right-1 w-2 h-2 rounded-full bg-border group-hover:bg-primary transition-colors" />
                    </div>
                    
                    {/*from the left*/}
                    <div
                        className="absolute top-0 left-0 bottom-0 w-2 cursor-ew-resize group z-10"
                        onMouseDown={(e) => handleResizeStart(e, 'left')}
                    >
                        <div className="absolute left-1 top-1/2 transform -translate-y-1/2 w-1 h-8 rounded-full bg-border group-hover:bg-primary transition-colors" />
                    </div>
                    
                    {/*from the top*/}
                    <div
                        className="absolute top-0 left-0 right-0 h-2 cursor-ns-resize group z-10"
                        onMouseDown={(e) => handleResizeStart(e, 'top')}
                    >
                        <div className="absolute top-1 left-1/2 transform -translate-x-1/2 w-8 h-1 rounded-full bg-border group-hover:bg-primary transition-colors" />
                    </div>
                    
                    {/*from the top left*/}
                    <div
                        className="absolute top-0 left-0 w-3 h-3 cursor-nw-resize group z-10"
                        onMouseDown={(e) => handleResizeStart(e, 'top-left')}
                    >
                        <div className="absolute top-1 left-1 w-2 h-2 rounded-full bg-border group-hover:bg-primary transition-colors" />
                    </div>
                    
                    {/*from the top right*/}
                    <div
                        className="absolute top-0 right-0 w-3 h-3 cursor-ne-resize group z-10"
                        onMouseDown={(e) => handleResizeStart(e, 'top-right')}
                    >
                        <div className="absolute top-1 right-1 w-2 h-2 rounded-full bg-border group-hover:bg-primary transition-colors" />
                    </div>
                    
                    {/*from the bottom left*/}
                    <div
                        className="absolute bottom-0 left-0 w-3 h-3 cursor-sw-resize group z-10"
                        onMouseDown={(e) => handleResizeStart(e, 'bottom-left')}
                    >
                        <div className="absolute bottom-1 left-1 w-2 h-2 rounded-full bg-border group-hover:bg-primary transition-colors" />
                    </div>
                </>
            )}
        </div>
    );
}
