"use client";

import { HelpCircle, X } from "lucide-react";
import { Button } from "@/components/atoms/button";
import { useRouter } from "next/navigation";
import {
    Tooltip,
    TooltipContent,
    TooltipProvider,
    TooltipTrigger,
} from "@/components/atoms/tooltip";

//this is to add the help menu button for each step on the setup wizards
//will include a tooltip to inform users this is where they can get help

interface PropsForHelpButton {
    provider: "aws" | "gcp" | "azure";
}

export function HelpButton({ provider }: Readonly<PropsForHelpButton>) {
    const router = useRouter();

    const handlingTroubleshoot = () => {
        router.push(`/helpMenu/documents/connections?forProviders=${provider}`);
    };

    return (
        <TooltipProvider>
            <div className="fixed bottom-6 right-6 z-50">
                <Tooltip>
                    <TooltipTrigger asChild>
                        <Button
                            type="button"
                            onClick={handlingTroubleshoot}
                            className="rounded-full w-10 h-10 flex items-center justify-center shadow-lg bg-primary hover:bg-primary/90 text-primary-foreground transition-all duration-200 hover:scale-105 p-0"
                        >
                            {" "}
                            <HelpCircle size={24} strokeWidth={2} className="text-white" />{" "}
                        </Button>
                    </TooltipTrigger>

                    <TooltipContent side="left" className="max-w-xs">
                        <p>
                            {" "}
                            Need help setting up {provider.toUpperCase()}? Click here for the setup
                            guide.{" "}
                        </p>
                    </TooltipContent>
                </Tooltip>
            </div>
        </TooltipProvider>
    );
}
