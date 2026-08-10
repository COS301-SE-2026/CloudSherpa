"use client";

import React, {ReactNode} from "react";
import {Card, CardContent, CardHeader, CardTitle, CardDescription} from "@/components/atoms/card";
import {Button} from "@/components/atoms/button";

interface PropsForStepOne{
    heading : string;
    description : string;
    onSubmit : (event : React.SubmitEvent<HTMLFormElement>) => void;
    isSubmitting?: boolean;
    children : ReactNode;
}

export function StepOne({
    heading, description, onSubmit, isSubmitting = false, children,
} : Readonly<PropsForStepOne>){
    return(
        <div className = "min-h-screen bg-background flex items-center justify-center p-8">
            <Card className = "w-full max-w-2xl shadow-none">
                <CardHeader className = "pb-2">
                    <div className = "flex items-center gap-2 mb-4">
                        <div className = "w-2 h-2 rounded-full bg-primary"/>

                        <span className = "text-sm font-medium text-muted-foreground/70"> STEP 1 OF 3 </span>
                    </div>

                    <CardTitle className = "text-2xl font-semibold tracking-tight text-foreground"> {heading} </CardTitle>

                    <CardDescription className = "mt-2 text-muted-foreground/70"> {description} </CardDescription>

                </CardHeader>

                <CardContent>
                    <form onSubmit = {onSubmit} className = "space-y-6"> {children}
                        <div className = "flex justify-end pt-4">
                            <Button type = "submit" disabled = {isSubmitting} className = "bg-primary hover:bg-accent hover:text-accent-foreground text-primary-foreground px-6 py-2 rounded-md transition-all duration-200 font-medium"> {isSubmitting ? "Processing..." : "Next"} </Button>
                        </div>
                    </form>
                </CardContent>
            </Card>
        </div>
    );
}