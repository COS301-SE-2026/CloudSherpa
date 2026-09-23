import type { CurrencyEnum } from "@/features/dashboard/types/currency";

export type ScopeForBudget = "TENANT" | "ACCOUNT" | "RESOURCE";

export type BudgetCurrency = CurrencyEnum;

export interface Budget{
    budget_id : string;
    user_id : string;
    scope : ScopeForBudget;
    scope_id : string | null;
    amount : number;
    currency : CurrencyEnum;
    window_days : number;
    enabled : boolean;
    created_at : string;
    updated_at : string;
}

export interface CreateBudgetRequest{
    userId : string;
    scope : ScopeForBudget;
    scope_id : string | null;
    amount : number;
    currency : CurrencyEnum;
    window_days : number;
    enabled : boolean;
}

export interface UpdateBudgetRequest{
    amount?: number;
    currency?: CurrencyEnum;
    window_days?: number;
    enabled?: boolean;
}

export const BUDGET_SCOPES : ScopeForBudget[] = ["TENANT", "ACCOUNT", "RESOURCE"];

export const BUDGET_CURRENCY : CurrencyEnum[] = ["USD", "EUR", "ZAR"];