"use client";

import { Button } from "@/components/atoms/button";

import { Pencil } from "lucide-react";
import { cn } from "@/lib/utils";

interface ToolbarProps {
  isEditMode: boolean;
  handleAddWidget: () => void;
  handleStartEditing: () => void;
  handleSaveEdit: () => void;
  handleCancelEdit: () => void;
}

export default function EditButton({
  isEditMode,
  handleAddWidget,
  handleStartEditing,
  handleSaveEdit,
  handleCancelEdit,
}: ToolbarProps) {
  return (
            <div className="flex items-center gap-2">
              <Button
                variant="outline"
                size={isEditMode ? "sm" : "icon"}
                onClick={isEditMode ? handleCancelEdit : handleStartEditing}
                className={cn(
                  "bg-card border-border text-foreground hover:text-primary hover:border-primary transition-all duration-200",
                  isEditMode &&
                    "bg-destructive/10 border-destructive text-destructive hover:bg-destructive/20 hover:text-destructive hover:border-destructive",
                )}>
                {isEditMode ? "Cancel" : <Pencil className="h-4 w-4" />}
              </Button>

              {isEditMode && (
                <div className="flex items-center gap-2">
                  <Button
                    size="sm"
                    onClick={handleSaveEdit}
                    className="bg-primary text-primary-foreground hover:bg-primary/90 shadow-sm">
                    Save
                  </Button>
                  <Button
                    size="sm"
                    onClick={() => handleAddWidget()} 
                    className="bg-primary text-primary-foreground hover:bg-primary/90 shadow-sm">
                    Add Widget
                  </Button>
                </div>
              )}
            </div>
  );
}
