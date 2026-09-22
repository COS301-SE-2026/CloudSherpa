"use client";

import { Button } from "@/components/atoms/button";
import {
    Tooltip,
    TooltipContent,
    TooltipProvider,
    TooltipTrigger,
} from "@/components/atoms/tooltip";
import { Kbd, KbdGroup } from "@/components/atoms/kbd";

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
                                onClick={handleSaveEdit}
                                className="bg-primary text-primary-foreground hover:bg-primary/90 shadow-sm"
                            >
                                Save
                            </Button>
                        </TooltipTrigger>
                        <TooltipContent>
                            <div className="flex flex-row items-center gap-3">
                                <span className="text-sm">Save Changes</span>
                                <KbdGroup>
                                    <Kbd>Shift</Kbd>
                                    <span>+</span>
                                    <Kbd>S</Kbd>
                                </KbdGroup>
                            </div>
                        </TooltipContent>
                    </Tooltip>
                )}
                <Tooltip>
                    <TooltipTrigger asChild>
                        <Button
                            aria-label="editbtn"
                            variant={isEditMode ? "destructive" : "outline"}
                            onClick={isEditMode ? handleCancelEdit : handleStartEditing}
                        >
                            {isEditMode ? (
                                "Cancel"
                            ) : (
                                <>
                                    <Pencil />
                                    <span className="text-sm">Edit Layout</span>
                                </>
                            )}
                        </Button>
                    </TooltipTrigger>
                    <TooltipContent>
                        {isEditMode ? (
                            <div className="flex flex-row items-center gap-3">
                                <span className="text-sm">Cancel Edit</span>
                                <KbdGroup>
                                    <Kbd>Esc</Kbd>
                                </KbdGroup>
                            </div>
                        ) : (
                            <div className="flex flex-row items-center gap-3">
                                <span className="text-sm">Edit Dashboard Layout</span>
                                <KbdGroup>
                                    <Kbd>Shift</Kbd>
                                    <span>+</span>
                                    <Kbd>E</Kbd>
                                </KbdGroup>
                            </div>
                        )}
                    </TooltipContent>
                </Tooltip>
            </div>
        </TooltipProvider>
    );
}
