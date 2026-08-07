import {create} from "zustand";
import {getAwsAccountConnections, getAwsAccountResources, CloudAccount, CloudResource} from "@/lib/fetch/aws-connection-api";

interface BillingIntelligenceStore{
    provider : string | null;
    accountId : string | null;
    resourceId : string | null;
    accountDisplayName : string | null;
    resourceDisplayName : string | null;

    pastTimeWindowDays : number;
    forecastTimeWindowDays : number;
    accounts : CloudAccount[];
    resources : CloudResource[];
    isFetching : boolean;
    breakdownSearch : string;

    setProvider : (provider : string) => void;
    setAccount : (accountId : string, displayName : string) => void;
    setBreakdownSearch : (search : string) => void;
    setTimeWindows : (past : number, forecast : number) => void;
    setResource : (resourceId : string, displayName : string) => void;
    fetchResources : (accountId : string) => Promise<void>;
    fetchAccounts : (accountId : string) => Promise<void>;

    reset : () => void;
}

export const billingIntelligenceStore = create<BillingIntelligenceStore>((set, get) => ({
    provider : null,
    accountId : null,
    resourceId : null,

    accountDisplayName : null,
    resourceDisplayName : null,
    pastTimeWindowDays : 30,
    forecastTimeWindowDays : 7,

    accounts : [],
    resources : [],
    isFetching : false,
    breakdownSearch : "",

    setProvider : (provider) => {
        set({
            provider, accountId : null, resourceId : null, accounts : [], resources : [],
        });

        get().fetchAccounts(provider);
    },

    setAccount : (accountId, accountDisplayName) => {
        set({
            accountId, accountDisplayName, resourceId : null,
        });

        get().fetchResources(accountId);
    },

    setResource : (resourceId, resourceDisplayName) =>
        set({
            resourceId, resourceDisplayName,
        }),

    setTimeWindows : (past, forecast) =>
        set({
            pastTimeWindowDays : past, forecastTimeWindowDays : forecast,
        }),

    setBreakdownSearch : (breakdownSearch) => set({breakdownSearch}),

    fetchAccounts : async (provider) => {
        if(provider !== "AWS"){
            return;
        }

        set({isFetching : true});

        try{
            const accounts = await getAwsAccountConnections();

            set({accounts, isFetching : false});
        } catch{
            set({isFetching : false});
        }
    },

    fetchResources : async (accountId) => {
        set({isFetching : true});

        try{
            const resources = await getAwsAccountResources(accountId);

            set({resources, isFetching : false});
        } catch{
            set({isFetching : false});
        }
    },

    reset : () =>
        set({
            provider : null,
            accountId : null,
            resourceId : null,
        }),

}));