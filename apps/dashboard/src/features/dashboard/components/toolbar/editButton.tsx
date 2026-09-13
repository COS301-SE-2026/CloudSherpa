"use client";

import { Button } from "@/components/atoms/button";
import {
    Tooltip,
    TooltipContent,
    TooltipProvider,
    TooltipTrigger,
} from "@/components/atoms/tooltip";

import { Pencil } from "lucide-react";
import { cn } from "@/lib/utils";

interface ToolbarProps {
    isEditMode: boolean;
    handleStartEditing: () => void;
    handleSaveEdit: () => void;
    handleCancelEdit: () => void;
}

export default function EditButton({
    isEditMode,
    handleStartEditing,
    handleSaveEdit,
    handleCancelEdit,
}: Readonly<ToolbarProps>) {
    return (
        <TooltipProvider>
            <div id="editDashboard" className="flex items-center gap-2">
                <Tooltip>
                    <TooltipTrigger asChild>
                        <Button
                            aria-label="editbtn"
                            variant="outline"
                            onClick={isEditMode ? handleCancelEdit : handleStartEditing}
                            className={cn(
                                "bg-card border-border text-foreground hover:text-foreground transition-all duration-200",
                                isEditMode &&
                                    "bg-destructive/10 border-destructive text-destructive hover:bg-destructive/20 hover:text-destructive hover:border-destructive order-3 md:order-1"
                            )}
                        >
                            {isEditMode ? (
                                "Cancel"
                            ) : (
                                <>
                                    <Pencil className="h-4 w-4" />
                                    <span className="sm:hidden text-base">Edit</span>
                                </>
                            )}
                        </Button>
                    </TooltipTrigger>
                    <TooltipContent>
                        <p className="text-xs font-medium">{isEditMode ? "Esc" : "Shift + E"}</p>
                    </TooltipContent>
                </Tooltip>

                {isEditMode && (
                    <Tooltip>
                        <TooltipTrigger asChild>
                            <Button
                                size="sm"
                                onClick={handleSaveEdit}
                                className="bg-primary text-primary-foreground hover:bg-primary/90 shadow-sm order-2"
                            >
                                Save
                            </Button>
                        </TooltipTrigger>
                        <TooltipContent>
                            <p className="text-xs font-medium">Shift + S</p>
                        </TooltipContent>
                    </Tooltip>
                )}
            </div>
        </TooltipProvider>
    );
}
