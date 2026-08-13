import { create } from "zustand";
import {
    getAwsAccountConnections,
    getAwsAccountResources,
    CloudAccount,
    CloudResource,
} from "@/lib/fetch/aws-connection-api";
import {
    MOCK_ACCOUNTS,
    MOCK_RESOURCES,
    getMockBillingData,
    mockApiResponse,
    BillingSummaryDto,
    CostBreakdownItem,
} from "@/features/intelligence/mock/billingMockData";

const USE_MOCK = true;

interface BillingIntelligenceStore {
    provider: string | null;
    accountId: string | null;
    resourceId: string | null;
    accountDisplayName: string | null;
    resourceDisplayName: string | null;

    pastTimeWindowDays: number;
    forecastTimeWindowDays: number;
    accounts: CloudAccount[];
    resources: CloudResource[];
    isFetching: boolean;
    breakdownSearch: string;

    billingData: { forSummary: BillingSummaryDto; forBreakdown: CostBreakdownItem[] } | null;

    isLoading: boolean;
    error: string | null;
    disableFilters: boolean;

    setProvider: (provider: string) => void;
    setAccount: (accountId: string, displayName: string) => void;
    setBreakdownSearch: (search: string) => void;
    setTimeWindows: (past: number, forecast: number) => void;
    setResource: (resourceId: string, displayName: string) => void;
    fetchResources: (accountId: string) => Promise<void>;
    fetchAccounts: (accountId: string) => Promise<void>;
    fetchBillingData: () => Promise<void>;

    reset: () => void;
}

export const useBillingIntelligenceStore = create<BillingIntelligenceStore>((set, get) => ({
    provider: null,
    accountId: null,
    resourceId: null,

    accountDisplayName: null,
    resourceDisplayName: null,
    pastTimeWindowDays: 30,
    forecastTimeWindowDays: 7,

    accounts: [],
    resources: [],
    isFetching: false,
    breakdownSearch: "",

    isLoading: false,
    error: null,
    billingData: null,
    disableFilters: true,

    setProvider: (provider) => {
        set({
            provider,
            accountId: null,
            resourceId: null,
            accounts: [],
            resources: [],
            billingData: null,
        });

        get().fetchAccounts(provider);
    },

    setAccount: (accountId, accountDisplayName) => {
        set({
            accountId,
            accountDisplayName,
            resourceId: null,
            billingData: null,
        });

        get().fetchResources(accountId);
    },

    setResource: (resourceId, resourceDisplayName) =>
        set({
            resourceId,
            resourceDisplayName,
            billingData: null,
        }),

    setTimeWindows: (past, forecast) => {
        set({
            pastTimeWindowDays: past,
            forecastTimeWindowDays: forecast,
            billingData: null,
        });

        const { accountId } = get();
        if (accountId) {
            get().fetchBillingData();
        }
    },

    setBreakdownSearch: (breakdownSearch) => set({ breakdownSearch }),

    fetchAccounts: async (provider) => {
        if (provider !== "AWS") {
            return;
        }

        set({ isFetching: true });

        try {
            const accounts = USE_MOCK
                ? await mockApiResponse(MOCK_ACCOUNTS, 400)
                : await getAwsAccountConnections();

            set({ accounts, isFetching: false });
        } catch {
            set({ isFetching: false });
        }
    },

    fetchResources: async (accountId) => {
        set({ isFetching: true });

        try {
            const resources = USE_MOCK
                ? await mockApiResponse(MOCK_RESOURCES[accountId] ?? [], 300)
                : await getAwsAccountResources(accountId);

            set({ resources, isFetching: false });
        } catch {
            set({ isFetching: false });
        }
    },

    fetchBillingData: async () => {
        const { accountId, resourceId, pastTimeWindowDays, forecastTimeWindowDays } = get();

        if (!accountId) {
            return;
        }

        set({ isLoading: true, error: null });

        try {
            const data = USE_MOCK
                ? await mockApiResponse(
                      getMockBillingData(
                          accountId,
                          resourceId,
                          pastTimeWindowDays,
                          forecastTimeWindowDays
                      ),
                      600
                  )
                : (() => {
                      throw new Error("API not implemented");
                  })();

            set({ billingData: data, isLoading: false });
        } catch {
            set({
                isLoading: false,
            });
        }
    },

    reset: () =>
        set({
            provider: null,
            accountId: null,
            resourceId: null,
            billingData: null,
        }),
}));
