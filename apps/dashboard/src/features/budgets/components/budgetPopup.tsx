"use client";

import { useEffect, useState } from "react";
import { Label } from "@/components/atoms/label";
import { Input } from "@/components/atoms/input";
import { Button } from "@/components/atoms/button";
import {
    Dialog,
    DialogContent,
    DialogFooter,
    DialogHeader,
    DialogTitle,
} from "@/components/atoms/dialog";
import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from "@/components/atoms/select";
import { Checkbox } from "@/components/atoms/checkbox";
import {
    BUDGET_SCOPES,
    LABELS_FOR_SCOPE,
    Budget,
    ScopeForBudget,
    CreateBudgetRequest,
} from "@/features/budgets/types/budgetTypes";
import { useAuthContext } from "@/features/authentication/providers/AuthContext";
import { getAwsAccountConnections, getAwsAccountResources } from "@/lib/fetch/cloud-account-api";
import type { CloudAccount } from "@/lib/fetch/dto/cloud-account";
import type { CloudResource } from "@/lib/fetch/dto/cloud-resource";

interface PropsForPopup {
    open: boolean;
    initial?: Budget | null;
    onClose: () => void;
    onSubmit: (payload: CreateBudgetRequest) => Promise<void>;
}

export function BudgetPopup({ open, initial, onClose, onSubmit }: Readonly<PropsForPopup>) {
    const { user } = useAuthContext();

    const [scope, setScope] = useState<ScopeForBudget>(initial?.scope ?? "TENANT");

    const [amount, setAmount] = useState<string>(
        initial?.amount !== undefined ? String(initial.amount) : ""
    );

    const [windowDays, setWindowDays] = useState<number>(initial?.window_days ?? 30);

    const [enabled, setEnabled] = useState<boolean>(initial?.enabled ?? true);

    const [selectedAccountId, setSelectedAccountId] = useState<string | null>(
        initial?.scope === "ACCOUNT" ? initial.scope_id : null
    );

    const [selectedResourceId, setSelectedResourceId] = useState<string | null>(
        initial?.scope === "RESOURCE" ? initial.scope_id : null
    );

    const [accounts, setAccounts] = useState<CloudAccount[]>([]);

    const [resources, setResources] = useState<CloudResource[]>([]);

    const [loadingAccounts, setLoadingAccounts] = useState(false);

    const [loadingResources, setLoadingResources] = useState(false);

    const [submitting, setSubmitting] = useState(false);

    const [amountError, setAmountError] = useState<string | null>(null);

    const [windowError, setWindowError] = useState<string | null>(null);

    const [scopeError, setScopeError] = useState<string | null>(null);

    useEffect(() => {
        if (!open) {
            return;
        }

        if (initial?.scope !== "RESOURCE") {
            return;
        }

        if (!initial.scope_id) {
            return;
        }

        if (selectedAccountId) {
            return;
        }

        let cancelled = false;

        (async () => {
            const allConnections = await getAwsAccountConnections();

            for (const connection of allConnections) {
                if (cancelled) {
                    return;
                }

                const connectionResources = await getAwsAccountResources(connection.id);

                if (cancelled) {
                    return;
                }

                if (connectionResources.some((resource) => resource.id === initial.scope_id)) {
                    setSelectedAccountId(connection.id);

                    setSelectedResourceId(initial.scope_id);

                    return;
                }
            }
        })();

        return () => {
            cancelled = true;
        };
    }, [open, initial?.scope, initial?.scope_id, selectedAccountId]);

    useEffect(() => {
        if (!open) {
            return;
        }

        if (scope !== "ACCOUNT" && scope !== "RESOURCE") {
            return;
        }

        let cancelled = false;

        (async () => {
            setLoadingAccounts(true);

            try {
                const forData = await getAwsAccountConnections();

                if (!cancelled) {
                    setAccounts(forData);
                }
            } catch {
                if (!cancelled) {
                    setAccounts([]);
                }
            } finally {
                if (!cancelled) {
                    setLoadingAccounts(false);
                }
            }
        })();

        return () => {
            cancelled = true;
        };
    }, [open, scope]);

    useEffect(() => {
        if (scope !== "RESOURCE" || !selectedAccountId) {
            return;
        }

        let cancelled = false;

        (async () => {
            setLoadingResources(true);

            try {
                const forData = await getAwsAccountResources(selectedAccountId);

                if (!cancelled) {
                    setResources(forData);
                }
            } catch {
                if (!cancelled) {
                    setResources([]);
                }
            } finally {
                if (!cancelled) {
                    setLoadingResources(false);
                }
            }
        })();

        return () => {
            cancelled = true;
        };
    }, [scope, selectedAccountId]);

    const handlingScopeChange = (next: ScopeForBudget) => {
        setScope(next);
        setSelectedAccountId(null);
        setSelectedResourceId(null);
        setScopeError(null);
    };

    const resolvedTarget = (() => {
        if (scope === "TENANT") {
            return "All cloud accounts";
        }

        if (scope === "ACCOUNT") {
            if (!selectedAccountId) {
                return "No accounts selected";
            }

            const account = accounts.find((forAcc) => forAcc.id === selectedAccountId);

            return `${account?.displayName ?? selectedAccountId} - all resources`;
        }

        if (!selectedAccountId) {
            return "No account selected";
        }

        const resource = resources.find((forRes) => forRes.id === selectedResourceId);

        return resource?.resourceName ?? selectedResourceId;
    })();

    const handlingSubmit = async (submit: React.FormEvent) => {
        submit.preventDefault();

        let hasError = false;

        setScopeError(null);

        const forScopeId: Record<ScopeForBudget, string | null> = {
            TENANT: null,
            ACCOUNT: selectedAccountId,
            RESOURCE: selectedResourceId,
        };

        const resolvedScopeId = forScopeId[scope];

        const scopeErrorMessage: Record<ScopeForBudget, string> = {
            TENANT: "Not signed in",
            ACCOUNT: "Select an account",
            RESOURCE: "Select an account and a resource",
        };

        const validScope =
            scope === "TENANT" ||
            (scope === "ACCOUNT" && !!selectedAccountId) ||
            (scope === "RESOURCE" && !!selectedResourceId);

        if (!validScope) {
            setScopeError(scopeErrorMessage[scope]);

            hasError = true;
        }

        if (!user?.userId) {
            setScopeError("Not signed in");

            hasError = true;
        }

        const amountParsed = Number(amount);

        if (!Number.isFinite(amountParsed) || amountParsed < 0) {
            setAmountError("Amount must be 0 or greater");

            hasError = true;
        }

        if (!Number.isFinite(windowDays) || windowDays <= 0) {
            setWindowError("Window must be 0 or greater");

            hasError = true;
        }

        if (hasError) {
            return;
        }

        setSubmitting(true);

        try {
            await onSubmit({
                userId: user!.userId,
                scope,
                scope_id: resolvedScopeId!,
                amount: amountParsed,
                currency: "USD",
                window_days: Number(windowDays),
                enabled,
            });

            onClose();
        } finally {
            setSubmitting(false);
        }
    };

    let forPlaceholder: string;

    if (!selectedAccountId) {
        forPlaceholder = "Select an account first";
    } else if (loadingResources) {
        forPlaceholder = "Loading resources";
    } else {
        forPlaceholder = "Select resource";
    }

    return (
        <Dialog open={open} onOpenChange={(next) => !next && onClose()}>
            <DialogContent className="sm:max-w-md">
                <DialogHeader>
                    <DialogTitle> {initial ? "Edit budget" : "New budget"} </DialogTitle>
                </DialogHeader>

                <form onSubmit={handlingSubmit} className="space-y-4" noValidate>
                    <div className="space-y-2">
                        <Label htmlFor="scope"> Scope </Label>

                        <Select
                            value={scope}
                            onValueChange={(value) => handlingScopeChange(value as ScopeForBudget)}
                        >
                            <SelectTrigger id="scope">
                                <SelectValue />
                            </SelectTrigger>

                            <SelectContent>
                                {BUDGET_SCOPES.map((scoping) => (
                                    <SelectItem key={scoping} value={scoping}>
                                        {LABELS_FOR_SCOPE[scoping]}
                                    </SelectItem>
                                ))}
                            </SelectContent>
                        </Select>
                    </div>

                    {(scope === "ACCOUNT" || scope === "RESOURCE") && (
                        <div className="space-y-2">
                            <Label htmlFor="account"> Account </Label>

                            <Select
                                value={selectedAccountId ?? ""}
                                onValueChange={(value) => {
                                    setSelectedAccountId(value);
                                    setSelectedResourceId(null);
                                }}
                            >
                                <SelectTrigger id="account">
                                    <SelectValue
                                        placeholder={
                                            loadingAccounts ? "Loading accounts" : "Select account"
                                        }
                                    />
                                </SelectTrigger>

                                <SelectContent>
                                    {accounts.map((account) => (
                                        <SelectItem key={account.id} value={account.id}>
                                            {" "}
                                            {account.displayName}{" "}
                                        </SelectItem>
                                    ))}
                                </SelectContent>
                            </Select>
                        </div>
                    )}

                    {scope === "RESOURCE" && (
                        <div className="space-y-2">
                            <Label htmlFor="resource"> Resource </Label>

                            <Select
                                value={selectedResourceId ?? ""}
                                onValueChange={setSelectedResourceId}
                                disabled={!selectedAccountId}
                            >
                                <SelectTrigger id="resource">
                                    <SelectValue placeholder={forPlaceholder} />
                                </SelectTrigger>

                                <SelectContent>
                                    {resources.map((resource) => (
                                        <SelectItem key={resource.id} value={resource.id}>
                                            {" "}
                                            {resource.resourceName}{" "}
                                        </SelectItem>
                                    ))}
                                </SelectContent>
                            </Select>
                        </div>
                    )}

                    <div className="space-y-2">
                        <Label> Applies to </Label>

                        <p className="text-sm text-muted-foreground"> {resolvedTarget} </p>
                    </div>

                    {scopeError && <p className="text-xs text-destructive"> {scopeError} </p>}

                    <div className="space-y-2">
                        <Label htmlFor="amount"> Amount (USD) </Label>

                        <Input
                            id="amount"
                            type="number"
                            step="any"
                            min={0}
                            value={amount}
                            onChange={(change) => {
                                setAmount(change.target.value);
                                if (amountError) {
                                    setAmountError(null);
                                }
                            }}
                        />

                        {amountError && <p className="text-xs text-destructive"> {amountError} </p>}
                    </div>

                    <div className="space-y-2">
                        <Label htmlFor="window"> Window (days) </Label>

                        <Input
                            id="window"
                            type="number"
                            min={1}
                            step={1}
                            value={windowDays}
                            onChange={(change) => {
                                setWindowDays(Number(change.target.value));
                                if (windowError) {
                                    setWindowError(null);
                                }
                            }}
                        />

                        {windowError && <p className="text-xs text-destructive"> {windowError} </p>}
                    </div>

                    <div className="flex items-center gap-2">
                        <Checkbox
                            id="enabled"
                            checked={enabled}
                            onCheckedChange={(change) => setEnabled(change === true)}
                        />

                        <Label htmlFor="enabled"> Enabled </Label>
                    </div>

                    <DialogFooter className="pt-2">
                        <Button type="button" variant="outline" onClick={onClose}>
                            {" "}
                            Cancel{" "}
                        </Button>

                        <Button type="submit" disabled={submitting}>
                            {" "}
                            {submitting ? "Saving" : "Save"}{" "}
                        </Button>
                    </DialogFooter>
                </form>
            </DialogContent>
        </Dialog>
    );
}
