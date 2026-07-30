"use client";

import {
    Breadcrumb,
    BreadcrumbItem,
    BreadcrumbLink,
    BreadcrumbList,
    BreadcrumbPage,
    BreadcrumbSeparator,
} from "@/components/atoms/breadcrumb";
import { LucideIcon } from "lucide-react";
import { Card, CardContent } from "@/components/atoms/card";

/*
- created this pgae to help with the code duplication in the docs pages
*/

interface Instructions {
    id: string;
    name: string;
    description: string;
    details: string[];
}

interface InstructionPagesProps {
    name: string;
    description: string;
    breadcrumb: string;
    icon: LucideIcon;
    instructions: Instructions[];
}

export default function HelpCenter({
    name,
    description,
    breadcrumb,
    icon: Icons,
    instructions,
}: Readonly<InstructionPagesProps>) {
    return (
        <div className="min-h-screen bg-background">
            <div className="border-b border-border px-8 py-5">
                <Breadcrumb>
                    <BreadcrumbList>
                        <BreadcrumbItem>
                            <BreadcrumbLink
                                href="/dashboard"
                                className="text-[13px] text-muted-foreground"
                            >
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
                                {breadcrumb}{" "}
                            </BreadcrumbPage>
                        </BreadcrumbItem>
                    </BreadcrumbList>
                </Breadcrumb>
            </div>

            <div className="mx-auto max-w-[720px] px-6 pb-20 pt-12">
                <div className="mb-10 flex items-center gap-4">
                    <div className="flex h-12 w-12 items-center justify-center rounded-lg bg-muted text-primary">
                        {" "}
                        <Icons className="h-6 w-6" strokeWidth={1.75} />{" "}
                    </div>

                    <div>
                        <h1 className="text-[24px] font-medium text-foreground"> {name} </h1>

                        <p className="mt-1 text-[14px] text-muted-foreground"> {description} </p>
                    </div>
                </div>

                <div className="flex flex-col gap-4">
                    {instructions.map((instruction, i) => (
                        <Card
                            key={instruction.id}
                            className="gap-0 overflow-hidden border-border bg-muted/40 p-0"
                        >
                            <CardContent className="p-5">
                                <div className="flex items-start gap-4">
                                    <div className="flex flex-col items-center">
                                        <span className="flex h-8 w-8 items-center justify-center rounded-full bg-muted text-[12px] font-medium text-primary">
                                            {" "}
                                            {i + 1}{" "}
                                        </span>

                                        {i < instructions.length - 1 && (
                                            <span className="mt-2 h-full w-px flex-1 bg-border" />
                                        )}
                                    </div>

                                    <div className="flex-1">
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
                                                    className="flex items-stat gap-2 text-[12.5px] text-muted-foreground"
                                                >
                                                    <span className="mt-1.5 h-1 w-1 rounded-full bg-muted-foreground/60" />

                                                    <span> {detail} </span>
                                                </li>
                                            ))}
                                        </ul>
                                    </div>
                                </div>
                            </CardContent>
                        </Card>
                    ))}
                </div>
            </div>
        </div>
    );
}
