"use client";

import React, { ReactNode } from "react";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/atoms/card";
import { Button } from "@/components/atoms/button";

export interface PropsForStepTwo {
    heading: string;
    description: string;
    onSubmit: (event: React.SubmitEvent<HTMLFormElement>) => void;
    onBack: () => void;
    forLoading: boolean;
    forErrors: string;
    children: ReactNode;
}

export function StepTwo({
    heading,
    description,
    onSubmit,
    onBack,
    forLoading,
    forErrors,
    children,
}: Readonly<PropsForStepTwo>) {
    return (
        <div className="min-h-screen bg-background flex items-center justify-center p-8">
            <Card className="w-full max-w-2xl shadow-none">
                <CardHeader className="pb-2">
                    <div className="flex items-center gap-2 mb-4">
                        <div className="w-2 h-2 rounded-full bg-primary" />

                        <span className="text-sm font-medium text-muted-foreground/70">
                            {" "}
                            STEP 2 OF 3{" "}
                        </span>
                    </div>

                    <CardTitle className="text-2xl font-semibold tracking-tight text-foreground">
                        {" "}
                        {heading}{" "}
                    </CardTitle>

                    <CardDescription className="mt-2 text-muted-foreground/70">
                        {" "}
                        {description}{" "}
                    </CardDescription>

                    {forErrors && (
                        <div className="mt-3 rounded-sm bg-destructive/5 p-3 text-destructive text-sm">
                            {" "}
                            {forErrors}{" "}
                        </div>
                    )}
                </CardHeader>

                <CardContent>
                    <form onSubmit={onSubmit} className="space-y-8">
                        {" "}
                        {children}
                        <div className="flex justify-between pt-4">
                            <Button
                                type="button"
                                disabled={forLoading}
                                onClick={onBack}
                                className="bg-primary hover:bg-accent hover:text-accent-foreground text-primary-foreground px-6 py-2 rounded-md transition-all duration-200 font-medium"
                            >
                                {" "}
                                Back{" "}
                            </Button>

                            <Button
                                type="submit"
                                disabled={forLoading}
                                className="bg-primary hover:bg-accent hover:text-accent-foreground text-primary-foreground px-6 py-2 rounded-md transition-all duration-200 font-medium"
                            >
                                {" "}
                                Next{" "}
                            </Button>
                        </div>
                    </form>
                </CardContent>
            </Card>
        </div>
    );
}
