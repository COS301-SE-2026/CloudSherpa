import apiClient from "@/lib/fetch/api-client";
import type {Budget, CreateBudgetRequest, UpdateBudgetRequest} from "@/features/budgets/types/budgetTypes";

export const fetchBudgets = async (scope?: string, scopeId?: string) : Promise<Budget[]> => {
    const forParameters = new URLSearchParams();

    if(scope){
        forParameters.set("scope", scope);
    }

    if(scopeId){
        forParameters.set("scopeId", scopeId);
    }

    const forQuery = forParameters.toString();

    return apiClient<Budget[]>(`/budgets${forQuery ? `?${forQuery}` : ""}`, {method : "GET"});
};

export const addBudget = async (forPayload : CreateBudgetRequest) : Promise<Budget> =>
    apiClient<Budget>("/budgets", {method : "POST", body : JSON.stringify(forPayload)});

export const editBudget = async (budgetId : string, forPayload : UpdateBudgetRequest) : Promise<Budget> => 
    apiClient<Budget>(`/budgets/${budgetId}`, {method : "PUT", body : JSON.stringify(forPayload)});

export const deleteBudget = async (budgetId : string) : Promise<void> =>
    apiClient<void>(`/budgets/${budgetId}`, {method : "DELETE"});