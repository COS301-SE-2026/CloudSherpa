"use client";

import { createContext, useContext, useRef, RefObject } from "react";

const ScrollContainerContext = createContext<RefObject<HTMLDivElement | null> | null>(null);

export function ScrollContainerProvider({
    scrollRef,
    children,
}: {
    scrollRef: RefObject<HTMLDivElement | null>;
    children: React.ReactNode;
}) {
    return (
        <ScrollContainerContext.Provider value={scrollRef}>
            {children}
        </ScrollContainerContext.Provider>
    );
}

export function useScrollContainer() {
    return useContext(ScrollContainerContext);
}
