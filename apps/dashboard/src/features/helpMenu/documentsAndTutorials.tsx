"use client";

import { useMemo, useState, type ComponentType, useEffect, useRef, Suspense } from "react";
import {
    Search,
    BookOpen,
    PlayCircle,
    Rocket,
    Plug,
    Boxes,
    Clock,
    ArrowRight,
    Play,
    X,
} from "lucide-react";
import {
    Breadcrumb,
    BreadcrumbList,
    BreadcrumbItem,
    BreadcrumbLink,
    BreadcrumbPage,
    BreadcrumbSeparator,
} from "@/components/atoms/breadcrumb";
import { Card, CardContent } from "@/components/atoms/card";
import { Button } from "@/components/atoms/button";
import { Input } from "@/components/atoms/input";
import { Badge } from "@/components/atoms/badge";
import * as TabsPrimitive from "@radix-ui/react-tabs"; //using this instead of tabs bc i want to create my own styling of the tabs
import { useRouter, useSearchParams } from "next/navigation";
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "@/components/atoms/dialog";
import {
    TUTFILTERS,
    filterTutorialsByCategory,
    type Tutorials,
} from "@/features/helpMenu/tutorials/tutorials";

/*
- users should be able to view documents and videos about nav around cloudsherpa
- separated by categories
- can search for smtg in particular
*/

interface BrowseCategory {
    id: string;
    label: string;
    description: string;
    href?: string;
    icon: ComponentType<{ className?: string; strokeWidth?: number }>;
}

const BROWSECATEGORIES: BrowseCategory[] = [
    {
        id: "gettingStarted",
        label: "Getting started",
        description: "Get to know CloudSherpa",
        href: "/helpMenu/documents/gettingStarted",
        icon: Rocket,
    },

    {
        id: "connections",
        label: "Connections",
        description: "Connect your AWS account",
        href: "/helpMenu/documents/connections",
        icon: Plug,
    },

    {
        id: "resources",
        label: "Resources",
        description: "Manage your resources",
        href: "/helpMenu/documents/resources",
        icon: Boxes,
    },
];

interface Documents {
    id: string;
    name: string;
    category: string;
    timeToRead: number;
    href: string;
}

const DOCUMENTS: Documents[] = [
    {
        id: "document1",
        name: "Connecting your AWS account",
        category: "Connections",
        timeToRead: 3,
        href: "/helpMenu/documents/connections",
    },

    {
        id: "document2",
        name: "How to manage your reasources",
        category: "Resources",
        timeToRead: 3,
        href: "/helpMenu/documents/resources",
    },
];

type FilterForTutorials = (typeof TUTFILTERS)[number];

