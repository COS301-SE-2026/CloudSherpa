"use client";

import {Tooltip, TooltipContent, TooltipProvider, TooltipTrigger} from "@/components/atoms/tooltip";

//copied both blocks from thresholds table file

interface PropsForTruncation {
    text: string;
    className?: string;
    tooltipText?: string;
}

export function Truncation({text, className = "", tooltipText} : Readonly<PropsForTruncation>){
    return(
        <TooltipProvider>
            <Tooltip>
                <TooltipTrigger asChild>
                    <span className={`block truncate ${className}`}> {text} </span>
                </TooltipTrigger>

                <TooltipContent>
                    {" "}
                    <p className="max-w-xs break-all"> {tooltipText ?? text} </p>
                </TooltipContent>
            </Tooltip>
        </TooltipProvider>
    );
}