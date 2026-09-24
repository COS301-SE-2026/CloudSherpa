"use client";

import {useEffect, useState} from "react";
import {Label} from "@/components/atoms/label";
import {Input} from "@/components/atoms/input";
import {Button} from "@/components/atoms/button";
import {Dialog, DialogContent, DialogFooter, DialogHeader, DialogTitle} from "@/components/atoms/dialog";
import {Select, SelectContent, SelectItem, SelectTrigger, SelectValue} from "@/components/atoms/select";
import {Checkbox} from "@/components/atoms/checkbox";
import { BUDGET_CURRENCY, BUDGET_SCOPES, Budget, ScopeForBudget, BudgetCurrency, CreateBudgetRequest } from "@/features/budgets/types/budgetTypes";
import {useAuthContext} from "@/features/authentication/providers/AuthContext";
import {getAwsAccountConnections, getAwsAccountResources} from "@/lib/fetch/cloud-account-api";
import type { CloudAccount } from "@/lib/fetch/dto/cloud-account";
import type {CloudResource} from "@/lib/fetch/dto/cloud-resource";

interface PropsForPopup{
    open : boolean;
    initial?: Budget | null;
    userId: string; 
    onClose : () => void;
    onSubmit : (payload : CreateBudgetRequest) => Promise<void>;
}

