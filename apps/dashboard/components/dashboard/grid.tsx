"use client"
import React, { useLayoutEffect, useRef, useState } from 'react';
import 'gridstack/dist/gridstack.min.css';
import { GridStack } from 'gridstack';

interface WidgetConfig {
  id: string;
  type: string;
  x: number;
  y: number;
  w: number;
  h: number;
}

//widget stubs
const WidgetStub = ({ type }: { type: string }) => (
  <div className="grid-stack-item-content bg-slate-800 border border-slate-700 rounded-lg p-4 text-white shadow-lg">
    <div className="flex justify-between items-center mb-2">
      <div className="drag-handle cursor-move p-1 text-slate-500 hover:text-white">⋮⋮</div>
    </div>
  </div>
);

const CloudSherpaGrid: React.FC = () => {
  const gridRef = useRef<HTMLDivElement>(null);
  const gridStackInstance = useRef<GridStack | null>(null);
//what I imagine the layout structure to look like
  const [widgets] = useState<WidgetConfig[]>([
    { id: '1', type: 'test1',x: 0, y: 0, w: 4, h: 2 },
    { id: '2', type: 'test1', x: 4, y: 0, w: 4, h: 4 },
    { id: '3', type: 'test1',x: 0, y: 2, w: 4, h: 2 },
  ]);

  useLayoutEffect(() => {
    if (gridRef.current && !gridStackInstance.current) {
      // Initialize gridStack
      gridStackInstance.current = GridStack.init({
        cellHeight: 150,
        margin: 10,
        handle: '.drag-handle', //restrict dragging to "handle" to prevent accidental dragging of components
        draggable: {
          handle: '.drag-handle',
          scroll: false,
          appendTo: 'body',
        },
      }, gridRef.current);

      // listens for layout changes
      gridStackInstance.current.on('change', (event, items) => {
        console.log('Layout updated. Save this to CloudSherpa DB:', items);
      });
    }

    return () => {
      gridStackInstance.current?.destroy(false);
      gridStackInstance.current = null;
    };
  }, []);

  return (
    <div className="p-6 min-h-screen bg-slate-900">

      {/* grid */}
      <div ref={gridRef} className="grid-stack">
        {widgets.map((w) => (
          <div 
            key={w.id} 
            className="grid-stack-item" 
            gs-id={w.id} 
            gs-x={w.x} 
            gs-y={w.y} 
            gs-w={w.w} 
            gs-h={w.h}
          >
            <WidgetStub type={w.type} />
          </div>
        ))}
      </div>
    </div>
  );
};

export default CloudSherpaGrid;