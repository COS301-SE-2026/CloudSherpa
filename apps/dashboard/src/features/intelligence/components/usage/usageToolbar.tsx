"use client";
import Dropdown from "@/components/molecules/dropdown";
import { useUsageIntelligenceConfigStore } from "@/features/intelligence/stores/useUsageIntelligenceConfigStore";
import { useMetricStore } from "@/features/dashboard/stores/metric-store";
import {
    Dialog,
    DialogTrigger,
    DialogContent,
    DialogHeader,
    DialogTitle,
} from "@/components/atoms/dialog";
import { Button } from "@/components/atoms/button";
import { Settings2 } from "lucide-react";
import { presets } from "@/lib/timeUtils";
import { TimeWindowPreset } from "@/features/dashboard/types/timewindow";
import { AccountType } from "@/lib/fetch/dto/cloud-account";

//mock
const PROVIDERS = [
    { label: "AWS", value: AccountType.AWS_ACCOUNT },
    { label: "GCP", value: AccountType.GCP_PROJECT },
    { label: "Azure", value: AccountType.AZURE_SUBSCRIPTION },
];

type DropdownWidth = "small" | "medium" | "large" | "full";

const PAST_PRESETS = presets.map((preset) => ({
    value: preset.id,
    label: preset.label,
}));

export default function UsageToolbar() {
    const {
        provider,
        setProvider,
        accountId,
        setAccount,
        resourceId,
        setResource,
        metricName,
        setMetricName,
        accounts,
        resources,
        isFetching,
        pastTimeWindowPreset,
        setTimeWindows,
    } = useUsageIntelligenceConfigStore();

    const getMetricList = useMetricStore((state) => state.getMetricList);
    const metricsByResource = getMetricList();
    const availableMetrics = resourceId ? (metricsByResource[resourceId] ?? []) : [];

    const renderProviderDropdown = (width: DropdownWidth) => (
        <Dropdown
            options={PROVIDERS.map((p) => ({ value: p.value, label: p.label }))}
            value={provider}
            onSelect={(val) => setProvider(val as AccountType)}
            placeholder="Provider..."
            disableSearch={true}
            widthVariant={width}
        />
    );

    const renderAccountDropdown = (width: DropdownWidth) => (
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
            placeholder={isFetching && provider ? "Loading accounts..." : "Select Account..."}
            disabled={!provider || isFetching}
            widthVariant={width}
        />
    );

    const renderResourceDropdown = (width: DropdownWidth) => (
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
            placeholder={isFetching && accountId ? "Loading resources..." : "Select Resource..."}
            disabled={!accountId || isFetching}
            widthVariant={width}
        />
    );

    const renderMetricDropdown = (width: DropdownWidth) => (
        <Dropdown
            options={availableMetrics.map((metric) => ({
                value: metric,
                label: metric.toUpperCase(),
            }))}
            value={metricName}
            onSelect={(val) => setMetricName(val)}
            placeholder={"Select Metric..."}
            disabled={!resourceId || isFetching}
            widthVariant={width}
        />
    );

    const renderTimeWindowDropdown = (width: DropdownWidth) => (
        <Dropdown
            options={PAST_PRESETS}
            value={pastTimeWindowPreset}
            onSelect={(val) => {
                setTimeWindows(val as TimeWindowPreset);
            }}
            placeholder="Select Past Window"
            disableSearch={true}
            widthVariant={width}
        />
    );

    return (
        <header className=" h-16 flex flex-row items-center justify-between">
            <div className="hidden lg:flex flex-row items-center justify-between w-full">
                <div className="flex flex-row gap-2">
                    {renderProviderDropdown("small")}
                    {renderAccountDropdown("large")}
                    {renderResourceDropdown("large")}
                    {renderMetricDropdown("medium")}
                </div>
                <div className="flex flex-row gap-2">{renderTimeWindowDropdown("medium")}</div>
            </div>

            <div className="flex lg:hidden flex-row items-center justify-end w-full">
                <Dialog>
                    <DialogTrigger asChild>
                        <Button variant="outline" className="gap-2">
                            <Settings2 className="h-4 w-4" />
                            Configure
                        </Button>
                    </DialogTrigger>
                    <DialogContent className="sm:max-w-[425px]">
                        <DialogHeader>
                            <DialogTitle>Configure Chart Data</DialogTitle>
                        </DialogHeader>
                        <div className="flex flex-col gap-4 py-4 items-stretch w-full">
                            {renderProviderDropdown("full")}
                            {renderAccountDropdown("full")}
                            {renderResourceDropdown("full")}
                            {renderMetricDropdown("full")}
                            {renderTimeWindowDropdown("full")}
                        </div>
                    </DialogContent>
                </Dialog>
            </div>
        </header>
    );
}
