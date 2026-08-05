"use client";

import React, {useState, useEffect} from "react";
import {StepTwo} from "@/features/connectionManager/components/connectionManager/wizardSetup/stepTwo";
import {Button} from "@/components/atoms/button";

interface DetailsForResource{
    id : string;
    name : string;
    type : string;
}

interface DetailsForGcp{
    accountKey : string;
}

interface StepTwoPropsForGcp{
    credentials : DetailsForGcp | null;
    onNext : (forData : {
        servicesSelected : string[];
        resources : DetailsForResource[]}) => void;
    
    onBack : () => void;
}

const HARDCODEDSERVICES = [
    {id : "service1",
     name : "Service one"
    },
];

const HARDCODEDPERMISSIONS : Record<string, string[]> = {
    service1 : [
        "Permission one",
        "Permission two",
    ],
};

export default function StepTwoGcp({
    onNext, onBack
} : Readonly<StepTwoPropsForGcp>){
    const [servicesAvailable, setServicesAvailable] = useState<{id : string; name : string}[]>([]);

    const [selectedServices, setSelectedServices] = useState<string[]>([]);

    const [permissions, setPermissions] = useState<string[]>([]);

    const [forLoading, setForLoading] = useState(false);

    const [errors, setErrors] = useState("");

    useEffect(() => {
        setServicesAvailable(HARDCODEDSERVICES);
    }, []);

    useEffect(() => {
        if(selectedServices.length === 0){
            setPermissions([]);

            return;
        }

        const permissionsSelected = selectedServices.flatMap((idForService) => HARDCODEDPERMISSIONS[idForService] || []);

        setPermissions(permissionsSelected);

    }, [selectedServices]);

    const checkingService = (idForService : string) => {
        setSelectedServices((previous) => previous.includes(idForService) ? previous.filter((id) => id !== idForService) : [...previous, idForService]);
    };

    const handlingSubmit = async (submitting : React.SubmitEvent<HTMLFormElement>) => {
        submitting.preventDefault();

        try{
            setForLoading(true);
            setErrors("");

            const resourcesDiscovered : DetailsForResource[] = [
                {id : "gcp-resource-1",
                 name : "Resource one",
                 type : "service1",
                },
            ];

            onNext({servicesSelected : selectedServices, resources : resourcesDiscovered});

        } catch(error){
            setErrors("Failed to discover any resources");
        } finally{
            setForLoading(false);
        }

    };

    const handlingSelectedAll = () => {
        if(selectedServices.length == servicesAvailable.length){
            setSelectedServices([]);
        } else{
            setSelectedServices(servicesAvailable.map((services) => services.id));
        }
    };

    return(
        <StepTwo heading = "Select service"
                 description = "Choose which GCP service you want to monitor."
                 onSubmit = {handlingSubmit} onBack = {onBack} forLoading = {forLoading} forErrors = {errors}
        >
            <section>

                <div className = "flex flex-wrap items-center justify-between gap-2 mb-4">
                    <h3 className = "text-foreground text-sm font-semibold uppercase tracking-wider opacity-80"> Services we offer </h3>

                    <Button type = "button" variant = "ghost" size = "sm" onClick = {handlingSelectedAll} className = "text-primary hover:text-accent text-sm transition-colors px-0">
                        {selectedServices.length === servicesAvailable.length ? "Deselect All" : "Select All"}
                    </Button>
                </div>

                <div className = "space-y-3">
                    {servicesAvailable.map((service) => (
                        <label key = {service.id} className = "flex items-center gap-3 w-full p-4 bg-background rounded-lg border border-border hover:border-primary/40 transition-all cursor-pointer focus-within:ring-2 focus-within:ring-primary focus-within:ring-offset-2">
                            <input type = "checkbox" checked = {selectedServices.includes(service.id)} onChange = {() => checkingService(service.id)} className = "w-4 h-4 rounded border-border bg-background text-primary focus:ring-2 focus:ring-primary"/>

                            <span className = "text-foreground font-medium"> {service.name} </span>
                        </label>
                    ))}
                </div>

            </section>

            <section>
                <h3 className = "text-foreground text-sm font-semibold uppercase tracking-wider opacity-80 mb-4"> Permissions </h3>

                <div className = "rounded-lg border border-border bg-background p-4 space-y-3">
                    {permissions.length === 0 ? (
                        <p className = "text-sm text-muted-foreground/70"> Select a service to view the permissions </p>
                    ) : (
                        permissions.map((permissions) => (
                            <div key = {permissions} className = "rounded-md bg-card px-4 py-3 text-sm text-foreground"> - {permissions} </div>
                        ))
                    )}
                </div>
            </section>
            
        </StepTwo>
    );
}