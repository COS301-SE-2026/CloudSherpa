"use client";

import React, {useState} from "react";
import {StepThree} from "@/features/connectionManager/components/connectionManager/wizardSetup/stepThree";
import {Button} from "@/components/atoms/button";

interface DetailsForResource{
    id : string;
    name : string;
    type?: string;
}

interface StepThreePropsForGcp{
    name : string;
    resources?: DetailsForResource[];
    onComplete : () => void;
    onBack : () => void;
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
    resources = [], onComplete, onBack,
} : Readonly<StepThreePropsForGcp>){
    const [resourcesSelected, setResourcesSelected] = useState<string[]>([]);

    const [forSaving, setForSaving] = useState(false);

    const [errors, setErrors] = useState<string | null>(null);

    const hardCodedResources : DetailsForResource[] = [
        {id : "resource1",
         name : "Resource one",
         type : "Service one",
        },
    ];

    const realResources = resources && resources.length > 0 ? resources : hardCodedResources;

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
            onComplete();
        } catch(forError){
            setErrors("Unable to complete GCP connection setup");
        } finally{
            setForSaving(false);
        }
    };

    return(
        <StepThree heading = "Select instances"
                   description = "Select the instance you want CloudSherpa to monitor"
                   onSubmit = {handlingSubmit} onBack = {onBack} forSaving = {forSaving} forErrors = {errors}
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
        </StepThree>
    );
}