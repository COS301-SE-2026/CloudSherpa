import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuTrigger,
} from "@/components/atoms/dropdown-menu";
import { Button } from "@/components/atoms/button";
import { ChevronDown, Plus } from "lucide-react";

interface ToolbarProps {
    handleAddWidget: () => void;
    handleAddKpi: () => void;
    isEditMode: boolean;
}

export default function AddWidget({
    handleAddWidget,
    handleAddKpi,
    isEditMode,
}: Readonly<ToolbarProps>) {
    return (
        <DropdownMenu>
            <DropdownMenuTrigger asChild>
                <Button
                    aria-label="add widget"
                    className="group p-0 h-auto flex items-stretch"
                    disabled={isEditMode}
                >
                    <span className="flex items-center gap-1.5 px-3 py-1.5">
                        <span>Add Widget</span>
                    </span>

                    <span className="flex items-center border-l border-primary-foreground/50 px-2">
                        <ChevronDown className="h-4 w-4 text-primary-foreground transition-transform duration-200 group-data-[state=open]:rotate-180" />
                    </span>
                </Button>
            </DropdownMenuTrigger>

            <DropdownMenuContent align="start" className="w-50">
                <DropdownMenuItem
                    className="cursor-pointer w-full font-medium flex flex-row justify-between gap-3"
                    onClick={handleAddWidget}
                    aria-label="add chart widget"
                >
                    <span>Chart Widget</span>
                    <span className="text-xs text-muted-foreground bg-muted px-1.5 py-0.5 rounded flex flex-row gap-1">
                        <span>shift</span>
                        <Plus className="h-2 w-2" />
                        <span>C</span>
                    </span>
                </DropdownMenuItem>

                <DropdownMenuItem
                    className="cursor-pointer w-full font-medium flex flex-row justify-between gap-3"
                    onClick={handleAddKpi}
                    aria-label="add kpi widget"
                >
                    <span>KPI Widget</span>
                    <span className="text-xs text-muted-foreground bg-muted px-1.5 py-0.5 rounded flex flex-row gap-1">
                        <span>shift</span>
                        <Plus className="h-2 w-2" />
                        <span>K</span>
                    </span>
                </DropdownMenuItem>
            </DropdownMenuContent>
        </DropdownMenu>
    );
}
