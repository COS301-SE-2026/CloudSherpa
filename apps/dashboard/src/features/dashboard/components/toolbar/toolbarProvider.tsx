"use client";

import { createContext, useContext, useState, useMemo, ReactNode } from "react";

interface ToolbarContextType {
    isEditMode: boolean;
    setIsEditMode: (value: boolean) => void;
    isSelectorOpen: boolean;
    setIsSelectorOpen: (value: boolean) => void;
    selectorView: "list" | "create";
    setSelectorView: (value: "list" | "create") => void;
    isApplyDialogOpen: boolean;
    setIsApplyDialogOpen: (value: boolean) => void;
}

const ToolbarContext = createContext<ToolbarContextType | undefined>(undefined);

export function ToolbarProvider({ children }: Readonly<{ children: ReactNode }>) {
    const [isEditMode, setIsEditMode] = useState(false);
    const [isSelectorOpen, setIsSelectorOpen] = useState(false);
    const [selectorView, setSelectorView] = useState<"list" | "create">("list");
    const [isApplyDialogOpen, setIsApplyDialogOpen] = useState(false);

    const contextValue = useMemo(
        () => ({
            isEditMode,
            setIsEditMode,
            isSelectorOpen,
            setIsSelectorOpen,
            selectorView,
            setSelectorView,
            isApplyDialogOpen,
            setIsApplyDialogOpen,
        }),
        [isEditMode, isSelectorOpen, selectorView, isApplyDialogOpen]
    );

    return <ToolbarContext.Provider value={contextValue}>{children}</ToolbarContext.Provider>;
}

export function useToolbar() {
    const context = useContext(ToolbarContext);
    if (context === undefined) {
        throw new Error("useToolbar must be used within a ToolbarProvider");
    }
    return context;
}
