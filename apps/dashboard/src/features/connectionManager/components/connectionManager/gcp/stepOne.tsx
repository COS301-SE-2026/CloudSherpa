"use client";

import React, {useState, useRef} from "react";
import {StepOne} from "@/features/connectionManager/components/connectionManager/wizardSetup/stepOne";
import {UploadCloud, FileJson, X} from "lucide-react";
import {Label} from "@/components/atoms/label";
import {Input} from "@/components/atoms/input";

interface StepOnePropsForGcp{
    onNext : (data : {
        name : string;
        accountKey : string
    }) => void;
}

export default function StepOneGcp({
    onNext
} : Readonly<StepOnePropsForGcp>){
    const [name, setName] = useState("");

    const [accountKey, setAccountKey] = useState<File | null>(null);

    const [draggingFile, setDraggingFile] = useState(false);

    const [errors, setErrors] = useState("");

    const [isSubmitting, setIsSubmitting] = useState(false);

    const inputForFile = useRef<HTMLInputElement>(null);

    const readingFile = async (forFile : File) : Promise<string> => {
        return await forFile.text();
    }

    const acceptingFile = (forFile : File | undefined) => {
        if(!forFile){
            return;
        }

        if(forFile.type !== "application/json" && !forFile.name.endsWith(".json")){
            setErrors("Please upload a JSON service account key file.");

            return;
        }

        setErrors("");

        setAccountKey(forFile);
    };

    const handlingDroppingFile = (dropFile : React.DragEvent<HTMLButtonElement>) => {
        dropFile.preventDefault();
        setDraggingFile(false);

        acceptingFile(dropFile.dataTransfer.files?.[0]);
    };

    const handlingSubmit = async (submittingFile : React.SubmitEvent<HTMLFormElement>) => {
        submittingFile.preventDefault();

        if(!accountKey){
            setErrors("Please upload a service account key before continuing");

            return;
        }

        try{
            setIsSubmitting(true);
            const serviceAccountKeyJson = await readingFile(accountKey);

            onNext({name, accountKey : serviceAccountKeyJson});
        } catch{
            setErrors("Failed to load account key");
        } finally{
            setIsSubmitting(false);
        }
    };

    return(
        <StepOne
            heading = "Link your GCP account"
            description = "Please download the service account key as a JSON file and upload it below to begin connecting your account"
            onSubmit = {handlingSubmit} isSubmitting = {isSubmitting}
        >
            
            <div className = "space-y-2">
                <Label htmlFor = "name" className = "text-foreground text-sm font-medium"> Account name </Label>

                <Input id = "name" type = "text" placeholder = "Connection name" value = {name} onChange = {(changing) => setName(changing.target.value)}
                       className = "bg-background border-border rounded-md px-4 py-3 text-foreground placeholder:text-muted-foreground/40 focus:outline-none focus:ring-2 focus:ring-ring focus:border-transparent transition-all w-full" required/>
            </div>

            <div className = "space-y-2">
                <Label className = "text-foreground text-sm font-medium"> Service account key </Label>

                <button type = "button"

                        onDragOver = {(dragging) => {
                            dragging.preventDefault();
                            setDraggingFile(true);
                        }}

                        onDragLeave = {() => setDraggingFile(false)}
                        onDrop = {handlingDroppingFile}
                        onClick = {() => inputForFile.current?.click()}

                        onKeyDown = {(pressKey) => {
                            if(pressKey.key === 'Enter' || pressKey.key === ' '){
                                pressKey.preventDefault();
                                inputForFile.current?.click();
                            }
                        }}

                        className = {`flex flex-col items-center justify-center gap-3 rounded-lg border-2 border-dashed p-10 text-center cursor-pointer transition-all w-full ${
                            draggingFile ? "border-primary bg-primary/5" : "border-border bg-background hover:border-primary/40"
                        } focus:outline-none`}
                    >

                        <input ref = {inputForFile} type = "file" accept = "application/json,.json" className = "hidden" onChange = {(change) => acceptingFile(change.target.files?.[0])}/>

                        {accountKey ? (
                            <>
                                <FileJson className = "w-8 h-8 text-primary"/>

                                <div className = "flex items-center gap-2">
                                    <span className = "text-foreground text-sm font-medium"> {accountKey.name} </span>

                                    <button type = "button"
                                            onClick = {(click) => {
                                                click.stopPropagation();
                                                setAccountKey(null);
                                            }}
                                            className = "text-muted-foreground hover:text-foreground transition-colors"
                                    >
                                        <X className = "w-4 h-4"/>

                                    </button>

                                </div>
                            </>
                        ) : (
                            <>
                                <UploadCloud className = "w-8 h-8 text-primary"/>

                                <p className = "text-sm text-muted-foreground/70"> drag and drop your file here <br/> or </p>

                                <button type = "button" className = "bg-foreground text-background hover:bg-primary hover:text-background px-4 py-2 rounded-md text-sm font-medium transition-colors"> browse files </button>
                            </>
                        )}
                    </button>

                    {errors && <p className = "text-sm text-destructive"> {errors} </p>}
            </div>
        </StepOne>
    );
}