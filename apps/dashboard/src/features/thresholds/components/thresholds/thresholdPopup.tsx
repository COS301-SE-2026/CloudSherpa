"use client";

import { useState, useEffect } from "react";
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
    CreateThresholdRequest,
    Threshold,
    OperatorsForThreshold,
    SeverityForThreshold,
    OPERATORS,
    SEVERITY,
    OPERATOR_LABEL,
} from "@/features/thresholds/types/thresholdTypes";
import { getAwsAccountConnections, getAwsAccountResources } from "@/lib/fetch/cloud-account-api";
import { CloudAccount } from "@/lib/fetch/dto/cloud-account";
import { CloudResource, ResourceStatus } from "@/lib/fetch/dto/cloud-resource";
import { useMetricStore } from "@/features/dashboard/stores/metric-store";
import { MetricStore } from "@/features/dashboard/types/metric";
import Dropdown from "@/components/molecules/dropdown";

const PROVIDERS = ["AWS", "AZURE", "GCP"];

const PROVIDER_MAP: Record<string, string> = {
    AWS: "AWS_ACCOUNT",
    AZURE: "AZURE_SUBSCRIPTION",
    GCP: "GCP_PROJECT",
};

const REVERSE_PROVIDER_MAP: Record<string, string> = {
    AWS_ACCOUNT: "AWS",
    AZURE_SUBSCRIPTION: "AZURE",
    GCP_PROJECT: "GCP",
};

interface PropsForThresholds {
    open: boolean;
    initial?: Threshold | null;
    resourceId?: string;
    initialMetricName?: string;
    userId: string;
    onClose: () => void;
    onSubmit: (thresholdPayload: CreateThresholdRequest) => Promise<void>;
}

