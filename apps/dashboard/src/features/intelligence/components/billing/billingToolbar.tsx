"use client";

import { useBillingIntelligenceStore } from "@/features/intelligence/stores/billingIntelligenceStore";
import Dropdown from "@/components/molecules/dropdown";

const PROVIDERS = ["AWS", "GCP"];

const PAST_TIME_PERIODS = [
    { value: "1", label: "Last 24 hours" },
    { value: "7", label: "Last 7 days" },
    { value: "30", label: "Last 30 days" },
    { value: "90", label: "Last 90 days" },
];

const FORECAST_TIME_PERIODS = [
    { value: "1", label: "Next 24 hours" },
    { value: "7", label: "Next 7 days" },
    { value: "14", label: "Next 14 days" },
    { value: "30", label: "Next 30 days" },
];

export default function BillingToolbar() {
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
        disableFilters,
        pastTimeWindowDays,
        forecastTimeWindowDays,
        setTimeWindows,
    } = useBillingIntelligenceStore();

    return (
        <header className="h-16 flex flex-row items-center justify-between">
            {!disableFilters && (
                <div className="flex flex-row gap-2">
                    <Dropdown
                        options={PROVIDERS.map((providers) => ({
                            value: providers,
                            label: providers,
                        }))}
                        value={provider}
                        onSelect={(value) => setProvider(value)}
                        placeholder="Select provider"
                        disableSearch={true}
                        widthVariant="medium"
                    />

                    <Dropdown
                        options={accounts.map((forAccount) => ({
                            value: forAccount.id,
                            label: forAccount.displayName,
                        }))}
                        value={accountId}
                        onSelect={(selectedVal) => {
                            const accountSelected = accounts.find(
                                (findAccount) => findAccount.id === selectedVal
                            );

                            if (accountSelected) {
                                setAccount(accountSelected.id, accountSelected.displayName);
                            }
                        }}
                        placeholder={isFetching && provider ? "Loading accounts" : "Select Account"}
                        disabled={!provider || isFetching}
                        widthVariant="medium"
                    />

                    <Dropdown
                        options={resources.map((forResources) => ({
                            value: forResources.id,
                            label: forResources.resourceName,
                        }))}
                        value={resourceId}
                        onSelect={(selectedVal) => {
                            const resourceSelected = resources.find(
                                (resource) => resource.id === selectedVal
                            );

                            if (resourceSelected) {
                                setResource(resourceSelected.id, resourceSelected.resourceName);
                            }
                        }}

                        placeholder={
                            isFetching && accountId ? "Loading resources" : "Select Resource"
                        }
                        disabled={!accountId || isFetching}
                        widthVariant="medium"
                    />
                </div>
            )}
            <div className={`${disableFilters ? "ml-auto" : ""} flex flex-row gap-2`}>
                <Dropdown
                    options={PAST_TIME_PERIODS}
                    value={pastTimeWindowDays.toString()}
                    onSelect={(selectedVal) =>
                        setTimeWindows(Number(selectedVal), forecastTimeWindowDays)
                    }
                    placeholder="Select past window"
                    disableSearch={true}
                    widthVariant="medium"
                />

                <Dropdown
                    options={FORECAST_TIME_PERIODS}
                    value={forecastTimeWindowDays.toString()}
                    onSelect={(selectedVal) =>
                        setTimeWindows(pastTimeWindowDays, Number(selectedVal))
                    }
                    placeholder="Select forecast window"
                    disableSearch={true}
                    widthVariant="medium"
                />
            </div>
        </header>
    );
}
