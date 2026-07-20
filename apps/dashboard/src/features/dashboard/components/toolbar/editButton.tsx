"use client";

import { Button } from "@/components/atoms/button";

import { Pencil } from "lucide-react";
import { cn } from "@/lib/utils";

interface ToolbarProps {
    isEditMode: boolean;
    handleAddWidget: () => void;
    handleAddKpi: () => void;
    handleStartEditing: () => void;
    handleSaveEdit: () => void;
    handleCancelEdit: () => void;
}

export default function EditButton({
    isEditMode,
    handleAddWidget,
    handleAddKpi,
    handleStartEditing,
    handleSaveEdit,
    handleCancelEdit,
}: Readonly<ToolbarProps>) {
    return (
        <div className="flex items-center gap-2">
            <Button
                variant="outline"
                size={isEditMode ? "sm" : "icon"}
                onClick={isEditMode ? handleCancelEdit : handleStartEditing}
                className={cn(
                    "bg-card border-border text-foreground hover:text-foreground hover:bg-primary transition-all duration-200",
                    isEditMode &&
                        "bg-destructive/10 border-destructive text-destructive hover:bg-destructive/20 hover:text-destructive hover:border-destructive order-3 md:order-1"
                )}
            >
                {isEditMode ? "Cancel" : <Pencil className="h-4 w-4" />}
            </Button>

            {isEditMode && (
                <>
                    <Button
                        size="sm"
                        onClick={handleSaveEdit}
                        className="bg-primary text-primary-foreground hover:bg-primary/90 shadow-sm order-2"
                    >
                        Save
                    </Button>
                    <Button
                        size="sm"
                        onClick={() => handleAddWidget()}
                        className="bg-primary text-primary-foreground hover:bg-primary/90 shadow-sm order-1 md:order-3"
                    >
                        Add Widget
                    </Button>
                    <Button
                        size="sm"
                        onClick={() => handleAddKpi()}
                        className="bg-primary text-primary-foreground hover:bg-primary/90 shadow-sm order-1 md:order-3"
                    >
                        Add KPI
                    </Button>
                </>
            )}
        </div>
    );
}
