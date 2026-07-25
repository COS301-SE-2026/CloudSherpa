import { create } from "zustand";
import { devtools } from "zustand/middleware";
import {
    getAwsAccountConnections,
    getAwsAccountResources,
    CloudAccount,
    CloudResource,
} from "@/lib/fetch/aws-connection-api";

export interface ConnectionState {
    accounts: CloudAccount[];
    resourcesByAccountId: Record<string, CloudResource[]>;

    isLoadingAccounts: boolean;
    isLoadingResources: Record<string, boolean>;

    error: string | null;
}

export interface ConnectionActions {
    fetchAccounts: () => Promise<void>;
    fetchResourcesForAccount: (accountId: string) => Promise<void>;
    clearError: () => void;
}

export type ConnectionStore = ConnectionState & { actions: ConnectionActions };

export const useConnectionStore = create<ConnectionStore>()(
    devtools(
        (set, get) => ({
            accounts: [],
            resourcesByAccountId: {},
            isLoadingAccounts: false,
            isLoadingResources: {},
            error: null,

            actions: {
                fetchAccounts: async () => {
                    set({ isLoadingAccounts: true, error: null });
                    try {
                        const accounts = await getAwsAccountConnections();
                        set({ accounts, isLoadingAccounts: false });
                    } catch (error: unknown) {
                        const errorMessage =
                            error instanceof Error ? error.message : "An unexpected error occurred";

                        set({
                            error: errorMessage,
                            isLoadingAccounts: false,
                        });
                    }
                },

                fetchResourcesForAccount: async (accountId: string) => {
                    const { resourcesByAccountId, isLoadingResources } = get();

                    if (resourcesByAccountId[accountId]) return;

                    set({
                        isLoadingResources: { ...isLoadingResources, [accountId]: true },
                        error: null,
                    });

                    try {
                        const resources = await getAwsAccountResources(accountId);
                        set((state) => ({
                            resourcesByAccountId: {
                                ...state.resourcesByAccountId,
                                [accountId]: resources,
                            },
                            isLoadingResources: {
                                ...state.isLoadingResources,
                                [accountId]: false,
                            },
                        }));
                    } catch (error: unknown) {
                        const errorMessage =
                            error instanceof Error ? error.message : "an unexpected error occurred";
                        set((state) => ({
                            error: errorMessage,
                            isLoadingResources: {
                                ...state.isLoadingResources,
                                [accountId]: false,
                            },
                        }));
                    }
                },

                clearError: () => set({ error: null }),
            },
        }),
        { name: "ConnectionStore" }
    )
);
