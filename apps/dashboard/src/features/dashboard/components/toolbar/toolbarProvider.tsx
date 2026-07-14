"use client";

import { createContext, useContext, useState, useMemo, ReactNode } from "react";

interface ToolbarContextType {
    isEditMode: boolean;
    setIsEditMode: (value: boolean) => void;
}

const ToolbarContext = createContext<ToolbarContextType | undefined>(undefined);

export function ToolbarProvider({ children }: Readonly<{ children: ReactNode }>) {
    const [isEditMode, setIsEditMode] = useState(false);

    const contextValue = useMemo(
        () => ({
            isEditMode,
            setIsEditMode,
        }),
        [isEditMode]
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