export function BudgetPopup({open, initial, userId: string, onClose, onSubmit} : Readonly<PropsForPopup>){
    const {user} = useAuthContext();

    const [scope, setScope] = useState<ScopeForBudget>(initial?.scope ?? "TENANT");

    const [amount, setAmount] = useState<number>(initial?.amount ?? 0);

    const [currency, setCurrency] = useState<BudgetCurrency>(initial?.currency ?? "USD");

    const [windowDays, setWindowDays] = useState<number>(initial?.window_days ?? 30);

    const [enabled, setEnabled] = useState<boolean>(initial?.enabled ?? true);

    const [selectedAccountId, setSelectedAccountId] = useState<string | null>(initial?.scope === "ACCOUNT" || initial?.scope === "RESOURCE" ? initial.scope_id : null);

    const [selectedResourceId, setSelectedResourceId] = useState<string | null>(initial?.scope === "RESOURCE" ? initial.scope_id : null);

    const [accounts, setAccounts] = useState<CloudAccount[]>([]);

    const [resources, setResources] = useState<CloudResource[]>([]);

    const [loadingAccounts, setLoadingAccounts] = useState(false);

    const [loadingResources, setLoadingResources] = useState(false);

    const [submitting, setSubmitting] = useState(false);

    const [amountError, setAmountError] = useState<string | null>(null);

    const [windowError, setWindowError] = useState<string | null>(null);

    const [scopeError, setScopeError] = useState<string | null>(null);

    useEffect(() => {
        if(!open){
            return;
        }

        if(scope!== "ACCOUNT" && scope !== "RESOURCE"){
            return;
        }

        let cancelled = false;

        (async () => {
            setLoadingAccounts(true);

            try{
                const forData = await getAwsAccountConnections();

                if(!cancelled){
                    setAccounts(forData);
                }
            }catch{
                if(!cancelled){
                    setAccounts([]);
                }
            }finally{
                if(!cancelled){
                    setLoadingAccounts(false);
                }
            }
        })();

        return () => {cancelled = true};
    }, [open, scope]);

    useEffect(() => {
        if(scope !== "RESOURCE" || !selectedAccountId){
            return;
        }

        let cancelled = false;

        (async () => {
            setLoadingResources(true);

            try{
                const forData = await getAwsAccountResources(selectedAccountId);

                if(!cancelled){
                    setResources(forData);
                }
            }catch{
                if(!cancelled){
                    setResources([]);
                }
            }finally{
                if(!cancelled){
                    setLoadingResources(false);
                }
            }
        })();

        return () => {cancelled = true;};
    }, [scope, selectedAccountId]);

    const handlingScopeChange = (next : ScopeForBudget) => {
        setScope(next);
        setSelectedAccountId(null);
        setSelectedResourceId(null);
        setScopeError(null);
    };

    const handlingSubmit = async (submit : React.FormEvent) => {
        submit.preventDefault();

        let hasError = false;

        setScopeError(null);

        const resolvedScopeId = scope === "TENANT" ? user?.userId ?? null : scope === "ACCOUNT" ? selectedAccountId : selectedResourceId;

        if(!resolvedScopeId){
            setScopeError(scope === "TENANT" ? "Not signed in" : scope === "ACCOUNT" ? "Select an account" : "Select an account and a resource");

            hasError = true;
        }

        if(!Number.isFinite(amount) || amount<=0){
            setAmountError("Amount must be greater than 0");

            hasError = true;
        }

        if(!Number.isFinite(windowDays) || windowDays<=0){
            setWindowError("Window must be greater than 0");

            hasError = true;
        }

        if(hasError){
            return;
        }

        setSubmitting(true);

        try{
            await onSubmit({
                userId : user!.userId, scope, scope_id : resolvedScopeId!, amount : Number(amount), currency, window_days : Number(windowDays), enabled,
            });

            onClose();
        }finally{
            setSubmitting(false);
        }
    };

    return(
        <Dialog open = {open} onOpenChange = {(next) => !next && onClose()}>
            <DialogContent className = "sm:max-w-md">
                <DialogHeader>
                    <DialogTitle> {initial ? "Edit budget" : "New budget"} </DialogTitle>
                </DialogHeader>

                <form onSubmit = {handlingSubmit} className = "space-y-4">
                    <div className = "space-y-2">
                        <Label htmlFor = "scope"> Scope </Label> 

                        <Select value = {scope} onValueChange ={(value) => handlingScopeChange(value as ScopeForBudget)}>
                            <SelectTrigger id = "scope"> <SelectValue/> </SelectTrigger>

                            <SelectContent>
                                {BUDGET_SCOPES.map((scoping) => (
                                    <SelectItem key = {scoping} value = {scoping}> {scoping} </SelectItem>
                                ))}
                            </SelectContent>
                        </Select>
                    </div>

                    {(scope === "ACCOUNT" || scope === "RESOURCE") && (
                        <div className = "space-y-2">
                            <Label htmlFor = "account"> Account </Label>

                            <Select value = {selectedAccountId ?? ""} onValueChange = {(value) => {setSelectedAccountId(value); setSelectedResourceId(null);}}>
                                <SelectTrigger id = "account">
                                    <SelectValue placeholder = {loadingAccounts ? "Loading accounts" : "Select account"}/>
                                </SelectTrigger>

                                <SelectContent>
                                    {accounts.map((account) => (
                                        <SelectItem key = {account.id} value = {account.id}> {account.displayName} </SelectItem>
                                    ))}
                                </SelectContent>
                            </Select>
                        </div>
                    )}

                    {scope === "RESOURCE" && (
                        <div className = "space-y-2">
                            <Label htmlFor = "resource"> Resource </Label>

                            <Select value = {selectedResourceId ?? ""} onValueChange = {setSelectedResourceId} disabled = {!selectedAccountId}>
                                <SelectTrigger id = "resource">
                                    <SelectValue placeholder = {!selectedAccountId ? "Select an account first" : loadingResources ? "Loading resources" : "Select resource"}/>
                                </SelectTrigger>

                                <SelectContent>
                                    {resources.map((resource) => (
                                        <SelectItem key = {resource.id} value = {resource.id}> {resource.resourceName} </SelectItem>
                                    ))}
                                </SelectContent>
                            </Select>
                        </div>
                    )}

                    {scopeError && <p className = "text-xs text-destructive"> {scopeError} </p>}

                    <div className = "space-y-2">
                        <Label htmlFor = "amount"> Amount </Label>

                        <Input id = "amount" type = "number" step = "any" value = {amount} onChange = {(change) => {setAmount(Number(change.target.value)); if(amountError){
                            setAmountError(null);
                        }}}/>

                        {amountError && <p className = "text-xs text-destructive"> {amountError} </p>}
                    </div>

                    <div className = "space-y-2">
                        <Label htmlFor = "currency"> Currency </Label>

                        <Select value = {currency} onValueChange = {(value) => setCurrency(value as BudgetCurrency)}>
                            <SelectTrigger id = "currency"> <SelectValue/> </SelectTrigger>

                            <SelectContent>
                                {BUDGET_CURRENCY.map((currencies) => (
                                    <SelectItem key = {currencies} value = {currencies}> {currencies} </SelectItem>
                                ))}
                            </SelectContent>
                        </Select>
                    </div>

                    <div className = "space-y-2">
                        <Label htmlFor = "window"> Window (days) </Label>

                        <Input id = "window" type = "number" value = {windowDays} onChange = {(change) => {setWindowDays(Number(change.target.value)); if(windowError){
                            setWindowError(null);
                        }}}/>

                        {windowError && <p className = "text-xs text-destructive"> {windowError} </p>}
                    </div>

                    <div className = "flex items-center gap-2">
                        <Checkbox id = "enabled" checked = {enabled} onCheckedChange = {(change) => setEnabled(change === true)}/>

                        <Label htmlFor = "enabled"> Enabled </Label>
                    </div>

                    <DialogFooter className = "pt-2">
                        <Button type = "button" variant = "outline" onClick = {onClose}> Cancel </Button>

                        <Button type = "submit" disabled = {submitting}> {submitting ? "Saving" : "Save"} </Button>
                    </DialogFooter>
                </form>
            </DialogContent>
        </Dialog>
    );
}