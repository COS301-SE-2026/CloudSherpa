"use client";

import { useMemo, useState } from "react";
import { Button } from "@/components/atoms/button";
import { Card, CardContent } from "@/components/atoms/card";
import { Input } from "@/components/atoms/input";
import { ThresholdPopup } from "@/features/thresholds/components/thresholds/thresholdPopup";
import { ThresholdTable } from "@/features/thresholds/components/thresholds/thresholdTable";
import { useThresholds } from "@/features/thresholds/hooks/useThresholds";
import type { CreateThresholdRequest, Threshold } from "@/features/thresholds/types/thresholdTypes";
import { Tabs, TabsList, TabsTrigger, TabsContent } from "@/components/atoms/tabs";
import {
    AlertDialog,
    AlertDialogAction,
    AlertDialogCancel,
    AlertDialogContent,
    AlertDialogDescription,
    AlertDialogFooter,
    AlertDialogHeader,
    AlertDialogTitle,
} from "@/components/atoms/alert-dialog";
import { Plus } from "lucide-react";
import { toast } from "sonner";
import { BudgetSection } from "@/features/budgets/components/budgetSection";

interface PropsForMainPage {
    resourceId?: string;
    userId?: string;
}

export function MainPage({ resourceId, userId }: Readonly<PropsForMainPage>) {
    const { thresholds, loading, forError, createThreshold, updateThreshold, removeThreshold } =
        useThresholds(resourceId);

    const [search, setSearch] = useState("");

    const [selectedTab, setSelectedTab] = useState<"budgets" | "thresholds">("thresholds");

    const [popupOpen, setPopupOpen] = useState(false);

    const [isEditing, setIsEditing] = useState<Threshold | null>(null);

    const [deleteThreshold, setDeleteThreshold] = useState<Threshold | null>(null);

    const forFilters = useMemo(
        () =>
            thresholds.filter((forThreshold) =>
                forThreshold.metricName.toLowerCase().includes(search.toLowerCase())
            ),
        [thresholds, search]
    );

    //temporary
    if (!userId) {
        return null;
    }

    const countForEnabled = thresholds.filter((forThreshold) => forThreshold.enabled).length;

    const forTotalCount = thresholds.length;

    const handlingToggleEnabled = async (forThreshold: Threshold, enabled: boolean) => {
        try {
            await updateThreshold(forThreshold.thresholdId, { enabled });
            toast.success(enabled ? "Threshold enabled" : "Threshold disabled");
        } catch {
            toast.error("Failed to update threshold");
        }
    };

    const handlingEdit = (forThreshold: Threshold) => {
        setIsEditing(forThreshold);

        setPopupOpen(true);
    };

    const handlingNewThreshold = () => {
        setIsEditing(null);

        setPopupOpen(true);
    };

    const handlingSubmit = async (forPayload: CreateThresholdRequest) => {
        try {
            if (isEditing) {
                await updateThreshold(isEditing.thresholdId, {
                    metric_name: forPayload.metric_name,
                    operator: forPayload.operator,
                    value: forPayload.value,
                    severity: forPayload.severity,
                    enabled: forPayload.enabled,
                });

                toast.success("Threshold updated");
            } else {
                await createThreshold(forPayload);

                toast.success("Threshold added");
            }
        } catch {
            toast.error(isEditing ? "Failed to update threshold" : "Failed to add threshold");
        }
    };

    const handlingDelete = async (forThreshold: Threshold) => {
        setDeleteThreshold(forThreshold);
    };

    const confirmDelete = async () => {
        if (!deleteThreshold) {
            return;
        }

        try {
            await removeThreshold(deleteThreshold.thresholdId);

            toast.success("Threshold deleted");

            setDeleteThreshold(null);
        } catch {
            toast.error("Failed to delete threshold");
        }
    };

    return (
        <div className="min-h-screen bg-background text-foreground">
            <div className="mx-auto max-w-6xl px-6 py-8">
                <header className="mb-6">
                    <h1 className="mb-4 text-2xl font-semibold tracking-tight text-foreground">
                        {" "}
                        Thresholds &amp; Budgets{" "}
                    </h1>

                    <Tabs
                        value={selectedTab}
                        onValueChange={(value) => setSelectedTab(value as "budgets" | "thresholds")}
                    >
                        <TabsList>
                            <TabsTrigger value="thresholds"> Thresholds </TabsTrigger>

                            <TabsTrigger value="budgets"> Budgets </TabsTrigger>
                        </TabsList>

                        <TabsContent value="thresholds" className="mt-6">
                            <div className="mb-4 flex items-center gap-2">
                                <Input
                                    className="h-9 w-64"
                                    placeholder="Search thresholds"
                                    value={search}
                                    onChange={(change) => setSearch(change.target.value)}
                                />
                            </div>

                            <div className="mb-4 flex items-center justify-between">
                                <span className="text-sm text-muted-foreground">
                                    {" "}
                                    {countForEnabled} of {forTotalCount} rules enabled{" "}
                                </span>

                                <Button onClick={handlingNewThreshold}>
                                    {" "}
                                    <Plus className="h-4 w-4" /> New rule{" "}
                                </Button>
                            </div>

                            {loading && (
                                <Card>
                                    <CardContent className="py-8 text-center text-sm text-muted-foreground">
                                        {" "}
                                        Loading{" "}
                                    </CardContent>
                                </Card>
                            )}

                            {forError && (
                                <Card>
                                    <CardContent className="py-8 text-center text-sm text-destructive">
                                        {" "}
                                        {forError}{" "}
                                    </CardContent>
                                </Card>
                            )}

                            {!loading && !forError && (
                                <ThresholdTable
                                    thresholds={forFilters}
                                    edit={handlingEdit}
                                    toggleEnabled={handlingToggleEnabled}
                                    onDelete={handlingDelete}
                                />
                            )}
                        </TabsContent>

                        <TabsContent value="budgets" className="mt-6">
                            {" "}
                            <BudgetSection />{" "}
                        </TabsContent>
                    </Tabs>
                </header>

                <ThresholdPopup
                    key={isEditing?.thresholdId ?? "new"}
                    open={popupOpen}
                    initial={isEditing}
                    resourceId={resourceId}
                    userId={userId}
                    onClose={() => setPopupOpen(false)}
                    onSubmit={handlingSubmit}
                />

                <AlertDialog
                    open={deleteThreshold !== null}
                    onOpenChange={(change) => !change && setDeleteThreshold(null)}
                >
                    <AlertDialogContent>
                        <AlertDialogHeader>
                            <AlertDialogTitle> Delete threshold? </AlertDialogTitle>

                            <AlertDialogDescription>
                                {" "}
                                This will permanently delete the {deleteThreshold?.metricName}{" "}
                                threshold.
                            </AlertDialogDescription>
                        </AlertDialogHeader>

                        <AlertDialogFooter>
                            <AlertDialogCancel> Cancel </AlertDialogCancel>

                            <AlertDialogAction onClick={confirmDelete}> Delete </AlertDialogAction>
                        </AlertDialogFooter>
                    </AlertDialogContent>
                </AlertDialog>
            </div>
        </div>
    );
}
