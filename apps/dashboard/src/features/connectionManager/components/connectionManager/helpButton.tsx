"use client";

import {useState, useEffect} from "react";
import {HelpCircle, X} from "lucide-react";
import {Button} from "@/components/atoms/button";
import {useRouter} from "next/navigation";

//this is to add the help menu button for each step on the setup wizards
//will include a tooltip to inform users this is where they can get help

interface PropsForHelpButton{
    provider : "aws" | "gcp" | "azure";
}

export function HelpButton({provider} : Readonly<PropsForHelpButton>){
    const router = useRouter();

    const [tooltip, setTooltip] = useState(false);

    const [isLoading, setIsLoading] = useState(true);

    useEffect(() => {
        const keyForTooltip = `dismissed_${provider}`;

        const dismissed = localStorage.getItem(keyForTooltip);

        if(!dismissed){
            setTooltip(true);
        }

        setIsLoading(false);
    }, [provider]);

    const handlingTroubleshoot = () => {
        router.push(`/helpMenu/documents/connections?forProviders=${provider}`);
    };

    const handlingDismissedTooltip = () => {
        setTooltip(false);

        const keyForTooltip = `dismissed_${provider}`;

        localStorage.setItem(keyForTooltip, "true");
    };

    if(isLoading){
        return null;
    }

    return(
        <div className = "fixed bottom-6 right-6 z-50">

            {tooltip && (
                <div className = "absolute bottom-12 right-0 mb-1 w-64 p-3 bg-popover rounded-lg shadow-lg border border-border">
                    <button type = "button" onClick = {handlingDismissedTooltip} className = "absolute top-1 right-1 text-muted-foreground hover:text-foreground transition-colors" aria-label = "Dismiss tooltip"> <X size = {14} strokeWidth = {1.75}/> </button>

                    <p className = "text-sm text-foreground pr-4"> Need help setting up {provider.toUpperCase()}? Click here for the setup guide. </p>
                </div>
            )}

            <Button type = "button" onClick = {handlingTroubleshoot} className = "rounded-full w-10 h-10 flex items-center justify-center shadow-lg bg-primary hover:bg-primary/90 text-primary-foreground transition-all duration-200 hover:scale-105 p-0"> <HelpCircle size = {24} strokeWidth = {2} className = "text-white"/> </Button>
        </div>
    );
}