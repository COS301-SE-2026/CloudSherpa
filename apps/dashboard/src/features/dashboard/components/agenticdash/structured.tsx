import { useState } from "react";
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
    const [accountId, setAccountId] = useState<string | null>(null);
    const [resourceId, setResourceId] = useState<string | null>(null);
    const [themeId, setThemeId] = useState<string | null>(null);

    const [connections, setConnections] = useState<CloudAccount[]>([]);
    const [resources, setResources] = useState<CloudResource[]>([]);

    const getMetricList = useMetricStore((state: MetricStore) => state.getMetricList);

    const availableMetrics: string[] = resourceId ? (getMetricList()[resourceId] ?? []) : [];
    const availableThemes = THEMES.filter((theme) =>
        theme.metricNames.some((name) => availableMetrics.includes(name))
    );

    const handleProviderSelect = (value: string) => {
        const next = value.toUpperCase();

        setProvider(next);
        setAccountId(null);
        setResourceId(null);
        setThemeId(null);
        setConnections([]);
        setResources([]);

        getAwsAccountConnections()
            .then((all) => {
                const targetType = PROVIDER_MAP[next];
                setConnections(
                    all.filter((conn) => (conn.accountType || "").toUpperCase() === targetType)
                );
            })
            .catch(() => toast.error("Failed to load connections."));
    };

    const handleAccountSelect = (value: string) => {
        setAccountId(value);
        setResourceId(null);
        setThemeId(null);
        setResources([]);

        getAwsAccountResources(value)
            .then((all) => setResources(all.filter((r) => r.status === ResourceStatus.ACTIVE)))
            .catch(() => toast.error("Failed to load resources."));
    };

    const handleResourceSelect = (value: string) => {
        setResourceId(value);
        setThemeId(null);
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
                    value={accountId}
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
                    value={resourceId}
                    options={resources.map((r) => ({ value: r.id, label: r.resourceName }))}
                    onSelect={handleResourceSelect}
                    disabled={!accountId}
                    widthVariant="full"
                    placeholder="Select Resource"
                    emptyMessage="No resources found"
                />
            </div>
            <div className="grid gap-2 w-full">
                <Label>Theme</Label>
                <Dropdown
                    value={themeId}
                    options={availableThemes.map((t) => ({ value: t.id, label: t.label }))}
                    onSelect={setThemeId}
                    disabled={!resourceId}
                    disableSearch={true}
                    widthVariant="full"
                    placeholder="Select Theme"
                    emptyMessage="No themes available for this resource"
                />
            </div>
        </div>
    );
}
