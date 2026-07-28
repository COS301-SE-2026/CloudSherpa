"use client";

/*
- users should be able to add connections
- when adding connections, they should be redirected to the wizard setup
- they should be able to view the resources assoc with that connection and delete connections
*/

import { useEffect, useState } from "react";
import { Trash2, ArrowLeft, SlidersHorizontal, Search, MoreVertical } from "lucide-react";
import { Card, CardContent } from "@/components/atoms/card";
import { Button } from "@/components/atoms/button";
import { Tabs, TabsList, TabsTrigger } from "@/components/atoms/tabs";
import { Separator } from "@/components/atoms/separator";
import { Badge } from "@/components/atoms/badge";
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuTrigger,
} from "@/components/atoms/dropdown-menu";

import { Eye, Pencil } from "lucide-react";
import {
    CloudAccount,
    getAwsAccountConnections,
    getAwsAccountResourceCount,
    deleteAwsAccount,
} from "@/lib/fetch/aws-connection-api";
import { useRouter } from "next/navigation";
import {
    AlertDialog,
    AlertDialogAction,
    AlertDialogCancel,
    AlertDialogContent,
    AlertDialogDescription,
    AlertDialogFooter,
    AlertDialogHeader,
    AlertDialogTitle,
    AlertDialogTrigger,
} from "@/components/atoms/alert-dialog";

type Providers = "All" | "AWS" | "Azure" | "GCP";
interface Connections {
    id: string;
    name: string;
    provider: Exclude<Providers, "All">;
    detail: string;
    resource: number;
    status: "active" | "inactive";
}

//will use these colours for the tabs for now, will add the company colours to the style dictionary and will update it
const providerTabs: Record<Providers, { active: string; inactive: string }> = {
    All: {
        active: "bg-primary text-primary-foreground hover:bg-primary/90",
        inactive: "bg-muted text-muted-foreground hover:bg-muted/80",
    },

    AWS: {
        active: "bg-primary text-primary-foreground hover:bg-primary/90",
        inactive: "bg-muted text-muted-foreground hover:bg-muted/80",
    },

    Azure: {
        active: "bg-primary text-primary-foreground hover:bg-primary/90",
        inactive: "bg-muted text-muted-foreground hover:bg-muted/80",
    },

    GCP: {
        active: "bg-primary text-primary-foreground hover:bg-primary/90",
        inactive: "bg-muted text-muted-foreground hover:bg-muted/80",
    },
};

const badges = (provider: Exclude<Providers, "All">) => {
    return providerTabs[provider].active;
};