function DocumentsAndTutorialsSuspense() {
    const router = useRouter();

    const [search, setSearch] = useState("");

    const searchParameters = useSearchParams();

    const [activeTab, setActiveTab] = useState<"documents" | "tutorials">("documents");

    const [filterTutorials, setFilterTutorials] = useState<FilterForTutorials>("All");

    //added this to hellp correct error
    //will prevent rerendering
    const selectedTab = useRef(false);

    const [videoSelected, setVideoSelected] = useState<Tutorials | null>(null);

    const [videoDialogOpen, setVideoDialogOpen] = useState(false);

    //htmliframeelement rep an html iframe ele & provides type safety
    const youtubeIframe = useRef<HTMLIFrameElement>(null);

    const searchDocument = useMemo(() => {
        if (!search.trim()) {
            return DOCUMENTS;
        }

        const searchQuery = search.toLowerCase();

        return DOCUMENTS.filter(
            (documents) =>
                documents.name.toLowerCase().includes(searchQuery) ||
                documents.category.toLowerCase().includes(searchQuery)
        );
    }, [search]);

    useEffect(() => {
        const tutorialTab = searchParameters.get("tab");

        if (tutorialTab === "tutorials" && !selectedTab.current) {
            setActiveTab("tutorials");

            selectedTab.current = true;
        }
    }, [searchParameters]);

    const filteredTutorials = useMemo(() => {
        const categories = filterTutorialsByCategory(filterTutorials);

        if (!search.trim()) {
            return categories;
        }

        const searchQuery = search.toLowerCase();

        return categories.filter(
            (tutorial) =>
                tutorial.name.toLowerCase().includes(searchQuery) ||
                tutorial.description.toLowerCase().includes(searchQuery)
        );
    }, [search, filterTutorials]);

    const handlingVideoClick = (tutorials: Tutorials) => {
        if (tutorials.videoLink) {
            setVideoSelected(tutorials);
            setVideoDialogOpen(true);
        }
    };

    const handlingVideoClose = () => {
        setVideoDialogOpen(false);
        setVideoSelected(null);
    };

    return (
        <div className="min-h-screen bg-background">
            {/* this is for the video dialog (youtube iframe) */}
            <Dialog open={videoDialogOpen} onOpenChange={handlingVideoClose}>
                <DialogContent className="max-w-3xl p-0 overflow-hidden bg-background">
                    <DialogHeader className="p-4 pb-0">
                        <div className="flex items-center justify-between">
                            <DialogTitle className="text-[16px] font-medium text-foreground">
                                {" "}
                                {videoSelected?.name}{" "}
                            </DialogTitle>

                            <Button
                                variant="ghost"
                                size="icon"
                                className="h-8 w-8 rounded-full hover:bg-muted"
                                onClick={handlingVideoClose}
                            >
                                {" "}
                                <X className="h-4 w-4" strokeWidth={1.75} />{" "}
                            </Button>
                        </div>
                    </DialogHeader>

                    {/* youtube iframe */}
                    <div className="relative aspect-video w-full bg-black">
                        {videoSelected?.videoLink && (
                            <iframe
                                ref={youtubeIframe}
                                src={videoSelected.videoLink}
                                title={videoSelected.name}
                                className="absolute inset-0 h-full w-full"
                                //are the feature policies for the iframe
                                allow="clipboard-write; picture-in-picture"
                                allowFullScreen
                            />
                        )}
                    </div>

                    {videoSelected && (
                        <div className="p-4 pt-3">
                            <p className="text-[13px] text-muted-foreground">
                                {" "}
                                {videoSelected.description}{" "}
                            </p>

                            <div className="mt-2 flex items-center gap-2">
                                <Badge variant="secondary" className="text-[11px]">
                                    {" "}
                                    {videoSelected.category}{" "}
                                </Badge>

                                <span className="text-[12px] text-muted-foreground">
                                    {" "}
                                    {videoSelected.lengthOfVideo}{" "}
                                </span>
                            </div>
                        </div>
                    )}
                </DialogContent>
            </Dialog>

            {/* this is for the breadcrumb - to be able to go back to the dashboard */}
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
                                {activeTab === "documents" ? "Documents" : "Tutorials"}{" "}
                            </BreadcrumbPage>
                        </BreadcrumbItem>
                    </BreadcrumbList>
                </Breadcrumb>
            </div>

            <div className="mx-auto max-w-[820px] px-6 pb-8 pt-16 text-center">
                <div className="mx-auto max-w-[440px]">
                    <h1 className="text-[22px] font-medium text-foreground"> How can we help? </h1>

                    <p className="mt-2 text-[14px] text-muted-foreground">
                        {" "}
                        Search documents and tutorials, or broswe below{" "}
                    </p>
                </div>

                <div className="relative mt-6">
                    <Search
                        className="pointer-events-none absolute left-3 top-1/2 h-3.5 w-3.5 -translate-y-1/2 text-muted-foreground"
                        strokeWidth={1.75}
                    />

                    <Input
                        value={search}
                        onChange={(change) => setSearch(change.target.value)}
                        placeholder="Search Help"
                        className="h-10 border-border bg-muted pl-9 text-[13px]"
                    />
                </div>
            </div>

            {/* these are for the dosc and tutorials tabs */}
            <div className="mx-auto max-w-[820px] px-6">
                <TabsPrimitive.Root
                    value={activeTab}
                    onValueChange={(value) => setActiveTab(value as "documents" | "tutorials")}
                >
                    <TabsPrimitive.List className="flex items-stretch justify-start gap-6 border-b border-border">
                        <TabsPrimitive.Trigger
                            value="documents"
                            className="flex items-center gap-1.5 border-b-2 border-transparent pb-3 text-[13px] font-medium text-muted-foreground transition-colors data-[state=active]:border-primary data-[state=active]:text-foreground"
                        >
                            {" "}
                            <BookOpen className="h-3.5 w-3.5" strokeWidth={1.75} /> Documents{" "}
                        </TabsPrimitive.Trigger>

                        <TabsPrimitive.Trigger
                            value="tutorials"
                            className="flex items-center gap-1.5 border-b-2 border-transparent pb-3 text-[13px] font-medium text-muted-foreground transition-colors data-[state=active]:border-primary data-[state=active]:text-foreground"
                        >
                            {" "}
                            <PlayCircle className="h-3.5 w-3.5" strokeWidth={1.75} /> Tutorials{" "}
                        </TabsPrimitive.Trigger>
                    </TabsPrimitive.List>

                    {/* this is for the docs */}
                    <TabsPrimitive.Content value="documents" className="mt-6 pb-16">
                        <h2 className="mb-3 text-[11px] font-medium uppercase tracking-wider text-muted-foreground">
                            {" "}
                            Browse by category{" "}
                        </h2>

                        <div className="grid grid-cols-3 gap-3">
                            {BROWSECATEGORIES.map((forCategories) => {
                                const Icons = forCategories.icon;

                                return (
                                    <Card
                                        key={forCategories.id}
                                        role="button"
                                        tabIndex={0}

                                        onClick={() => {
                                            if (forCategories.href) {
                                                router.push(forCategories.href);
                                            }
                                        }}

                                        onKeyDown={(change) => {
                                            if (
                                                (change.key === "Enter" || change.key === " ") &&
                                                forCategories.href
                                            ) {
                                                router.push(forCategories.href);
                                            }
                                        }}

                                        className="cursor-pointer border-border bg-muted/40 transition-colors hover:border-primary/50"
                                    >
                                        {" "}
                                        <CardContent className="flex items-start gap-3 p-4">
                                            <span className="flex h-8 w-8 shrink-0 items-center justify-center rounded-md bg-muted text-primary">
                                                {" "}
                                                <Icons
                                                    className="h-4 w-4"
                                                    strokeWidth={1.75}
                                                />{" "}
                                            </span>

                                            <span className="min-w-0">
                                                <span className="block text-[13px] font-medium text-foreground">
                                                    {" "}
                                                    {forCategories.label}{" "}
                                                </span>

                                                <span className="mt-0.5 block text-[12px] text-muted-foreground">
                                                    {" "}
                                                    {forCategories.description}{" "}
                                                </span>
                                            </span>
                                        </CardContent>
                                    </Card>
                                );
                            })}
                        </div>

                        <h2 className="mb-3 mt-8 text-[11px] font-medium uppercase tracking-wider text-muted-foreground">
                            {" "}
                            Popular documents{" "}
                        </h2>

                        {searchDocument.length > 0 && (
                            <div className="grid grid-cols-3 gap-3">
                                <div className="col-span-2 flex flex-col gap-3">
                                    {searchDocument.map((docs) => (
                                        <Card
                                            key={docs.id}
                                            role="button"
                                            tabIndex={0}

                                            onClick={() => router.push(docs.href)}

                                            onKeyDown={(keyPress) => {
                                                if (
                                                    keyPress.key === "Enter" ||
                                                    keyPress.key === " "
                                                ) {
                                                    router.push(docs.href);
                                                }
                                            }}

                                            className="cursor-pointer gap-0 overflow-hidden border-border bg-muted/40 p-0 transition-color hover:border-primary/50"
                                        >
                                            <Button
                                                variant="ghost"
                                                className="h-auto w-full items-center justify-between rounded-none px-4 py-3 text-left hover:bg-transparent"

                                                onClick={(clicked) => {
                                                    clicked.stopPropagation();
                                                    router.push(docs.href);
                                                }}
                                            >
                                                <span className="min-w-0">
                                                    <span className="block text-[13px] font-normal text-foreground">
                                                        {" "}
                                                        {docs.name}{" "}
                                                    </span>
                                                    <span className="mt-0.5 flex items-center gap-2 text-[11.5px] font-normal text-muted-foreground">
                                                        <span> {docs.category} </span>
                                                        <span> &middot; </span>

                                                        <span className="flex items-center gap-1">
                                                            {" "}
                                                            <Clock
                                                                className="h-3 w-3"
                                                                strokeWidth={1.75}
                                                            />{" "}
                                                            {docs.timeToRead}min read{" "}
                                                        </span>
                                                    </span>
                                                </span>

                                                <ArrowRight
                                                    className="h-3.5 w-3.5 shrink-0 text-muted-foreground"
                                                    strokeWidth={1.75}
                                                />
                                            </Button>
                                        </Card>
                                    ))}
                                </div>
                            </div>
                        )}
                    </TabsPrimitive.Content>

                    {/* this is for the tut tabs */}
                    <TabsPrimitive.Content value="tutorials" className="mt-6 pb-16">
                        <div className="mb-6 flex items-center gap-1.5">
                            {TUTFILTERS.map((filtered) => {
                                const activeTabs = filterTutorials === filtered;

                                return (
                                    <Button
                                        key={filtered}
                                        size="sm"
                                        variant={activeTabs ? "default" : "secondary"}
                                        className="h-auto rounded-full px-3 py-1.5 text-[12px] font-medium"
                                        onClick={() => setFilterTutorials(filtered)}
                                    >
                                        {" "}
                                        {filtered}{" "}
                                    </Button>
                                );
                            })}
                        </div>

                        <div className="grid grid-cols-3 gap-4">
                            {filteredTutorials.map((tuts) => (
                                <Card
                                    key={tuts.id}
                                    role="button"
                                    tabIndex={0}

                                    onClick={() => handlingVideoClick(tuts)}
                                    onKeyDown={(pressingButton) => {
                                        if (
                                            pressingButton.key === "Enter" ||
                                            pressingButton.key === " "
                                        ) {
                                            handlingVideoClick(tuts);
                                        }
                                    }}

                                    className="cursor-pointer gap-0 overflow-hidden border-border p-0 transition-colors hover:border-primary/50"
                                >
                                    <div className="relative flex h-[110px] items-center justify-center bg-muted-foreground/10">
                                        {/* adding thumbnail for the tut videos */}
                                        {tuts.thumbNail && (
                                            <img
                                                src={tuts.thumbNail}
                                                alt={tuts.name}
                                                className="h-full w-full object-cover"
                                            />
                                        )}

                                        <span className="absolute inset-0 flex items-center justify-center bg-black/20 transition-opacity hover:bg-black/30">
                                            <span className="flex h-9 w-9 items-center justify-center rounded-full bg-background/80 transition-transform group-hover:scale-110">
                                                {" "}
                                                <Play
                                                    className="h-4 w-4 fill-foreground text-foreground"
                                                    strokeWidth={0}
                                                />{" "}
                                            </span>
                                        </span>

                                        <Badge
                                            variant="secondary"
                                            className="absolute bottom-2 right-2 text-[10px]"
                                        >
                                            {" "}
                                            {tuts.lengthOfVideo}{" "}
                                        </Badge>
                                    </div>

                                    <CardContent className="bg-muted/40 px-3.5 py-3">
                                        <p className="text-[13px] font-medium text-foreground">
                                            {" "}
                                            {tuts.name}{" "}
                                        </p>

                                        <p className="mt-1 text-[12px] leading-snug text-muted-foreground">
                                            {" "}
                                            {tuts.description}{" "}
                                        </p>
                                    </CardContent>
                                </Card>
                            ))}

                            {filteredTutorials.length === 0 && (
                                <p className="col-span-3 py-8 text-center text-[12.5px] text-muted-foreground">
                                    {" "}
                                    No tutorials available.{" "}
                                </p>
                            )}
                        </div>
                    </TabsPrimitive.Content>
                </TabsPrimitive.Root>
            </div>
        </div>
    );
}

//the errror suggested that the useSearch should be wrapped in suspense
//it will help by displaying a fallback when the content is loading (checking if it will work)
export default function DocumentsAndTutorials() {
    return (
        <Suspense fallback={null}>
            <DocumentsAndTutorialsSuspense />
        </Suspense>
    );
}
