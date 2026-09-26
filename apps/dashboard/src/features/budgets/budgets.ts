import apiClient from "@/lib/fetch/api-client";
import type {
    Budget,
    CreateBudgetRequest,
    UpdateBudgetRequest,
} from "@/features/budgets/types/budgetTypes";

export const fetchBudgets = async (scope?: string, scopeId?: string): Promise<Budget[]> => {
    const forParameters = new URLSearchParams();

    if (scope) {
        forParameters.set("scope", scope);
    }

    if (scopeId) {
        forParameters.set("scopeId", scopeId);
    }

    const forQuery = forParameters.toString();

    const forSuffix = forQuery ? "?" + `${forQuery}` : "";

    return apiClient<Budget[]>(`/api/budgets${forSuffix}`, { method: "GET" });
};

export const addBudget = async (forPayload: CreateBudgetRequest): Promise<Budget> =>
    apiClient<Budget>("/api/budgets", { method: "POST", body: JSON.stringify(forPayload) });

export const editBudget = async (
    budgetId: string,
    forPayload: UpdateBudgetRequest
): Promise<Budget> =>
    apiClient<Budget>(`/api/budgets/${budgetId}`, { method: "PUT", body: JSON.stringify(forPayload) });

export const deleteBudget = async (budgetId: string): Promise<void> =>
    apiClient<void>(`/api/budgets/${budgetId}`, { method: "DELETE" });
