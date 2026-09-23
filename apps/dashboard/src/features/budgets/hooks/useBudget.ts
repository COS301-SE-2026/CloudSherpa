"use client";

import {useCallback, useEffect, useState} from "react";
import {addBudget, deleteBudget, editBudget, fetchBudgets} from "@/features/budgets/budgets";
import type {Budget, CreateBudgetRequest, UpdateBudgetRequest} from "@/features/budgets/types/budgetTypes";

interface BudegtResult{
    budgets : Budget[];
    loading : boolean;
    forError : string | null;
    refreshing : () => Promise<void>;
    createBudget : (forPayload : CreateBudgetRequest) => Promise<Budget>;
    updateBudget : (id : string, forPayload : UpdateBudgetRequest) => Promise<Budget>;
    removeBudget : (id : string) => Promise<void>;
}

export function useBudget(scope?: string, scopeId?: string) : BudegtResult{
    const [budgets, setBudgets] = useState<Budget[]>([]);

    const [loading, setLoading] = useState<boolean>(true);

    const [forError, setForError] = useState<string | null>(null);

    useEffect(() => {
        let cancelled = false;

        setLoading(true);

        setForError(null);

        (async () => {
            try{
                const forData = await fetchBudgets(scope, scopeId);

                if(!cancelled){
                    setBudgets(forData);
                }
            }catch(error){
                    if(!cancelled){
                        setForError(error instanceof Error ? error.message : "Failed to load budgets",);
                    }
                }finally{
                    if(!cancelled){
                        setLoading(false);
                    }
                }
        })();

        return () => {cancelled = true;};
    }, [scope, scopeId]);

    const refreshing = useCallback(async () => {
        setLoading(true);

        setForError(null);

        try{
            setBudgets(await fetchBudgets(scope, scopeId));
        }catch(error){
            setForError(error instanceof Error ? error.message : "Failed to load budgets");
        }finally{
            setLoading(false);
        }
    }, [scope, scopeId]);

    const createBudget = useCallback(async (forPayload : CreateBudgetRequest) => {
        const createdBudgets = await addBudget(forPayload);

        setBudgets((previous) => [...previous, createdBudgets]);

        return createdBudgets;
    }, []);

    const updateBudget = useCallback(async (id : string, forPayload : UpdateBudgetRequest) => {
        const updatedBudget = await editBudget(id, forPayload);

        setBudgets((previous) => previous.map((budget) => (budget.budget_id === id ? updatedBudget : budget)));

        return updatedBudget;
    }, []);

    const removeBudget = useCallback(async (id : string) => {
        await deleteBudget(id);

        setBudgets((previous) => previous.filter((budget) => budget.budget_id !== id));
    }, []);

    return {budgets, loading, forError, refreshing, createBudget, updateBudget, removeBudget};
}