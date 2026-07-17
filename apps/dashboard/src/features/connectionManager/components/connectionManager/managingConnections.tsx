"use client";

/*
- users should be able to add connections
- when adding connections, they should be redirected to the wizard setup
- they should be able to view the resources assoc with that connection and delete connections
*/

import { useState } from "react";
import { Trash2, ArrowLeft, SlidersHorizontal, Search, MoreVertical } from "lucide-react";
import { Card, CardContent } from "@/components/atoms/card";
import { Button } from "@/components/atoms/button";
import { Tabs, TabsList, TabsTrigger } from "@/components/atoms/tabs";
import { Separator } from "@/components/atoms/separator";
import { Badge } from "@/components/atoms/badge";

type Providers = "All" | "AWS" | "Azure" | "GCP";
interface Connections {
    id: number;
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

const hardCodedConn: Connections[] = [
    {
        id: 1,
        name: "Connection 1",
        detail: "details about conn",
        provider: "AWS",
        resource: 3,
        status: "active",
    },

    {
        id: 2,
        name: "Connection 2",
        detail: "details about connection",
        provider: "Azure",
        resource: 5,
        status: "active",
    },

    {
        id: 3,
        name: "Connection 3",
        detail: "details about connection",
        provider: "GCP",
        resource: 0,
        status: "inactive",
    },

    {
        id: 4,
        name: "Connection 4",
        detail: "details about connection",
        provider: "AWS",
        resource: 2,
        status: "active",
    },
];

export default function ManagingConnections() {
    const [connections, setConnections] = useState<Connections[]>(hardCodedConn);

    const [activeFilter, setActiveFilter] = useState<Providers>("All");

    const filtered =
        activeFilter === "All"
            ? connections
            : connections.filter(
                  (filteredConnections) => filteredConnections.provider === activeFilter
              );

    const handleDeletion = (id: number) => {
        setConnections((previous) =>
            previous.filter((filteredConnections) => filteredConnections.id != id)
        );
    };

    const handleAdd = () => {
        //empty for now
    };

    return (
        <div data-theme="dark" className="min-h-screen bg-background text-foreground p-8">
            {/* this si for the heading */}
            <div className="flex items-center justify-between mb-6">
                <div className="flex items-center gap-3">
                    <Button
                        variant="ghost"
                        size="icon"
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
                                    <Button
                                        onClick={() => handleDeletion(connection.id)}
                                        variant="ghost"
                                        size="icon"
                                        className="h-6 w-6 text-muted-foreground hover:text-destructive transition-colors"
                                    >
                                        {" "}
                                        <Trash2 size={13} />
                                    </Button>

                                    <Button
                                        variant="ghost"
                                        size="icon"
                                        className="h-6 w-6 text-muted-foreground hover:text-foreground transition-colors"
                                    >
                                        {" "}
                                        <MoreVertical size={13} />
                                    </Button>
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
