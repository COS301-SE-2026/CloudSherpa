import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuTrigger,
} from "@/components/atoms/dropdown-menu";
import { Button } from "@/components/atoms/button";
import { Plus } from "lucide-react";

interface ToolbarProps {
    handleAddWidget: () => void;
    handleAddKpi: () => void;
}

export default function AddWidget({ handleAddWidget, handleAddKpi }: Readonly<ToolbarProps>) {
    return (
        <DropdownMenu>
            <DropdownMenuTrigger asChild>
                <Button variant="outline" className="text-sm px-3 py-1.5 h-auto">
                    <Plus />
                    Widget
                </Button>
            </DropdownMenuTrigger>

            <DropdownMenuContent align="start" className="w-45">
                <DropdownMenuItem
                    className="cursor-pointer font-medium flex flex-row justify-between"
                    onClick={handleAddWidget}
                >
                    Chart Widget
                    <span className="text-muted-foreground text-xs flex flex-row">shift + c</span>
                </DropdownMenuItem>

                <DropdownMenuItem
                    className="cursor-pointer font-medium flex flex-row justify-between"
                    onClick={handleAddKpi}
                >
                    KPI Widget
                    <span className="text-muted-foreground text-xs flex flex-row">shift + k</span>
                </DropdownMenuItem>
            </DropdownMenuContent>
        </DropdownMenu>
    );
}
