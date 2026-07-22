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

interface BrowseCategory{
    id : string;
    label : string;
    description : string;
    icon : ComponentType <{className? : string; strokeWidth? : number}>;
}

const BROWSECATEGORIES : BrowseCategory[] = [
    {id : "gettingStarted", label : "Getting started",
     description : "Get to know CloudSherpa", icon : Rocket
    },

    {id : "connections", label : "Connections",
     description : "Connect your AWS account", icon : Plug
    },

    {id : "resources", label : "Resources",
     description : "Manage your resources", icon : Boxes
    },
];

interface Documents{
    id : string;
    name : string;
    category : string;
    timeToRead : number;
}

const DOCUMENTS : Documents[] = [
    {id : "document1", name : "Connecting your AWS account",
     category : "Connections", timeToRead : 3
    },

    {id : "document2", name : "How to manage your reasources",
     category : "Resources", timeToRead : 3
    },
];

const TUTFILTERS = ["All", "Getting started", "Connections", "Resources"] as const;

type FilterForTutorials = (typeof TUTFILTERS)[number];

interface Tutorials{
    id : string;
    name : string;
    description : string;
    category : Exclude<FilterForTutorials, "All">;
    lengthOfVideo : string;
}

const TUTORIALS : Tutorials[] = [
    {id : "tutorial1", name : "Getting started with CloudSherpa",
     description : "Learn by watching how to navigate about CloudSherpa", category : "Getting started",
     lengthOfVideo : "1:50",
    },

    {id : "tutorial2", name : "Managing your AWS connections",
     description : "Connect your first cloud provider & explore the dashboard", category : "Connections",
     lengthOfVideo : "1:50",
    },

    {id : "tutorial3", name : "Configuring your resources",
     description : "Add and remove resources for a specific connection", category : "Resources",
     lengthOfVideo : "1:50",
    },
];

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

            <div className = "mx-auto max-w-[820px] px-6 pb-8 pt-16 text-center">
                <div className = "mx-auto max-w-[440px]">
                    <h1 className = "text-[22px] font-medium text-foreground"> How can we help? </h1>

                    <p className = "mt-2 text-[14px] text-muted-foreground"> Search documents and tutorials, or broswe below </p>
                </div>

                <div className = "relative mt-6">
                    <Search className = "pointer-events-none absolute left-3 top-1/2 h-3.5 w-3.5 -translate-y-1/2 text-muted-foreground" strokeWidth = {1.75} />

                    <Input value = {search} onChange = {(change) => setSearch(change.target.value)} placeholder = "Search Help" className = "h-10 border-border bg-muted pl-9 text-[13px]" />
                </div>
            </div>

        </div>
    );
}