export default function ManagingConnections() {
    const [connections, setConnections] = useState<Connections[]>([]);
    const [activeFilter, setActiveFilter] = useState<Providers>("All");
    const router = useRouter();

    const handleViewDetails = (connectionId: string) => {
        router.push(`/manageConnections/${connectionId}`);
    };

    const handleEditResources = (connectionId: string) => {
        router.push(`/manageConnections/${connectionId}/resources`);
    };
    const filtered =
        activeFilter === "All"
            ? connections
            : connections.filter(
                  (filteredConnections) => filteredConnections.provider === activeFilter
              );

    const handleDeletion = async (id: string) => {
        try {
            await deleteAwsAccount(id);
            await loadConnections();
        } catch (error) {
            console.error("Failed to delete account", error);
        }
    };

    function formatIngestionPeriod(seconds: number): string {
        const days = Math.floor(seconds / 86400);
        seconds %= 86400;

        const hours = Math.floor(seconds / 3600);
        seconds %= 3600;

        const minutes = Math.floor(seconds / 60);
        seconds %= 60;

        const parts: string[] = [];

        if (days) parts.push(`${days} day${days === 1 ? "" : "s"}`);
        if (hours) parts.push(`${hours} hour${hours === 1 ? "" : "s"}`);
        if (minutes) parts.push(`${minutes} minute${minutes === 1 ? "" : "s"}`);
        if (seconds) parts.push(`${seconds} second${seconds === 1 ? "" : "s"}`);

        return parts.join(" ");
    }
    async function loadConnections() {
        try {
            const accounts: CloudAccount[] = await getAwsAccountConnections();

            const uiConnections: Connections[] = await Promise.all(
                accounts.map(async (account) => {
                    const resourceCount = await getAwsAccountResourceCount(account.id);

                    return {
                        id: account.id,
                        name: account.displayName,
                        detail: `Ingestion every ${formatIngestionPeriod(Number(account.ingestionPeriod))}`,
                        provider: "AWS",
                        resource: resourceCount,
                        status: resourceCount > 0 ? "active" : "inactive",
                    };
                })
            );

            setConnections(uiConnections);
        } catch (error) {
            console.error("Failed to load AWS connections", error);
        }
    }

    useEffect(() => {
        loadConnections();
    }, []);

    const handleAdd = () => {
        router.push(`/addConnection/aws`); // just aws for now
    };

    return (
        <div data-theme="dark" className="min-h-screen bg-background text-foreground p-8">
            {/* this si for the heading */}
            <div className="flex items-center justify-between mb-6">
                <div className="flex items-center gap-3">
                    <Button
                        variant="ghost"
                        size="icon"
                        onClick={() => router.back()}
                        className="text-muted-foreground hover:text-foreground h-8 w-8"
                    >
                        {" "}
                        <ArrowLeft size={18} />{" "}
                    </Button>

                    <h1 className="text-3xl font-semibold text-foreground"> Connection Manager </h1>
                </div>

                <Button
                    onClick={handleAdd}
                    className="text-sm px-3 py-1.5 h-auto bg-primary hover:bg-primary/90"
                >
                    {" "}
                    + add
                </Button>
            </div>

            {/* this is for the provider tabs */}
            <div className="flex items-center justify-between mb-6">
                <Tabs
                    value={activeFilter || undefined}
                    onValueChange={(value) => setActiveFilter(value as Providers)}
                    className="mb-4"
                >
                    <TabsList className="self-start inline-flex gap-1 h-auto p-1 bg-muted rounded-lg w-fit">
                        {(["All", "AWS", "Azure", "GCP"] as Providers[]).map((providers) => {
                            const isActive = activeFilter === providers;

                            const styling = providerTabs[providers];

                            return (
                                <TabsTrigger
                                    key={providers}
                                    value={providers}
                                    className={`flex-none text-xs px-2.5 py-0.5 h-auto rounded-[var(--radius-sm)] font-medium transition-all bg-transparent ${isActive ? styling.active : styling.inactive}`}
                                >
                                    {" "}
                                    {providers}
                                </TabsTrigger>
                            );
                        })}
                    </TabsList>
                </Tabs>

                {/* these are for the icons on the tiles of the conn */}
                <div className="flex items-center gap-2">
                    <Button
                        variant="ghost"
                        size="icon"
                        className="text-muted-foreground hover:text-foreground h-8 w-8"
                    >
                        {" "}
                        <Search size={16} />{" "}
                    </Button>
                    <Button
                        variant="ghost"
                        size="icon"
                        className="text-muted-foreground hover:text-foreground h-8 w-8"
                    >
                        {" "}
                        <SlidersHorizontal size={16} />{" "}
                    </Button>
                </div>
            </div>

            {/* this is for the list of conn */}
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-3">
                {filtered.map((connection) => (
                    <Card key={connection.id} className="border-border">
                        <CardContent className="p-4">
                            <div className="flex items-center justify-between mb-3">
                                <span
                                    className={`text-xs font-medium px-2.5 py-0.5 rounded-full ${badges(connection.provider)}`}
                                >
                                    {" "}
                                    {connection.provider}{" "}
                                </span>

                                <div className="flex items-center gap-1">
                                    <AlertDialog>
                                        <AlertDialogTrigger asChild>
                                            <Button
                                                variant="ghost"
                                                size="icon"
                                                className="h-6 w-6 text-muted-foreground hover:text-destructive transition-colors"
                                            >
                                                <Trash2 size={13} />
                                            </Button>
                                        </AlertDialogTrigger>

                                        <AlertDialogContent>
                                            <AlertDialogHeader>
                                                <AlertDialogTitle>
                                                    Delete connection?
                                                </AlertDialogTitle>

                                                <AlertDialogDescription>
                                                    This will permanently delete the connection
                                                    <strong> &quot;{connection.name}&quot;</strong>.
                                                    This action cannot be undone.
                                                </AlertDialogDescription>
                                            </AlertDialogHeader>

                                            <AlertDialogFooter>
                                                <AlertDialogCancel>Cancel</AlertDialogCancel>

                                                <AlertDialogAction
                                                    onClick={() => handleDeletion(connection.id)}
                                                    className="bg-destructive hover:bg-destructive/90"
                                                >
                                                    Delete
                                                </AlertDialogAction>
                                            </AlertDialogFooter>
                                        </AlertDialogContent>
                                    </AlertDialog>
                                    <DropdownMenu>
                                        <DropdownMenuTrigger asChild>
                                            <Button
                                                variant="ghost"
                                                size="icon"
                                                className="h-6 w-6 text-muted-foreground hover:text-foreground transition-colors"
                                            >
                                                <MoreVertical size={13} />
                                            </Button>
                                        </DropdownMenuTrigger>

                                        <DropdownMenuContent align="end" className="w-52">
                                            <DropdownMenuItem
                                                onClick={() => handleViewDetails(connection.id)}
                                                className="cursor-pointer"
                                            >
                                                <Eye className="mr-2 h-4 w-4" />
                                                View Details
                                            </DropdownMenuItem>

                                            <DropdownMenuItem
                                                onClick={() => handleEditResources(connection.id)}
                                                className="cursor-pointer"
                                            >
                                                <Pencil className="mr-2 h-4 w-4" />
                                                Edit Resources
                                            </DropdownMenuItem>
                                        </DropdownMenuContent>
                                    </DropdownMenu>{" "}
                                </div>
                            </div>

                            <p className="text-sm font-medium text-foreground mb-0.5">
                                {" "}
                                {connection.name}{" "}
                            </p>

                            <p className="text-xs text-muted-foreground mb-4">
                                {" "}
                                {connection.detail}{" "}
                            </p>

                            <div className="flex flex-col gap-3 pt-3">
                                <Separator className="bg-border" />

                                <div className="flex items-center justify-between">
                                    <span className="text-xs text-muted-foreground">
                                        {" "}
                                        {connection.resource} resource
                                        {connection.resource !== 1 ? "s" : ""}
                                    </span>

                                    {connection.resource === 0 ? (
                                        <Badge variant="secondary" className="text-xs px-2 py-0.5">
                                            {" "}
                                            inactive{" "}
                                        </Badge>
                                    ) : (
                                        <Badge className="bg-success/20 text-success border-success/20 hover:bg-success/30 text-xs px-2 py-0.5">
                                            {" "}
                                            active{" "}
                                        </Badge>
                                    )}
                                </div>
                            </div>
                        </CardContent>
                    </Card>
                ))}
            </div>
        </div>
    );
}
