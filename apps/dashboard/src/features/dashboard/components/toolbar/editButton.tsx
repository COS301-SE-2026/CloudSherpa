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
                {isEditMode && (
                    <Tooltip>
                        <TooltipTrigger asChild>
                            <Button
                                size="sm"
                                onClick={handleSaveEdit}
                                className="bg-primary text-primary-foreground hover:bg-primary/90 shadow-sm"
                            >
                                Save
                            </Button>
                        </TooltipTrigger>
                        <TooltipContent>
                            <div className="flex flex-row items-center gap-3">
                                <span className="text-sm font-medium">Save Changes</span>
                                <span className="text-xs text-muted-foreground bg-muted px-1.5 py-0.5 rounded">
                                    shift + s
                                </span>
                            </div>
                        </TooltipContent>
                    </Tooltip>
                )}
                <Tooltip>
                    <TooltipTrigger asChild>
                        <Button
                            aria-label="editbtn"
                            variant="outline"
                            onClick={isEditMode ? handleCancelEdit : handleStartEditing}
                            className={cn(
                                "bg-card border-border text-foreground hover:text-foreground transition-all duration-200",
                                isEditMode &&
                                    "bg-destructive/10 border-destructive text-destructive hover:bg-destructive/20 hover:text-destructive hover:border-destructive"
                            )}
                        >
                            {isEditMode ? (
                                "Cancel"
                            ) : (
                                <>
                                    <Pencil className="h-4 w-4" />
                                    <span className="text-base">Edit</span>
                                </>
                            )}
                        </Button>
                    </TooltipTrigger>
                    <TooltipContent>
                        {isEditMode ? (
                            <div className="flex flex-row items-center gap-3">
                                <span className="text-sm font-medium">Cancel Edit</span>
                                <span className="text-xs text-muted-foreground bg-muted px-1.5 py-0.5 rounded">
                                    esc
                                </span>
                            </div>
                        ) : (
                            <div className="flex flex-row items-center gap-3">
                                <span className="text-sm font-medium">Edit Dashboard Layout</span>
                                <span className="text-xs text-muted-foreground bg-muted px-1.5 py-0.5 rounded">
                                    shift + E
                                </span>
                            </div>
                        )}
                    </TooltipContent>
                </Tooltip>
            </div>
        </TooltipProvider>
    );
}
