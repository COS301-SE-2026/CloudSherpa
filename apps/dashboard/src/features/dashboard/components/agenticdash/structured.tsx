import { useState, useMemo } from "react";
import Dropdown from "@/components/molecules/dropdown";
import { Label } from "@/components/atoms/label";
import { CloudAccount } from "@/lib/fetch/dto/cloud-account";
import { CloudResource, ResourceStatus } from "@/lib/fetch/dto/cloud-resource";
import { getAwsAccountConnections, getAwsAccountResources } from "@/lib/fetch/cloud-account-api";
import { useMetricStore } from "@/features/dashboard/stores/metric-store";
import { MetricStore } from "@/features/dashboard/types/metric";
import { toast } from "sonner";

const PROVIDERS = ["AWS", "AZURE", "GCP"];

const PROVIDER_MAP: Record<string, string> = {
    AWS: "AWS_ACCOUNT",
    AZURE: "AZURE_SUBSCRIPTION",
    GCP: "GCP_PROJECT",
};
//dictionary placeholder
const THEMES = [
    {
        id: "cpu",
        label: "CPU utilization",
        metricNames: [
            "CPUUtilization",
            "Percentage CPU",
            "cpu_percent",
            "node_cpu_usage_percentage",
            "compute.googleapis.com/instance/cpu/utilization",
        ],
    },
    {
        id: "network",
        label: "Network traffic",
        metricNames: [
            "NetworkIn",
            "NetworkOut",
            "Network In Total",
            "Network Out Total",
            "compute.googleapis.com/instance/network/received_bytes_count",
            "compute.googleapis.com/instance/network/sent_bytes_count",
        ],
    },
];

export default function StructuredRequest() {
    const [provider, setProvider] = useState<string | null>(null);
    const [accountIds, setAccountIds] = useState<string[]>([]);
    const [resourceIds, setResourceIds] = useState<string[]>([]);
    const [themeIds, setThemeIds] = useState<string[]>([]);

    const [connections, setConnections] = useState<CloudAccount[]>([]);
    const [resources, setResources] = useState<CloudResource[]>([]);

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

    const connectionOptions = useMemo(() => {
        if (!provider) return [];
        const providertype = PROVIDER_MAP[provider];

        return connections
            .filter((c) => {
                const isCurrentProvider = (c.accountType || "").toUpperCase() === providertype;
                const isSelected = accountIds.includes(c.id);
                return isCurrentProvider || isSelected;
            })
            .map((c) => ({ value: c.id, label: c.displayName }));
    }, [connections, provider, accountIds]);

    const handleProviderSelect = (val: string | string[]) => {
        const value = Array.isArray(val) ? val[0] : val;
        if (!value) return;

        const next = value.toUpperCase();

        setProvider(next);

        getAwsAccountConnections()
            .then((all) => {
                setConnections(all);
            })
            .catch(() => toast.error("Failed to load connections."));
    };

    const handleAccountSelect = (val: string | string[]) => {
        const selectedAccountIds = Array.isArray(val) ? val : [val];

        setAccountIds(selectedAccountIds);

        if (selectedAccountIds.length === 0) return;

        Promise.all(selectedAccountIds.map((id) => getAwsAccountResources(id)))
            .then((results) => {
                const allResources = results
                    .flat()
                    .filter((r) => r.status === ResourceStatus.ACTIVE);
                setResources(allResources);

                setResourceIds((prev) =>
                    prev.filter((id) => allResources.some((r) => r.id === id))
                );
            })
            .catch(() => toast.error("Failed to load resources."));
    };

    const handleResourceSelect = (val: string | string[]) => {
        const selectedResourceIds = Array.isArray(val) ? val : [val];
        setResourceIds(selectedResourceIds);
    };

    const handleThemeSelect = (val: string | string[]) => {
        const selectedThemeIds = Array.isArray(val) ? val : [val];
        setThemeIds(selectedThemeIds);
    };

    return (
        <div className="h-full flex flex-col justify-start pt-4 gap-4">
            <div className="grid gap-2 w-full">
                <Label>Provider</Label>
                <Dropdown
                    value={provider}
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
                    options={connections.map((c) => ({ value: c.id, label: c.displayName }))}
                    onSelect={handleAccountSelect}
                    disabled={!provider}
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
                    options={availableThemes.map((t) => ({ value: t.id, label: t.label }))}
                    onSelect={handleThemeSelect}
                    disabled={resourceIds.length === 0}
                    disableSearch={true}
                    widthVariant="full"
                    placeholder="Select Theme"
                    emptyMessage="No themes available for this resource"
                />
            </div>
        </div>
    );
}
