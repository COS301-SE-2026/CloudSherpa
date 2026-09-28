import { useState, useMemo, useEffect } from "react";
import Dropdown from "@/components/molecules/dropdown";
import { Label } from "@/components/atoms/label";
import { CloudAccount } from "@/lib/fetch/dto/cloud-account";
import { CloudResource, ResourceStatus } from "@/lib/fetch/dto/cloud-resource";
import { getAwsAccountConnections, getAwsAccountResources } from "@/lib/fetch/cloud-account-api";
import { useMetricStore } from "@/features/dashboard/stores/metric-store";
import { MetricStore } from "@/features/dashboard/types/metric";
import { toast } from "sonner";
import { THEMES } from "@/features/dashboard/types/agentic";
import { useToolbar } from "@/features/dashboard/components/toolbar/toolbarProvider";
import { useDashboardStore } from "@/features/dashboard/stores/dashboard-store";
import ApplyDashboardDialog from "@/features/dashboard/components/agenticdash/applyDashboardDialog";
import { generatePrompt, ResourceMeta } from "@/features/dashboard/utils/structuredPromptGenerater";
import { AiDashboardApplyMode } from "@/features/dashboard/types/agentic";
import { Button } from "@/components/atoms/button";
import { Loader2, X } from "lucide-react";

const PROVIDERS = ["AWS", "AZURE", "GCP"];

const PROVIDER_MAP: Record<string, string> = {
    AWS: "AWS_ACCOUNT",
    AZURE: "AZURE_SUBSCRIPTION",
    GCP: "GCP_PROJECT",
};

