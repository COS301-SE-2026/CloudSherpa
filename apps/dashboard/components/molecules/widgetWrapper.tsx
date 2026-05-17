import { GripVertical, X , Trash} from "lucide-react";
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
  onDeleteWidget: (widgetId: string) => void; // Add onDeleteWidget prop
  autoPosition?: boolean;
}

export const WidgetWrapper = ({ id, x, y, w, h, isEditMode, children, title, autoPosition, onDeleteWidget }: WidgetWrapperProps) => {
  return (
    <div 
      className="grid-stack-item" 
      gs-id={id} gs-x={x} gs-y={y} gs-w={w} gs-h={h}
      gs-auto-position={autoPosition ? "true" : undefined}
    >
      <div className="grid-stack-item-content relative overflow-visible rounded-md group">
        {/* handle area overlay */}
       {isEditMode && ( 
          <div className="absolute top-2 right-2 z-50 flex items-center gap-1 opacity-0 group-hover:opacity-100 transition-opacity duration-200">
             <div className="drag-handle cursor-grab active:cursor-grabbing bg-background border border-border shadow-md p-1 rounded-md text-muted-foreground hover:text-primary transition-all">
              <GripVertical className="h-3.5 w-3.5" />
            </div>
            <button onClick={() => onDeleteWidget(id)} className="bg-background border border-border shadow-md p-1 rounded-md text-muted-foreground hover:bg-destructive hover:text-destructive-foreground transition-all">
              <Trash className="h-3.5 w-3.5" />
            </button>
          </div>
        )}

        <div className={cn(
          "h-full w-full",
          isEditMode && "pointer-events-none ring-2 ring-primary/20 rounded-xl transition-all" 
        )}>
          {children}
        </div>
      </div>
    </div>
  );
};