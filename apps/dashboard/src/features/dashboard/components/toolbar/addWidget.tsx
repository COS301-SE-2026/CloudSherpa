import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuTrigger,
} from "@/components/atoms/dropdown-menu";
import { Button } from "@/components/atoms/button";
import { Plus, ChevronDown } from "lucide-react";

interface ToolbarProps {
    handleAddWidget: () => void;
    handleAddKpi: () => void;
}

export default function AddWidget({ handleAddWidget, handleAddKpi }: Readonly<ToolbarProps>) {
    return (
        <DropdownMenu>
            <DropdownMenuTrigger asChild>
                <Button className="group p-0 h-auto flex items-stretch">
                    <span className="flex items-center gap-1.5 px-3 py-1.5">
                        <span>Add Widget</span>
                    </span>

                    <span className="flex items-center border-l border-primary-foreground/50 px-2">
                        <ChevronDown className="h-4 w-4 text-primary-foreground transition-transform duration-200 group-data-[state=open]:rotate-180" />
                    </span>
                </Button>
            </DropdownMenuTrigger>

            <DropdownMenuContent align="start" className="w-45">
                <DropdownMenuItem
                    className="cursor-pointer font-medium flex flex-row justify-between"
                    onClick={handleAddWidget}
                >
                    Chart Widget
                    <span className="text-xs text-muted-foreground bg-muted px-1.5 py-0.5 rounded">
                        shift + c
                    </span>
                </DropdownMenuItem>

                <DropdownMenuItem
                    className="cursor-pointer font-medium flex flex-row justify-between"
                    onClick={handleAddKpi}
                >
                    KPI Widget
                    <span className="text-xs text-muted-foreground bg-muted px-1.5 py-0.5 rounded">
                        shift + k
                    </span>
                </DropdownMenuItem>
            </DropdownMenuContent>
        </DropdownMenu>
    );
}
