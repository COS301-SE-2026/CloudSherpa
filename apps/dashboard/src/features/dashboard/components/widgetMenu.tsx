"use client";

import React, { ReactNode } from "react";
import { Pencil, Trash } from "lucide-react";
import {
    ContextMenu,
    ContextMenuContent,
    ContextMenuItem,
    ContextMenuSeparator,
    ContextMenuTrigger,
} from "@/components/atoms/context-menu";

export interface WidgetMenuProps {
    children: ReactNode;
    onConfigure: () => void;
    onDelete: () => void;
    isEditMode?: boolean;
    preview?: boolean;
}

export function WidgetMenu({
    children,
    onConfigure,
    onDelete,
    isEditMode = false,
    preview = false,
}: Readonly<WidgetMenuProps>) {
    return (
        <ContextMenu>
            <ContextMenuTrigger className="h-full w-full block">{children}</ContextMenuTrigger>

            {!preview && (
                <ContextMenuContent className="w-48">
                    <ContextMenuItem
                        onClick={onConfigure}
                        disabled={isEditMode}
                        aria-label="configure widget button"
                    >
                        <Pencil className="mr-2 h-4 w-4" />
                        Configure Widget
                    </ContextMenuItem>
                    <ContextMenuSeparator />
                    <ContextMenuItem
                        onClick={onDelete}
                        className="text-destructive focus:text-destructive"
                        aria-label="delete widget button"
                    >
                        <Trash className="mr-2 h-4 w-4" />
                        Delete Widget
                    </ContextMenuItem>
                </ContextMenuContent>
            )}
        </ContextMenu>
    );
}