export default function StructuredRequest() {
    const { isApplyDialogOpen, setIsApplyDialogOpen } = useToolbar();
    const [isApplying, setIsApplying] = useState(false);

    const [providers, setProviders] = useState<string[]>([]);
    const [accountIds, setAccountIds] = useState<string[]>([]);
    const [resourceIds, setResourceIds] = useState<string[]>([]);
    const [themeIds, setThemeIds] = useState<string[]>([]);

    const [connections, setConnections] = useState<CloudAccount[]>([]);
    const [resources, setResources] = useState<CloudResource[]>([]);

    const isSessionActive = useDashboardStore((state) => state.isSessionActive);
    const isGenerating = useDashboardStore((state) => state.isGenerating);
    const currentVersionId = useDashboardStore((state) => state.currentVersionId);
    const versions = useDashboardStore((state) => state.versions);

    const [userInstruction, setUserInstruction] = useState("");

    const { startSessionAndGenerate, sendPrompt, applyDashboard, cancelSession } =
        useDashboardStore((state) => state.agenticActions);

    const getMetricList = useMetricStore((state: MetricStore) => state.getMetricList);

    const availableMetrics = useMemo(() => {
        if (resourceIds.length === 0) return [];
        const metricList = getMetricList();
        const metricsSet = new Set<string>();
        resourceIds.forEach((id) => {
            const list = metricList[id] ?? [];
            list.forEach((m) => metricsSet.add(m));
        });
        return Array.from(metricsSet);
    }, [resourceIds, getMetricList]);

    const availableThemes = useMemo(() => {
        return THEMES.filter((theme) =>
            theme.metricNames.some((name) => availableMetrics.includes(name))
        );
    }, [availableMetrics]);

    const currentVersion = versions.find((version) => version.versionId === currentVersionId);

    const canApplyCurrentVersion =
        isSessionActive && currentVersion !== undefined && currentVersion.version > 0;

    const handleGenerate = async () => {
        if (isGenerating || providers.length === 0 || resourceIds.length === 0) {
            toast.error("Please select a provider and at least one resource.");
            return;
        }

        const taskDescription =
            userInstruction.trim() ||
            `Create a comprehensive dashboard analyzing the selected cloud infrastructure focusing on key performance indicators.`;

        const selectedThemes = THEMES.filter((theme) => themeIds.includes(theme.id));

        const metricList = getMetricList();

        const mappedResources: ResourceMeta[] = resources
            .filter((resource) => resourceIds.includes(resource.id))
            .map((resource) => ({
                id: resource.id,
                name: resource.resourceName,
            }));

        const metricsByResource: Record<string, string[]> = {};

        mappedResources.forEach((resource) => {
            metricsByResource[resource.id] = metricList[resource.id] ?? [];
        });

        const selectedConnections = connections
            .filter((connection) => accountIds.includes(connection.id))
            .map((connection) => ({
                id: connection.id,
                name: connection.displayName,
            }));

        const generatedPrompt = generatePrompt({
            taskDescription,
            providers: providers,
            connections: selectedConnections,
            resources: mappedResources,
            themes: selectedThemes,
            metricsByResource,
        });

        if (!generatedPrompt.trim()) {
            toast.error("Failed to generate a valid prompt.");
            return;
        }

        if (isSessionActive) {
            await sendPrompt(generatedPrompt);
        } else {
            await startSessionAndGenerate(generatedPrompt);
        }

        setUserInstruction("");
    };

    const handleApply = async (mode: AiDashboardApplyMode): Promise<void> => {
        if (!currentVersionId) return;

        setIsApplying(true);
        try {
            const applied = await applyDashboard(currentVersionId, mode);
            if (applied) {
                setIsApplyDialogOpen(false);
            }
        } finally {
            setIsApplying(false);
        }
    };

    const connectionOptions = useMemo(() => {
        if (providers.length === 0) return [];

        const selectedProviderTypes = new Set(providers.map((provider) => PROVIDER_MAP[provider]));

        return connections
            .filter((connection) =>
                selectedProviderTypes.has((connection.accountType || "").toUpperCase())
            )
            .map((connection) => ({
                value: connection.id,
                label: connection.displayName,
            }));
    }, [connections, providers]);

    const handleProviderSelect = (val: string | string[]) => {
        const selectedProviders = (Array.isArray(val) ? val : [val])
            .filter(Boolean)
            .map((value) => value.toUpperCase());

        setProviders(selectedProviders);

        setAccountIds((previous) => {
            if (selectedProviders.length === 0) {
                return [];
            }

            const selectedProviderTypes = new Set(
                selectedProviders.map((provider) => PROVIDER_MAP[provider])
            );

            return previous.filter((accountId) => {
                const connection = connections.find((connection) => connection.id === accountId);

                return (
                    connection &&
                    selectedProviderTypes.has((connection.accountType || "").toUpperCase())
                );
            });
        });

        setResources([]);
        setResourceIds([]);
        setThemeIds([]);

        if (selectedProviders.length === 0) {
            return;
        }

        getAwsAccountConnections()
            .then((all) => {
                setConnections(all);
            })
            .catch(() => toast.error("Failed to load connections."));
    };

    const handleAccountSelect = (val: string | string[]) => {
        const selectedAccountIds = Array.isArray(val) ? val : [val];
        setAccountIds(selectedAccountIds);
    };

    const handleResourceSelect = (val: string | string[]) => {
        const selectedResourceIds = Array.isArray(val) ? val : [val];

        setResourceIds(selectedResourceIds);

        const metricList = getMetricList();
        const selectedMetrics = new Set<string>();

        selectedResourceIds.forEach((id) => {
            const metrics = metricList[id] ?? [];

            metrics.forEach((metric) => {
                selectedMetrics.add(metric);
            });
        });

        setThemeIds((previous) =>
            previous.filter((themeId) => {
                const theme = THEMES.find((theme) => theme.id === themeId);

                return (
                    theme !== undefined &&
                    theme.metricNames.some((metric) => selectedMetrics.has(metric))
                );
            })
        );
    };

    const handleThemeSelect = (val: string | string[]) => {
        const selectedThemeIds = Array.isArray(val) ? val : [val];
        setThemeIds(selectedThemeIds);
    };

    useEffect(() => {
        if (accountIds.length === 0) {
            return;
        }

        let cancelled = false;

        Promise.all(accountIds.map((id) => getAwsAccountResources(id)))
            .then((results) => {
                if (cancelled) {
                    return;
                }

                const allResources = results
                    .flat()
                    .filter((resource) => resource.status === ResourceStatus.ACTIVE);

                setResources(allResources);

                setResourceIds((previous) =>
                    previous.filter((id) => allResources.some((resource) => resource.id === id))
                );

                setThemeIds([]);
            })
            .catch(() => {
                if (!cancelled) {
                    toast.error("Failed to load resources.");
                }
            });

        return () => {
            cancelled = true;
        };
    }, [accountIds]);

    return (
        <>
            <div className="h-full flex flex-col justify-start pt-4 gap-4">
                <div className="grid gap-2 w-full">
                    <Label>Provider</Label>
                    <Dropdown
                        multiple
                        value={providers}
                        options={PROVIDERS.map((p) => ({ value: p, label: p }))}
                        onSelect={handleProviderSelect}
                        disableSearch={true}
                        widthVariant="full"
                        placeholder="Select Provider"
                    />
                </div>
                <div className="grid gap-2 w-full">
                    <Label>Connection</Label>
                    <Dropdown
                        multiple
                        value={accountIds}
                        options={connectionOptions}
                        onSelect={handleAccountSelect}
                        disabled={!providers}
                        widthVariant="full"
                        placeholder="Select Connection"
                        emptyMessage="No connections found"
                    />
                </div>
                <div className="grid gap-2 w-full">
                    <Label>Resource</Label>
                    <Dropdown
                        multiple
                        value={resourceIds}
                        options={resources.map((r) => ({ value: r.id, label: r.resourceName }))}
                        onSelect={handleResourceSelect}
                        disabled={accountIds.length === 0}
                        widthVariant="full"
                        placeholder="Select Resource"
                        emptyMessage="No resources found"
                    />
                </div>
                <div className="grid gap-2 w-full">
                    <Label>Theme</Label>
                    <Dropdown
                        multiple
                        value={themeIds}
                        options={availableThemes.map((theme) => ({
                            value: theme.id,
                            label: theme.label,
                        }))}
                        onSelect={handleThemeSelect}
                        disabled={resourceIds.length === 0}
                        disableSearch={true}
                        widthVariant="full"
                        placeholder="Select Theme"
                        emptyMessage="No themes available for this resource"
                    />
                </div>
                <div className="h-full w-full flex flex flex-col justify-end items-end gap-2">
                    {/* {isSessionActive && (
                        <div className="w-full flex items-center justify-between px-3 py-2 bg-muted/30 border rounded-md">
                            <span className="text-muted-foreground text-xs font-medium">
                                {currentVersion?.version === 0
                                    ? "Viewing the dashboard you started with"
                                    : `Viewing version v${currentVersion?.version ?? ""}`}
                            </span>

                            <div className="flex items-center gap-1">
                                <Button
                                    variant="destructive"
                                    className="h-7"
                                    size="sm"
                                    onClick={() => cancelSession()}
                                    disabled={isGenerating || isApplying}
                                >
                                    <X />
                                    Discard
                                </Button>

                                {canApplyCurrentVersion && (
                                    <Button
                                        size="sm"
                                        className="h-7"
                                        onClick={() => setIsApplyDialogOpen(true)}
                                        disabled={isGenerating || isApplying}
                                    >
                                        Save
                                    </Button>
                                )}
                            </div>
                        </div>
                    )} */}
                    <Button
                        variant="default"
                        onClick={handleGenerate}
                        disabled={isGenerating || isApplying}
                        className="mx-3 my-2"
                    >
                        {isGenerating ? <Loader2 className="w-4 h-4 animate-spin" /> : "Generate"}
                    </Button>
                </div>
            </div>
            {canApplyCurrentVersion && currentVersion && (
                <ApplyDashboardDialog
                    open={isApplyDialogOpen}
                    dashboardName={currentVersion.title}
                    isApplying={isApplying}
                    onOpenChange={setIsApplyDialogOpen}
                    onApply={handleApply}
                />
            )}
        </>
    );
}
