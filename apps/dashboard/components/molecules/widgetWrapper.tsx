import { GripVertical, X } from "lucide-react";
import { cn } from "@/lib/utils";

interface WidgetWrapperProps {
  id: string;
  x: number;
  y: number;
  w: number;
  h: number;
  isEditMode: boolean;
  children: React.ReactNode;
  title: string;
}

export const WidgetWrapper = ({ id, x, y, w, h, isEditMode, children, title }: WidgetWrapperProps) => {
  return (
    <div 
      className="grid-stack-item" 
      gs-id={id} gs-x={x} gs-y={y} gs-w={w} gs-h={h}
    >
      <div className={cn(
        "grid-stack-item-content rounded-xl border bg-card shadow-sm transition-colors overflow-hidden flex flex-col",
        isEditMode ? "border-primary/40 ring-1 ring-primary/10" : "border-border"
      )}>
        {/* handle area */}
        <div className="flex items-center justify-between p-3 border-b border-border/50 bg-muted/30">
          <span className="text-xs font-semibold text-foreground-secondary uppercase tracking-tight truncate">
            {title}
          </span>
          
          {isEditMode && (
            <div className="flex items-center gap-2">
               <div className="drag-handle cursor-grab active:cursor-grabbing p-1 hover:bg-hover rounded transition-colors text-muted-foreground hover:text-primary">
                <GripVertical className="h-4 w-4" />
              </div>
              <button className="p-1 hover:bg-destructive/10 hover:text-destructive rounded text-muted-foreground transition-colors">
                <X className="h-3 w-3" />
              </button>
            </div>
          )}
        </div>

        <div className={cn(
          "flex-1 p-4",
          isEditMode && "pointer-events-none opacity-50" 
        )}>
          {children}
        </div>
      </div>
    </div>
  );
};