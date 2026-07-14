"use client";

/*
- users should be able to add connections
- when adding connections, they should be redirected to the wizard setup
- they should be able to view the resources assoc with that connection and delete connections
*/

import { useState } from "react";
import { Trash2 } from "lucide-react";
import { Card, CardContent } from "@/components/atoms/card";
import { Button } from "@/components/atoms/button";
import { Tabs, TabsList, TabsTrigger } from "@/components/atoms/tabs";

type Providers = "AWS" | "Azure" | "GCP";

interface Connections {
    id: number;
    name: string;
    provider: Providers;
    detail: string;
}

//will use these colours for the tabs for now, will add the company colours to the style dictionary and will update it
const providerTabs: Record<Providers, { active: string; inactive: string }> = {
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

const hardCodedConn: Connections[] = [
    {
        id: 1,
        name: "Connection 1",
        detail: "details about conn",
        provider: "AWS",
    },
];

export default function ManagingConnections() {
    const [connections, setConnections] = useState<Connections[]>(hardCodedConn);

    const [activeFilter, setActiveFilter] = useState<Providers | null>("AWS");

    const filtered = activeFilter
        ? connections.filter((filteredConnections) => filteredConnections.provider === activeFilter)
        : connections;

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
            <div className="flex items-start justify-between mb-5">
                <h1 className="text-3xl font-semibold text-foreground"> Connection Manager </h1>

                <Button onClick={handleAdd} className="text-sm px-3 py-1.5 h-auto">
                    {" "}
                    + add connection
                </Button>
            </div>

            {/* this is for the provider tabs */}
            <Tabs
                value={activeFilter || undefined}
                onValueChange={(value) => setActiveFilter(value as Providers)}
                className="mb-4"
            >
                <TabsList className="self-start inline-flex gap-1 h-auto p-1 bg-muted rounded-lg w-fit">
                    {(["AWS", "Azure", "GCP"] as Providers[]).map((providers) => {
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

            {/* this is for the list of conn */}
            <div className="flex flex-col gap-2">
                {filtered.map((connection) => (
                    <Card key={connection.id} className="border-border">
                        <CardContent className="px-4 py-3">
                            <div className="flex items-center justify-between">
                                <div>
                                    <p className="text-sm font-medium text-foreground">
                                        {" "}
                                        {connection.name}{" "}
                                    </p>

                                    <p className="text-xs text-[var(--color-neutral-400)] mt-0.5">
                                        {" "}
                                        {connection.detail}{" "}
                                    </p>
                                </div>

                                <div className="flex items-center gap-3">
                                    <span className="text-xs text-muted-foreground">
                                        {" "}
                                        resource{" "}
                                    </span>

                                    <Button
                                        onClick={() => handleDeletion(connection.id)}
                                        variant="ghost"
                                        size="icon"
                                        className="text-muted-foreground hover:text-destructive transition-colors"
                                    >
                                        {" "}
                                        <Trash2 size={15} />
                                    </Button>
                                </div>
                            </div>
                        </CardContent>
                    </Card>
                ))}
            </div>
        </div>
    );
}
