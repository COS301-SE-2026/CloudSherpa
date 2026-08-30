import { create } from "zustand";
import { MetricType } from "@/features/dashboard/types/metric";
import { persist } from "zustand/middleware";
import { TimeWindowPreset } from "@/features/dashboard/types/timewindow";
import { getAwsAccountConnections, getAwsAccountResources } from "@/lib/fetch/cloud-account-api";
import { CloudAccount } from "@/lib/fetch/dto/cloud-account";
import { CloudResource } from "@/lib/fetch/dto/cloud-resource";

interface UsageIntelligenceConfigStore {
    provider: string | null;
    accountId: string | null;
    resourceId: string | null;
    metricName: string | null;
    accountDisplayName: string | null;
    resourceDisplayName: string | null;

    pastTimeWindowPreset: TimeWindowPreset;

    accounts: CloudAccount[];
    resources: CloudResource[];
    isFetching: boolean;

    setProvider: (provider: string) => void;
    setAccount: (accountId: string, displayName: string) => void;
    setResource: (resourceId: string, displayName: string) => void;
    setMetricName: (metricName: string) => void;
    setTimeWindows: (past: TimeWindowPreset) => void;
    fetchAccounts: (provider: string) => Promise<void>;
    fetchResources: (accountId: string) => Promise<void>;
    reset: () => void;
}

export const useUsageIntelligenceConfigStore = create<UsageIntelligenceConfigStore>()(
    persist(
        (set, get) => ({
            provider: null,
            accountId: null,
            resourceId: null,
            metricName: null,
            accountDisplayName: null,
            resourceDisplayName: null,
            pastTimeWindowPreset: "T_6_HOUR",
            accounts: [],
            resources: [],
            isFetching: false,

            setProvider: (provider) => {
                set({
                    provider,
                    accountId: null,
                    resourceId: null,
                    metricName: null,
                    accounts: [],
                    resources: [],
                });
                get().fetchAccounts(provider);
            },

            setAccount: (accountId, accountDisplayName) => {
                set({
                    accountId,
                    accountDisplayName,
                    resourceId: null,
                    metricName: null,
                });
                get().fetchResources(accountId);
            },

            setResource: (resourceId, resourceDisplayName) =>
                set({
                    resourceId,
                    resourceDisplayName,
                    metricName: null,
                }),

            setMetricName: (metricName) =>
                set({
                    metricName,
                }),

            setTimeWindows: (past) =>
                set({
                    pastTimeWindowPreset: past,
                }),

            fetchAccounts: async (provider) => {
                if (provider !== "AWS") return;
                set({ isFetching: true });
                try {
                    const accounts = await getAwsAccountConnections();
                    set({ accounts, isFetching: false });
                } catch (error) {
                    console.error("Failed to fetch accounts:", error);
                    set({ isFetching: false });
                }
            },

            fetchResources: async (accountId) => {
                set({ isFetching: true });
                try {
                    const resources = await getAwsAccountResources(accountId);
                    set({ resources, isFetching: false });
                } catch (error) {
                    console.error("Failed to fetch resources:", error);
                    set({ isFetching: false });
                }
            },

            reset: () =>
                set({
                    provider: null,
                    accountId: null,
                    resourceId: null,
                    metricName: null,
                    accountDisplayName: null,
                    resourceDisplayName: null,
                    pastTimeWindowPreset: "T_6_HOUR",
                    accounts: [],
                    resources: [],
                    isFetching: false,
                }),
        }),
        { name: "billing_intelligence_config" }
    )
);
