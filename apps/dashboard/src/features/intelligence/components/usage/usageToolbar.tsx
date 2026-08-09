"use client";
import Dropdown from "@/components/molecules/dropdown";
import { useUsageIntelligenceConfigStore } from "@/features/intelligence/stores/useUsageIntelligenceConfigStore";
import { MetricType } from "@/features/dashboard/types/metric";
import { useMetricStore } from "@/features/dashboard/stores/metric-store";

//mock
const PROVIDERS = ["AWS", "GCP", "Azure"];

const PAST_PRESETS = [
    { value: "1", label: "Last 24 Hours" },
    { value: "7", label: "Last 7 Days" },
    { value: "30", label: "Last 30 Days" },
    { value: "90", label: "Last 90 Days" },
];

export default function UsageToolbar() {
    const {
        provider,
        setProvider,
        accountId,
        setAccount,
        resourceId,
        setResource,
        metricType,
        setMetricType,
        accounts,
        resources,
        isFetching,
        pastTimeWindowDays,
        setTimeWindows,
    } = useUsageIntelligenceConfigStore();

    const getMetricList = useMetricStore((state) => state.getMetricList);
    const metricsByResource = getMetricList();
    const availableMetrics = resourceId ? (metricsByResource[resourceId] ?? []) : [];

    return (
        <header className=" h-16 flex flex-row items-center justify-between ">
            <div className="flex flex-row gap-2 ">
                <Dropdown
                    options={PROVIDERS.map((p) => ({ value: p, label: p }))}
                    value={provider}
                    onSelect={(val) => setProvider(val)}
                    placeholder="Select Provider..."
                    disableSearch={true}
                    widthVariant="medium"
                />
                <Dropdown
                    options={accounts.map((acc) => ({
                        value: acc.id,
                        label: acc.displayName,
                    }))}
                    value={accountId}
                    onSelect={(val) => {
                        const selectedAcc = accounts.find((a) => a.id === val);
                        if (selectedAcc) {
                            setAccount(selectedAcc.id, selectedAcc.displayName);
                        }
                    }}
                    placeholder={
                        isFetching && provider ? "Loading accounts..." : "Select Account..."
                    }
                    disabled={!provider || isFetching}
                    widthVariant="large"
                />
                <Dropdown
                    options={resources.map((res) => ({
                        value: res.id,
                        label: res.resourceName,
                    }))}
                    value={resourceId}
                    onSelect={(val) => {
                        const selectedRes = resources.find((r) => r.id === val);
                        if (selectedRes) {
                            setResource(selectedRes.id, selectedRes.resourceName);
                        }
                    }}
                    placeholder={
                        isFetching && accountId ? "Loading resources..." : "Select Resource..."
                    }
                    disabled={!accountId || isFetching}
                    widthVariant="large"
                />
                <Dropdown
                    options={availableMetrics.map((metric) => ({
                        value: metric,
                        label: metric.toUpperCase(),
                    }))}
                    value={metricType}
                    onSelect={(val) => setMetricType(val as MetricType)}
                    placeholder={"Select Metric..."}
                    disabled={!resourceId || isFetching}
                    widthVariant="medium"
                />
            </div>
            <div className="flex flex-row gap-2">
                <Dropdown
                    options={PAST_PRESETS}
                    value={pastTimeWindowDays.toString()}
                    onSelect={(val) => {
                        setTimeWindows(Number(val), pastTimeWindowDays);
                    }}
                    placeholder="Select Past Window"
                    disableSearch={true}
                    widthVariant="medium"
                />
            </div>
        </header>
    );
}
