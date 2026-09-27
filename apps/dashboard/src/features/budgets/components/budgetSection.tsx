"use client";

import { useState, useMemo } from "react";
import { Plus } from "lucide-react";
import { toast } from "sonner";
import { Button } from "@/components/atoms/button";
import { Card, CardContent } from "@/components/atoms/card";
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
import { useBudget } from "@/features/budgets/hooks/useBudget";
import { BudgetTable } from "@/features/budgets/components/budgetTable";
import { BudgetPopup } from "@/features/budgets/components/budgetPopup";
import type { Budget, CreateBudgetRequest } from "@/features/budgets/types/budgetTypes";
import { Input } from "@/components/atoms/input";

export function BudgetSection() {
    const { budgets, loading, forError, createBudget, updateBudget, removeBudget } = useBudget();

    const [popupOpen, setPopupOpen] = useState(false);

    const [editing, setEditing] = useState<Budget | null>(null);

    const [deleteBudget, setDeleteBudget] = useState<Budget | null>(null);

    const [search, setSearch] = useState("");

    const forFilters = useMemo(
        () =>
            budgets.filter((budget) => {
                const wordSearched = search.toLowerCase();

                return (
                    budget.scope.toLowerCase().includes(wordSearched) ||
                    (budget.scope_id?.toLowerCase().includes(wordSearched) ?? false)
                );
            }),
        [budgets, search]
    );

    const countForEnabled = budgets.filter((budget) => budget.enabled).length;

    const forTotalCount = budgets.length;

    const handlingNew = () => {
        setEditing(null);

        setPopupOpen(true);
    };

    const handlingEdit = (budget: Budget) => {
        setEditing(budget);
        setPopupOpen(true);
    };

    const handlingToggle = async (budget: Budget, enabled: boolean) => {
        try {
            await updateBudget(budget.budget_id, { enabled });

            toast.success(enabled ? "Budget enabled" : "Budget disabled");
        } catch {
            toast.error("Failed to update budget");
        }
    };

    const handlingSubmit = async (payload: CreateBudgetRequest) => {
        try {
            if (editing) {
                await updateBudget(editing.budget_id, {
                    amount: payload.amount,
                    currency: payload.currency,
                    window_days: payload.window_days,
                    enabled: payload.enabled,
                });

                toast.success("Budget updated");
            } else {
                await createBudget(payload);

                toast.success("Budget added");
            }
        } catch {
            toast.error(editing ? "Failed to update budget" : "Failed to add budget");
        }
    };

    const confirmingDelete = async () => {
        if (!deleteBudget) {
            return;
        }

        try {
            await removeBudget(deleteBudget.budget_id);

            toast.success("Budget deleted");

            setDeleteBudget(null);
        } catch {
            toast.error("Failed to delete budget");
        }
    };

    return (
        <>
            <div className="mb-4 flex items-center gap-2">
                <Input
                    className="h-9 w-64"
                    placeholder="Search budgets"
                    value={search}
                    onChange={(change) => setSearch(change.target.value)}
                />
            </div>

            <div className="mb-4 flex items-center justify-between">
                <span className="text-sm text-muted-foreground">
                    {" "}
                    {countForEnabled} of {forTotalCount} budgets enabled{" "}
                </span>

                <Button onClick={handlingNew}>
                    {" "}
                    <Plus className="h-4 w-4" /> Add budget{" "}
                </Button>
            </div>

            {loading && (
                <Card>
                    <CardContent className="py-6 text-center text-sm text-muted-foreground">
                        {" "}
                        Loading budgets{" "}
                    </CardContent>
                </Card>
            )}

            {forError && (
                <Card>
                    <CardContent className="py-6 text-center text-sm text-destructive">
                        {" "}
                        {forError}{" "}
                    </CardContent>
                </Card>
            )}

            {!loading && !forError && (
                <BudgetTable
                    budgets={forFilters}
                    edit={handlingEdit}
                    toggleEnabled={handlingToggle}
                    onDelete={setDeleteBudget}
                />
            )}

            <BudgetPopup
                key={editing?.budget_id ?? "new"}
                open={popupOpen}
                initial={editing}
                onClose={() => setPopupOpen(false)}
                onSubmit={handlingSubmit}
            />

            <AlertDialog
                open={deleteBudget !== null}
                onOpenChange={(change) => !change && setDeleteBudget(null)}
            >
                <AlertDialogContent>
                    <AlertDialogHeader>
                        <AlertDialogTitle> Delete budget? </AlertDialogTitle>

                        <AlertDialogDescription>
                            {" "}
                            This will permanently delete the {deleteBudget?.scope} budget of{" "}
                            {deleteBudget?.currency} {deleteBudget?.amount}.{" "}
                        </AlertDialogDescription>
                    </AlertDialogHeader>

                    <AlertDialogFooter>
                        <AlertDialogCancel> Cancel </AlertDialogCancel>

                        <AlertDialogAction onClick={confirmingDelete}> Delete </AlertDialogAction>
                    </AlertDialogFooter>
                </AlertDialogContent>
            </AlertDialog>
        </>
    );
}