export function ThresholdPopup({
    open,
    initial,
    resourceId: resourceIdPreset,
    initialMetricName,
    userId,
    onClose,
    onSubmit,
}: Readonly<PropsForThresholds>) {
    const [metricName, setMetricName] = useState(initial?.metricName ?? initialMetricName ?? "");

    const [operator, setOperator] = useState<OperatorsForThreshold>(initial?.operator ?? "GT");

    const [value, setValue] = useState<number>(initial?.value ?? 0);

    const [severity, setSeverity] = useState<SeverityForThreshold>(initial?.severity ?? "WARNING");

    const [enabled, setEnabled] = useState(initial?.enabled ?? true);

    const [submit, setSubmit] = useState(false);

    const [metricError, setMetricError] = useState<string | null>(null);

    const [zeroValue, setZeroValue] = useState<string | null>(null);

    const [provider, setProvider] = useState<string | null>(null);

    const [accountId, setAccountId] = useState<string | null>(null);

    const [resourceId, setResourceId] = useState<string | null>(resourceIdPreset ?? null);

    const [connection, setConnection] = useState<CloudAccount[]>([]);

    const [activeResource, setActiveResource] = useState<CloudResource[]>([]);

    const allAvailableMetrics = useMetricStore((store: MetricStore) => store.getMetricList);

    const availableMetrics = resourceId ? (allAvailableMetrics()[resourceId] ?? []) : [];

    const [resourceError, setResourceError] = useState<string | null>(null);

    const resolvedResourceId = resourceIdPreset ?? resourceId;

    useEffect(() => {
        if (!open) {
            return;
        }

        if (!initial?.resourceId) {
            return;
        }

        if (provider) {
            return;
        }

        let cancelled = false;

        (async () => {
            const allConnections = await getAwsAccountConnections();

            for (const connect of allConnections) {
                if (cancelled) {
                    return;
                }

                const resources = await getAwsAccountResources(connect.id);

                if (cancelled) {
                    return;
                }

                if (resources.some((res) => res.id === initial.resourceId)) {
                    const providerKey =
                        REVERSE_PROVIDER_MAP[(connect.accountType || "").toUpperCase()];

                    if (providerKey) {
                        setProvider(providerKey);
                        setAccountId(connect.id);
                        setResourceId(initial.resourceId);
                    }

                    return;
                }
            }
        })();

        return () => {
            cancelled = true;
        };
    }, [open, initial?.resourceId, provider]);

    useEffect(() => {
        if (!provider) {
            return;
        }

        getAwsAccountConnections().then((gettingConnections) => {
            const forTarget = PROVIDER_MAP[provider];

            const forFiltered = gettingConnections.filter(
                (conn) => (conn.accountType || "").toUpperCase() === forTarget
            );

            setConnection(forFiltered);
        });
    }, [provider]);

    useEffect(() => {
        if (!accountId) {
            return;
        }

        getAwsAccountResources(accountId).then((resources) => {
            const active = resources.filter(
                (forResources) => forResources.status === ResourceStatus.ACTIVE
            );

            setActiveResource(active);
        });
    }, [accountId]);

    const handlingProviderChange = (current: string) => {
        setProvider(current.toUpperCase());
        setAccountId(null);
        setResourceId(null);
        setMetricName("");
    };

    const handlingAccountChange = (current: string) => {
        setAccountId(current);
        setResourceId(null);
        setMetricName("");
    };

    const handlingResourceChange = (current: string) => {
        setResourceId(current);
        setMetricName("");

        if (resourceError) {
            setResourceError(null);
        }
    };

    const handlingSubmit = async (submitting: React.FormEvent) => {
        submitting.preventDefault();

        let hasError = false;

        if (metricName.trim() === "") {
            setMetricError("Please fill out this field");

            hasError = true;
        }

        if (!Number.isFinite(value) || value < 0) {
            setZeroValue("Value must be 0 or greater");

            hasError = true;
        }

        if (hasError) {
            return;
        }

        setMetricError(null);

        setZeroValue(null);

        setSubmit(true);

        try {
            await onSubmit({
                resourceId: resolvedResourceId as string,
                userId,
                metric_name: metricName,
                operator,
                value: Number(value),
                severity,
                enabled,
            });

            onClose();
        } finally {
            setSubmit(false);
        }
    };

    return (
        <Dialog open={open} onOpenChange={(next) => !next && onClose()}>
            <DialogContent className="sm:max-w-md">
                <DialogHeader>
                    <DialogTitle> {initial ? "Edit threshold" : "New threshold"} </DialogTitle>
                </DialogHeader>

                <form onSubmit={handlingSubmit} className="space-y-4" noValidate>
                    <div className="grid gap-2">
                        <Label> Provider </Label>

                        <Dropdown
                            value={provider}
                            options={PROVIDERS.map((provider) => ({
                                value: provider,
                                label: provider,
                            }))}
                            onSelect={handlingProviderChange}
                            disableSearch={true}
                            widthVariant="full"
                            placeholder="Select provider"
                        />
                    </div>

                    <div className="grid gap-2">
                        <Label> Connection </Label>

                        <Dropdown
                            value={accountId}
                            options={connection.map((connections) => ({
                                value: connections.id,
                                label: connections.displayName,
                            }))}
                            onSelect={handlingAccountChange}
                            disabled={!provider}
                            widthVariant="full"
                            placeholder="Select connection"
                            emptyMessage="No connections found"
                        />
                    </div>

                    <div className="grid gap-2">
                        <Label> Resource </Label>

                        <Dropdown
                            value={resourceId}
                            options={activeResource.map((resource) => ({
                                value: resource.id,
                                label: resource.resourceName,
                            }))}
                            onSelect={handlingResourceChange}
                            disabled={!accountId}
                            widthVariant="full"
                            placeholder="Select resource"
                            emptyMessage="No resources found"
                        />

                        {resourceError && (
                            <p className="text-xs text-destructive"> {resourceError} </p>
                        )}
                    </div>

                    <div className="space-y-2">
                        <Label htmlFor="metricName"> Metric </Label>

                        <Dropdown
                            value={metricName || null}
                            options={availableMetrics.map((type) => ({ value: type, label: type }))}
                            onSelect={(change) => {
                                setMetricName(change as string);
                                if (metricError) {
                                    setMetricError(null);
                                }
                            }}
                            disabled={!resolvedResourceId}
                            widthVariant="full"
                            placeholder="Select metric"
                            emptyMessage="No metric found"
                        />

                        {metricError && (
                            <p id="metricName-error" className="text-xs text-destructive">
                                {" "}
                                {metricError}{" "}
                            </p>
                        )}
                    </div>

                    <div className="flex items-start gap-2">
                        <div className="w-16 space-y-2">
                            <Label htmlFor="operator"> Operator </Label>

                            <Select
                                value={operator}
                                onValueChange={(change) =>
                                    setOperator(change as OperatorsForThreshold)
                                }
                            >
                                <SelectTrigger id="operator">
                                    <SelectValue placeholder="Select operator" />
                                </SelectTrigger>

                                <SelectContent>
                                    {OPERATORS.map((forOperators) => (
                                        <SelectItem key={forOperators} value={forOperators}>
                                            {" "}
                                            {OPERATOR_LABEL[forOperators]}{" "}
                                        </SelectItem>
                                    ))}
                                </SelectContent>
                            </Select>
                        </div>

                        <div className="flex-1 space-y-2">
                            <Label htmlFor="value"> Value </Label>

                            <Input
                                id="value"
                                type="number"
                                step="any"
                                min={0}
                                value={value}
                                onChange={(change) => {
                                    setValue(Number(change.target.value));
                                    if (zeroValue) {
                                        setZeroValue(null);
                                    }
                                }}
                            />

                            {zeroValue && (
                                <p id="zero-error" className="text-xs text-destructive">
                                    {" "}
                                    {zeroValue}{" "}
                                </p>
                            )}
                        </div>
                    </div>

                    <div className="space-y-2">
                        <Label htmlFor="severity"> Severity </Label>

                        <Select
                            value={severity}
                            onValueChange={(change) => setSeverity(change as SeverityForThreshold)}
                        >
                            <SelectTrigger id="severity">
                                <SelectValue placeholder="Select severity" />
                            </SelectTrigger>

                            <SelectContent>
                                {SEVERITY.map((forSeverity) => (
                                    <SelectItem key={forSeverity} value={forSeverity}>
                                        {" "}
                                        {forSeverity}{" "}
                                    </SelectItem>
                                ))}
                            </SelectContent>
                        </Select>
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

                        <Button type="submit" disabled={submit}>
                            {" "}
                            {submit ? "Saving" : "Save"}{" "}
                        </Button>
                    </DialogFooter>
                </form>
            </DialogContent>
        </Dialog>
    );
}
