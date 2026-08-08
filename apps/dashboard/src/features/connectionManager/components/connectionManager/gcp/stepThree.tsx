"use client";

import React, {useState} from "react";
import {StepThree} from "@/features/connectionManager/components/connectionManager/wizardSetup/stepThree";
import {Button} from "@/components/atoms/button";
import {Tooltip, TooltipContent, TooltipProvider, TooltipTrigger} from "@/components/atoms/tooltip";
import {Slider} from "@/components/atoms/slider";
import {Label} from "@/components/atoms/label";

interface DetailsForResource{
    id : string;
    name : string;
    type?: string;
}

interface StepThreePropsForGcp{
    resources?: DetailsForResource[];
    onNext : (data : Record<string, unknown>) => void;
    onBack?: () => void;
    ingestionPeriod?: string;
}

function GcpResources({
    resource, selectedResource, onChecked
} : Readonly<{
    resource : DetailsForResource;
    selectedResource : boolean;
    onChecked : (resourceId : string, checked : boolean) => void;
}>){
    return(
        <label className = "flex items-start gap-3 p-4 bg-background rounded-lg border border-border hover:border-primary/40 transition-all cursor-pointer focus-within:ring-2 focus-within:ring-primary focus-within:ring-offset-2">
            <input type = "checkbox" checked = {selectedResource} onChange = {(change) => onChecked(resource.id, change.target.checked)} className = "mt-1 w-4 h-4 rounded border-border bg-background text-primary focus:ring-primary"/>

            <div className = "font-medium text-foreground">
                {resource.name}

                <span className = "ml-2 text-muted-foreground">({resource.id}) </span>
            </div>
        </label>
    );
}

export default function StepThreeGcp({
    resources = [], onNext, onBack, ingestionPeriod = "60",
} : Readonly<StepThreePropsForGcp>){
    const [resourcesSelected, setResourcesSelected] = useState<string[]>([]);

    const [forSaving, setForSaving] = useState(false);

    const [errors, setErrors] = useState<string | null>(null);

    const [forIngestionPeriod, setForIngestionPeriod] = useState<string>(ingestionPeriod);

    const hardCodedResources : DetailsForResource[] = [
        {id : "resource1",
         name : "Resource one",
         type : "Service one",
        },
    ];

    const realResources = resources && resources.length > 0 ? resources : hardCodedResources;

    const recIngestionPeriod = resourcesSelected.length*5*20;

    const formattingSecond = (totalSeconds : string | number) => {
        const seconds = Number(totalSeconds);

        if(Number.isNaN(seconds) || seconds<=0){
            return "0 seconds";
        }

        const minutes = Math.floor(seconds/60);

        const secondsLeft = seconds%60;

        let minText = "";
        if(minutes>0){
            const labelEnding = minutes === 1 ? "" : "s";
            minText = `${minutes} minute${labelEnding}`;
        }

        let secText = "";
        if(secondsLeft>0){
            const labelEnding = secondsLeft === 1 ? "" : "s";
            secText = `${secondsLeft} second${labelEnding}`;
        }

        if(minText && secText){
            return `${minText} ${secText}`;
        }

        return minText || secText;
    };

    const handlingResourceChecks = (resourceId : string, checked : boolean) => {
        setResourcesSelected((previous) => {
            if(checked){
                return previous.includes(resourceId) ? previous : [...previous, resourceId];
            }

            return previous.filter((id) => id !== resourceId);
        });
    };

    const handlingSelectedAll = () => {
        if(resourcesSelected.length === realResources.length){
            setResourcesSelected([]);
        } else{
            setResourcesSelected(realResources.map((resource) => resource.id));
        }
    };

    const handlingSubmit = async (forEvent : React.SubmitEvent<HTMLFormElement>) => {
        forEvent.preventDefault();

        setForSaving(true);
        setErrors(null);

        try{
            onNext({
                selectedResources : resourcesSelected, ingestionPeriod : forIngestionPeriod
            });
        } catch(forError){
            setErrors("Unable to complete GCP connection setup");
        } finally{
            setForSaving(false);
        }
    };

    return(
        <StepThree heading = "Select instances"
                   description = "Select the instance you want CloudSherpa to monitor"
                   onSubmit = {handlingSubmit} onBack = {onBack || (() => {})} forSaving = {forSaving} forErrors = {errors}
        >
            <div className = "flex flex-wrap items-center justify-between gap-2 mb-4">
                <h3 className = "text-foreground text-sm font-semibold uppercase tracking-wider opacity-80"> Available resources </h3>

                <Button type = "button" variant = "ghost" size = "sm" onClick = {handlingSelectedAll} className = "text-primary hover:text-accent text-sm transition-colors px-0">
                    {resourcesSelected.length === realResources.length ? "Deselect All" : "Select All"}
                </Button>
            </div>

            <div className = "space-y-3">
                {realResources.map((resource) => (
                    <GcpResources key = {resource.id} resource = {resource} selectedResource = {resourcesSelected.includes(resource.id)} onChecked = {handlingResourceChecks}/>
                ))}
            </div>

            <div className = "space-y-2 pt-4 border-t border-border">
                <div className = "flex items-center gap-2">
                    <Label htmlFor = "ingestionPeriod" className = "text-foreground text-sm font-medium"> Ingestion interval (seconds) </Label>

                    <TooltipProvider>
                        <Tooltip>
                            <TooltipTrigger asChild>
                                <button type = "button" className = "flex items-center justify-center w-5 h-5 rounded-full text-xs text-muted-foreground hover:text-foreground border border-border"> ? </button>
                            </TooltipTrigger>

                            <TooltipContent>
                                <p> Recommended ingestion interval: {recIngestionPeriod}{" "} seconds based on {resourcesSelected.length} selected resources.
                                    Setting the interval to a lower value could incur costs due to API free tier limits. The ingestion interval determines the 
                                    frequency of dashboard timeseries updates. 
                                </p>
                            </TooltipContent>

                        </Tooltip>
                    </TooltipProvider>
                </div>

                <div className = "flex flex-col gap-2 justify-center items-end">
                    <span className = "text-sm font-medium"> {formattingSecond(forIngestionPeriod)} </span>

                    <Slider value = {[Number(forIngestionPeriod)]} onValueChange = {(changeVal) => setForIngestionPeriod(String(changeVal[0]))} min = {60} max = {400} step = {1}/>

                    <p className = "text-sm text-muted-foreground/70"> Recommended: {formattingSecond(recIngestionPeriod)} </p>
                </div>
            </div>
        </StepThree>
    );
}