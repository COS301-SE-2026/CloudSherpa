"use client";
import Dropdown from "@/components/molecules/dropdown";
import { useUsageIntelligenceStore } from "@/features/intelligence/stores/useUsageIntelligenceStore";
import { getAwsAccountConnections, CloudAccount } from "@/lib/fetch/aws-connection-api";
import { useState, useEffect } from "react";

//mock
const PROVIDERS = ["AWS", "GCP", "Azure"];

const PAST_PRESETS = [
    { value: "1", label: "Last 24 Hours" },
    { value: "7", label: "Last 7 Days" },
    { value: "30", label: "Last 30 Days" },
    { value: "90", label: "Last 90 Days" },
];

const FORECAST_PRESETS = [
    { value: "1", label: "Next 24 Hours" },
    { value: "7", label: "Next 7 Days" },
    { value: "14", label: "Next 14 Days" },
    { value: "30", label: "Next 30 Days" },
];

export default function UsageToolbar() {
    const {
        provider,
        setProvider,
        accountId,
        setAccount,
        resourceId,
        setResource,
        accounts,
        resources,
        isFetching,
        pastTimeWindowDays,
        forecastTimeWindowDays,
        setTimeWindows,
    } = useUsageIntelligenceStore();

    return (
        <header className=" h-16 flex flex-row items-center justify-between ">
            <div className="flex flex-row gap-2 ">
                <Dropdown
                    options={PROVIDERS.map((p) => ({ value: p, label: p }))}
                    value={provider}
                    onSelect={(val) => setProvider(val)}
                    placeholder="Select Provider..."
                    disableSearch={true}
                    widthVariant="small"
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
            </div>
            <div className="flex flex-row gap-2">
                <Dropdown
                    options={PAST_PRESETS}
                    value={pastTimeWindowDays.toString()}
                    onSelect={(val) => {
                        setTimeWindows(Number(val), forecastTimeWindowDays);
                    }}
                    placeholder="Select Past Window"
                    disableSearch={true}
                    widthVariant="medium"
                />
                <Dropdown
                    options={FORECAST_PRESETS}
                    value={forecastTimeWindowDays.toString()}
                    onSelect={(val) => {
                        setTimeWindows(pastTimeWindowDays, Number(val));
                    }}
                    placeholder="Select Forecast Window"
                    disableSearch={true}
                    widthVariant="medium"
                />
            </div>
        </header>
    );
}
