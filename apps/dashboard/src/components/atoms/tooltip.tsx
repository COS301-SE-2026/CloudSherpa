"use client";

import * as React from "react";
import { Tooltip as TooltipPrimitive } from "radix-ui";

import { cn } from "@/lib/utils";

type TooltipContextType = {
    showOnTruncate?: boolean;
    isTruncated: boolean;
    setIsTruncated: React.Dispatch<React.SetStateAction<boolean>>;
};

const TooltipContext = React.createContext<TooltipContextType | null>(null);

function TooltipProvider({
    delayDuration = 600,
    ...props
}: React.ComponentProps<typeof TooltipPrimitive.Provider>) {
    return (
        <TooltipPrimitive.Provider
            data-slot="tooltip-provider"
            delayDuration={delayDuration}
            {...props}
        />
    );
}

function Tooltip({
    showOnTruncate,
    ...props
}: React.ComponentProps<typeof TooltipPrimitive.Root> & { showOnTruncate?: boolean }) {
    const [isTruncated, setIsTruncated] = React.useState(true);

    const open = showOnTruncate && !isTruncated ? false : props.open; // props.open just lets it defaults to normal behavriour as afallback
    return (
        <TooltipContext.Provider value={{ showOnTruncate, isTruncated, setIsTruncated }}>
            <TooltipPrimitive.Root data-slot="tooltip" open={open} {...props} />
        </TooltipContext.Provider>
    );
}

//depth first search on dom tree
function hasTruncatedChild(element: HTMLElement): boolean {
    const stack: HTMLElement[] = [element]; //init stack with root element

    while (stack.length > 0) {
        //loop til stack empty
        const current = stack.pop(); //remove and return last element added to stack and move to sibling nodes
        if (!current) continue;

        if (current.scrollWidth > current.clientWidth) {
            //clientwidth is visible width of element on screen, scrollwidth is totoal width component actually needs
            return true;
        }

        for (const child of Array.from(current.children)) {
            //if current not truncated add child elements to stack to be checked in following loops
            stack.push(child as HTMLElement);
        }
    }

    return false;
}

const TooltipTrigger = React.forwardRef<
    HTMLButtonElement,
    React.ComponentPropsWithoutRef<typeof TooltipPrimitive.Trigger>
>(({ onMouseEnter, onFocus, ...props }, ref) => {
    const context = React.useContext(TooltipContext);

    const internalRef = React.useRef<HTMLButtonElement | null>(null);

    const mergedRef = React.useCallback(
        (node: HTMLButtonElement | null) => {
            internalRef.current = node;
            if (typeof ref === "function") {
                ref(node);
            } else if (ref) {
                (ref as { current: HTMLButtonElement | null }).current = node;
            }
        },
        [ref]
    );

    const checkTruncated = () => {
        if (context?.showOnTruncate && internalRef.current) {
            context.setIsTruncated(hasTruncatedChild(internalRef.current));
        }
    };

    return (
        <TooltipPrimitive.Trigger
            ref={mergedRef}
            data-slot="tooltip-trigger"
            onPointerEnter={(e) => {
                checkTruncated();
                onMouseEnter?.(e);
            }}
            onFocus={(e) => {
                checkTruncated();
                onFocus?.(e);
            }}
            {...props}
        />
    );
});
TooltipTrigger.displayName = TooltipPrimitive.Trigger.displayName;

function TooltipContent({
    className,
    sideOffset = 0,
    children,
    ...props
}: React.ComponentProps<typeof TooltipPrimitive.Content>) {
    return (
        <TooltipPrimitive.Portal>
            <TooltipPrimitive.Content
                data-slot="tooltip-content"
                sideOffset={sideOffset}
                className={cn(
                    "z-50 inline-flex w-fit max-w-xs origin-(--radix-tooltip-content-transform-origin) items-center gap-1.5 rounded-md bg-foreground px-3 py-1.5 text-xs text-background has-data-[slot=kbd]:pr-1.5 data-[side=bottom]:slide-in-from-top-2 data-[side=left]:slide-in-from-right-2 data-[side=right]:slide-in-from-left-2 data-[side=top]:slide-in-from-bottom-2 **:data-[slot=kbd]:relative **:data-[slot=kbd]:isolate **:data-[slot=kbd]:z-50 **:data-[slot=kbd]:rounded-sm data-[state=delayed-open]:animate-in data-[state=delayed-open]:fade-in-0 data-[state=delayed-open]:zoom-in-95 data-open:animate-in data-open:fade-in-0 data-open:zoom-in-95 data-closed:animate-out data-closed:fade-out-0 data-closed:zoom-out-95",
                    className
                )}
                {...props}
            >
                {children}
                <TooltipPrimitive.Arrow className="z-50 size-2.5 translate-y-[calc(-50%_-_2px)] rotate-45 rounded-[2px] bg-foreground fill-foreground" />
            </TooltipPrimitive.Content>
        </TooltipPrimitive.Portal>
    );
}

export { Tooltip, TooltipContent, TooltipProvider, TooltipTrigger };
