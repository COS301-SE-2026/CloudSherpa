"use client";

import {Card, CardContent, CardFooter, CardHeader, CardTitle} from "@/components/atoms/card";
import {Button} from "@/components/atoms/button";
import type {CardComponentProps} from "nextstepjs";
import {X} from "lucide-react";
import {Progress} from "@/components/atoms/progress";

/*
- created my own onboading card as the nextstepjs one didnt go with our theme
*/

export default function tourCard({
    step : tourStep, currentStep : currentIndex, totalSteps : allSteps, nextStep  : goToNextStep, prevStep : goToPreviousStep, skipTour : closeTour, arrow

} : Readonly<CardComponentProps>){

    const forProgress = ((currentIndex+1)/allSteps)*100;

    return(
        <>

            {arrow}

            <Card className = "[400px] rounded-xl border border-border bg-popover shadow-2xl">
                <CardHeader className = "flex flex-row items-start justify-between space-y-0 pb-2">
                    <CardTitle className = "text-lg font-semibold text-foreground"> {tourStep.title} </CardTitle>

                    <Button variant = "ghost" size = "icon" onClick = {closeTour} className = "h-8 w-8 rounded-full"> <X className = "h-4 w-4"/> </Button>
                </CardHeader>

                <CardContent className = "space-y-6">
                    <div className = "text-sm leading-6 text-muted-foreground"> {tourStep.content} </div>

                    <Progress value = {forProgress} className = "h-2"/>
                </CardContent>

                <CardFooter className = "flex items-center justify-between border-t border-border pt-4">
                    <span className = "text-xs text-muted-foreground"> Step {currentIndex+1} of {allSteps} </span>

                    <div className = "flex items-center gap-2">
                        <Button variant = "ghost" size = "sm" onClick = {closeTour} > Skip Tour </Button>

                        {currentIndex>0 && (
                            <Button variant = "outline" size = "sm" onClick = {goToPreviousStep} > Previous </Button>
                        )}

                        <Button size = "sm" onClick = {goToNextStep} > {currentIndex === allSteps-1 ? "Finish" : "Next"} </Button>
                    </div>

                </CardFooter>
            </Card>
            
        </>
    );
}