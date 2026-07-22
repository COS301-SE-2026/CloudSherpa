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

    const searchDocument = useMemo(() => {
        if(!search.trim()){
            return DOCUMENTS;
        }

        const searchQuery = search.toLowerCase();

        return DOCUMENTS.filter((documents) => documents.name.toLowerCase().includes(searchQuery) || documents.category.toLowerCase().includes(searchQuery));

    }, [search]);

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

            {/* these are for the dosc and tutorials tabs */}
            <div className = "mx-auto max-w-[820px] px-6">
                <TabsPrimitive.Root value = {activeTab} onValueChange = {(value) => setActiveTab(value as "documents" | "tutorials")}>
                    <TabsPrimitive.List className = "flex items-stretch justify-start gap-6 border-b border-border">
                        <TabsPrimitive.Trigger value = "documents" className = "flex items-center gap-1.5 border-b-2 border-transparent pb-3 text-[13px] font-medium text-muted-foreground transition-colors data-[state=active]:border-primary data-[state=active]:text-foreground"> <BookOpen className = "h-3.5 w-3.5" strokeWidth = {1.75} /> Documents </TabsPrimitive.Trigger>

                        <TabsPrimitive.Trigger value = "tutorials" className = "flex items-center gap-1.5 border-b-2 border-transparent pb-3 text-[13px] font-medium text-muted-foreground transition-colors data-[state=active]:border-primary data-[state=active]:text-foreground"> <PlayCircle className = "h-3.5 w-3.5" strokeWidth = {1.75} /> Tutorials </TabsPrimitive.Trigger>
                    </TabsPrimitive.List>

                {/* this is for the docs */}
                <TabsPrimitive.Content value = "documents" className = "mt-6 pb-16">
                    <h2 className = "mb-3 text-[11px] font-medium uppercase tracking-wider text-muted-foreground"> Browse by category </h2>

                    <div className = "grid grid-cols-3 gap-3">
                        {BROWSECATEGORIES.map((forCategories) => {
                            const Icons = forCategories.icon;

                            return(
                                <Card key = {forCategories.id} role = "button" tabIndex = {0} className = "cursor-pointer border-border bg-muted/40 transition-colors hover:border-primary/50">
                                    <CardContent className = "flex items-start gap-3 p-4">
                                        <span className = "flex h-8 w-8 shrink-0 items-center justify-center rounded-md bg-muted text-primary"> <Icons className = "h-4 w-4" strokeWidth = {1.75}/> </span>

                                        <span className = "min-w-0">
                                            <span className = "block text-[13px] font-medium text-foreground"> {forCategories.label} </span>

                                            <span className = "mt-0.5 block text-[12px] text-muted-foreground"> {forCategories.description} </span>
                                        </span>
                                    </CardContent>
                                </Card>
                            );
                        })}
                    </div>

                    <h2 className = "mb-3 mt-8 text-[11px] font-medium uppercase tracking-wider text-muted-foreground"> Popular documents </h2>

                    {searchDocument.length > 0 && (
                        <div className = "grid grid-cols-3 gap-3">
                            <div className = "col-span-2 flex flex-col gap-3">
                                {searchDocument.map((docs) => (
                                    <Card key = {docs.id} className = "cursor-pointer gap-0 overflow-hidden border-border bg-muted/40 p-0 transition-color hover:border-primary/50">
                                        <Button variant = "ghost" className = "h-auto w-full items-center justify-between rounded-none px-4 py-3 text-left hover:bg-transparent">

                                            <span className = "min-w-0">
                                                <span className = "block text-[13px] font-normal text-foreground"> {docs.name} </span>
                                                <span className = "mt-0.5 flex items-center gap-2 text-[11.5px] font-normal text-muted-foreground">
                                                    <span> {docs.category} </span>
                                                    <span> &middot; </span>

                                                    <span className = "flex items-center gap-1"> <Clock className = "h-3 w-3" strokeWidth = {1.75}/> {docs.timeToRead}min read </span>
                                                </span>
                                            </span>

                                            <ArrowRight className = "h-3.5 w-3.5 shrink-0 text-muted-foreground" strokeWidth = {1.75}/>

                                        </Button>
                                    </Card>
                                ))}
                            </div>
                        </div>
                    )}

                </TabsPrimitive.Content>

                </TabsPrimitive.Root>
            </div>

        </div>
    );
}