"use client";

import React, {ReactNode} from "react";
import {Card, CardContent, CardHeader, CardTitle, CardDescription} from "@/components/atoms/card";
import {Button} from "@/components/atoms/button";

export interface PropsForStepThree{
    heading : string;
    description : string;
    onSubmit : (event : React.SubmitEvent<HTMLFormElement>) => void;
    onBack : () => void;
    forSaving : boolean;
    forErrors : string | null;
    children : ReactNode;
}

export function StepThree({
    heading, description, onSubmit, onBack, forSaving, forErrors, children,
} : Readonly<PropsForStepThree>){
    return(
        <div className = "min-h-screen bg-background flex items-center justify-center p-8">
            <Card className = "w-full max-w-2xl shadow-none">
                <CardHeader className = "pb-2">
                    <div className = "flex items-center gap-2 mb-4">
                        <div className = "w-2 h-2 rounded-full bg-primary"/>

                        <span className = "text-sm font-medium text-muted-foreground/70"> STEP 3 OF 3 </span>
                    </div>

                    <CardTitle className = "text-2xl font-semibold tracking-tight text-foreground"> {heading} </CardTitle>

                    <CardDescription className = "mt-2 text-muted-foreground/70"> {description} </CardDescription>

                </CardHeader>

                <CardContent>
                    <form onSubmit = {onSubmit} className = "space-y-8"> {children}
                        {forErrors && (
                            <div className = "rounded-md border border-red-500 bg-red-50 p-3 text-sm text-red-700"> {forErrors} </div>
                        )}

                        <div className = "flex justify-between pt-6">
                            <Button type = "button" disabled = {forSaving} onClick = {onBack} className = "bg-primary hover:bg-accent hover:text-accent-foreground text-primary-foreground px-6 rounded-md transition-all duration-200 font-medium"> Back </Button>

                            <Button type = "submit" disabled = {forSaving} className = "bg-primary hover:bg-accent hover:text-accent-foreground text-primary-foreground px-8 py-2 rounded-md transition-all duration-200 font-medium"> {forSaving ? "Saving..." : "Finish"} </Button>
                        </div>

                    </form>
                </CardContent>
            </Card>
        </div>
    );
}