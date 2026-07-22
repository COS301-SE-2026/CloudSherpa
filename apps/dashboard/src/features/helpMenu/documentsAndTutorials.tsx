"use client";

import {useMemo, useState, type ComponentType} from "react";
import {Search, BookOpen, PlayCircle, Rocket, Plug, Boxes, Clock, ArrowRight, Play} from "lucide-react";
import {Breadcrumb, BreadcrumbList, BreadcrumbItem, BreadcrumbLink, BreadcrumbPage, BreadcrumbSeparator} from "@/components/atoms/breadcrumb";
import {Card, CardContent} from "@/components/atoms/card";
import {Button} from "@/components/atoms/button";
import {Input} from "@/components/atoms/input";
import {Badge} from "@/components/atoms/badge";
import * as TabsPrimitive from "@radix-ui/react-tabs"; //using this instead of tabs bc i want to create my own styling of the tabs

/*
- users should be able to view documents and videos about nav around cloudsherpa
- separated by categories
- can search for smtg in particular
*/

export default function DocumentsAndTutorials(){
    const [search, setSearch] = useState("");
    
    const [activeTab, setActiveTab] = useState<"documents" | "tutorials">("documents");

    return(
        <div className = "min-h-screen bg-background">

            {/* this is for the breadcrumb - to be able to go back to the dashboard */}
            <div className = "border-b border-border px-8 py-5">
                <Breadcrumb>
                    <BreadcrumbList>
                        <BreadcrumbItem>
                            <BreadcrumbLink href = "/" className = "text-[13px] text-muted-foreground"> Dashboard </BreadcrumbLink>
                        </BreadcrumbItem>

                        <BreadcrumbSeparator/>

                        <BreadcrumbItem>
                            <BreadcrumbLink href = "/documentsAndTutorials" className = "text-[13px] text-muted-foreground"> Help Center </BreadcrumbLink>
                        </BreadcrumbItem>

                        <BreadcrumbSeparator/>

                        <BreadcrumbItem>
                            <BreadcrumbPage className = "text-[13px] font-medium text-foreground"> {activeTab === "documents" ? "Documents" : "Tutorials"} </BreadcrumbPage>
                        </BreadcrumbItem>
                    </BreadcrumbList>
                </Breadcrumb>
            </div>

        </div>
    );
}