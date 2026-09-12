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
                <Button
                    variant="outline"
                    className="text-sm px-3 py-1.5 h-auto bg-primary hover:bg-primary/90"
                >
                    <Plus />
                    Widget
                </Button>
            </DropdownMenuTrigger>

            <DropdownMenuContent align="start" className="w-35">
                <DropdownMenuItem className="cursor-pointer font-medium" onClick={handleAddWidget}>
                    Chart Widget
                </DropdownMenuItem>

                <DropdownMenuItem className="cursor-pointer font-medium" onClick={handleAddKpi}>
                    KPI Widget
                </DropdownMenuItem>
            </DropdownMenuContent>
        </DropdownMenu>
    );
}
