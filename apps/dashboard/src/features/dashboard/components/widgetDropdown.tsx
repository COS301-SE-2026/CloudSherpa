"use client";

import React from "react";
import { EllipsisVertical, Pencil, Trash } from "lucide-react";
import { Button } from "@/components/atoms/button";
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuSeparator,
    DropdownMenuTrigger,
} from "@/components/atoms/dropdown-menu";

export interface WidgetDropdownProps {
    onConfigure: () => void;
    onDelete: () => void;
    isEditMode?: boolean;
}

export function WidgetDropdown({
    onConfigure,
    onDelete,
    isEditMode = false,
}: Readonly<WidgetDropdownProps>) {
    return (
        <DropdownMenu>
            <DropdownMenuTrigger asChild>
                <Button variant="ghost" size="icon" className="h-8 w-8">
                    <EllipsisVertical className="h-4 w-4" />
                </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end" className="w-fit">
                <DropdownMenuItem onClick={onConfigure} disabled={isEditMode}>
                    <Pencil className="mr-2 h-4 w-4" />
                    Configure Widget
                </DropdownMenuItem>
                <DropdownMenuSeparator />
                <DropdownMenuItem
                    onClick={onDelete}
                    className="text-destructive focus:text-destructive"
                >
                    <Trash className="mr-2 h-4 w-4" />
                    Delete Widget
                </DropdownMenuItem>
            </DropdownMenuContent>
        </DropdownMenu>
    );
}
