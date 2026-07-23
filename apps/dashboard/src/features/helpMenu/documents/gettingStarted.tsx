"use client";

import {
    Breadcrumb,
    BreadcrumbList,
    BreadcrumbItem,
    BreadcrumbLink,
    BreadcrumbPage,
    BreadcrumbSeparator,
} from "@/components/atoms/breadcrumb";
import { Card, CardContent } from "@/components/atoms/card";
import { Rocket } from "lucide-react";

/*
-this page should help people with navigating around ClourSherpa if they are not familiar with it
*/

interface Instructions {
    id: string;
    name: string;
    description: string;
    details: string[];
}

const INS: Instructions[] = [
    {
        id: "ins1",
        name: "Create your account",
        description: "Sign up and log in to CloudSherpa for the first time",
        details: [
            "Go to the signup page and create your CloudSherpa account",
            "Enter your credentials",
            "Log in and you will land on an empty dashboard and can thereafter utilize CloudSherpas features",
        ],
    },

    {
        id: "ins2",
        name: "Explore your dashboard",
        description: "Monitor data laid out across widgets",
        details: [
            "You can add widgets that you would like to monitor which are represented in charts",
            "Use the period selector to monitor is specific time range",
            "Click edit to rearrange widget or to add new widgets",
        ],
    },
];

export default function GettingStarted() {
    return (
        <div className="min-h-screen bg-background">
            <div className="border-b border-border px-8 py-5">
                <Breadcrumb>
                    <BreadcrumbList>
                        <BreadcrumbItem>
                            <BreadcrumbLink href="/" className="text-[13px] text-muted-foreground">
                                {" "}
                                Dashboard{" "}
                            </BreadcrumbLink>
                        </BreadcrumbItem>

                        <BreadcrumbSeparator />

                        <BreadcrumbItem>
                            <BreadcrumbLink
                                href="/helpMenu/documentsAndTutorials"
                                className="text-[13px] text-muted-foreground"
                            >
                                {" "}
                                Help Center{" "}
                            </BreadcrumbLink>
                        </BreadcrumbItem>

                        <BreadcrumbSeparator />

                        <BreadcrumbItem>
                            <BreadcrumbPage className="text-[13px] font-medium text-foreground">
                                {" "}
                                Getting started{" "}
                            </BreadcrumbPage>
                        </BreadcrumbItem>
                    </BreadcrumbList>
                </Breadcrumb>
            </div>

            <div className="mx-auto max-w-[720px] px-6 pb-20 pt-12">
                <div className="mb-10 flex items-center gap-4">
                    <div className="flex h-12 w-12 shrink-0 items-center justify-center rounded-lg bg-muted text-primary">
                        {" "}
                        <Rocket className="h-6 w-6" strokeWidth={1.75} />{" "}
                    </div>

                    <div>
                        <h1 className="text-[24px] font-medium text-foreground">
                            {" "}
                            Getting started with CloudSherpa{" "}
                        </h1>

                        <p className="mt-1 text-[14px] text-muted-foreground">
                            {" "}
                            Here are some easy steps to follow to navigate around CloudSherpa{" "}
                        </p>
                    </div>
                </div>

                <div className="flex flex-col gap-4">
                    {INS.map((instruction, i) => {
                        return (
                            <Card
                                key={instruction.id}
                                className="gap-0 overflow-hidden border-border bg-muted/40 p-0"
                            >
                                <CardContent className="p-5">
                                    <div className="flex items-start gap-4">
                                        <div className="flex flex-col items-center">
                                            <span className="flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-muted text-[12px] font-medium text-primary">
                                                {" "}
                                                {i + 1}{" "}
                                            </span>

                                            {i < INS.length - 1 && (
                                                <span className="mt-2 h-full w-px flex-1 bg-border" />
                                            )}
                                        </div>

                                        <div className="min-w-0 flex-1 pb-1">
                                            <h2 className="text-[15px] font-medium text-foreground">
                                                {" "}
                                                {instruction.name}{" "}
                                            </h2>

                                            <p className="mt-1 text-[13px] text-muted-foreground">
                                                {" "}
                                                {instruction.description}{" "}
                                            </p>

                                            <ul className="mt-3 flex flex-col gap-2">
                                                {instruction.details.map((detail) => (
                                                    <li
                                                        key={detail}
                                                        className="flex items-start gap-2 text-[12.5px] text-muted-foreground"
                                                    >
                                                        <span className="mt-1.5 h-1 w-1 shrink-0 rounded-full bg-muted-foreground/60" />

                                                        <span> {detail} </span>
                                                    </li>
                                                ))}
                                            </ul>
                                        </div>
                                    </div>
                                </CardContent>
                            </Card>
                        );
                    })}
                </div>
            </div>
        </div>
    );
}
