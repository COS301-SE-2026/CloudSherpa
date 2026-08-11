import { create } from "zustand";
import { MetricType } from "@/features/dashboard/types/metric";

import {
    getAwsAccountConnections,
    getAwsAccountResources,
    CloudAccount,
    CloudResource,
} from "@/lib/fetch/aws-connection-api";
import { persist } from "zustand/middleware";

interface UsageIntelligenceConfigStore {
    provider: string | null;
    accountId: string | null;
    resourceId: string | null;
    metricType: MetricType | null;
    accountDisplayName: string | null;
    resourceDisplayName: string | null;

    pastTimeWindowDays: number;

    accounts: CloudAccount[];
    resources: CloudResource[];
    isFetching: boolean;

    setProvider: (provider: string) => void;
    setAccount: (accountId: string, displayName: string) => void;
    setResource: (resourceId: string, displayName: string) => void;
    setMetricType: (metricType: MetricType) => void;
    setTimeWindows: (past: number, forecast: number) => void;
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
            metricType: null,
            accountDisplayName: null,
            resourceDisplayName: null,
            pastTimeWindowDays: 30,
            accounts: [],
            resources: [],
            isFetching: false,

            setProvider: (provider) => {
                set({
                    provider,
                    accountId: null,
                    resourceId: null,
                    metricType: null,
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
                    metricType: null,
                });
                get().fetchResources(accountId);
            },

            setResource: (resourceId, resourceDisplayName) =>
                set({
                    resourceId,
                    resourceDisplayName,
                    metricType: null,
                }),

            setMetricType: (metricType) =>
                set({
                    metricType,
                }),

            setTimeWindows: (past) =>
                set({
                    pastTimeWindowDays: past,
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
                    metricType: null,
                    accountDisplayName: null,
                    resourceDisplayName: null,
                    pastTimeWindowDays: 30,
                    accounts: [],
                    resources: [],
                    isFetching: false,
                }),
        }),
        { name: "billing_intelligence_config" }
    )
);
