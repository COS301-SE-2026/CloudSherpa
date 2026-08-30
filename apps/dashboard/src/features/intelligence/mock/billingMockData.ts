import { CloudAccount, AccountType } from "@/lib/fetch/dto/cloud-account";
import { CloudResource, ResourceStatus } from "@/lib/fetch/dto/cloud-resource";
import type { BillingSummaryDto, CostBreakdownItem } from "../types/dtos";

export const MOCK_ACCOUNTS: CloudAccount[] = [
    {
        id: "account-1",
        connectionId: "connection-1",
        accountType: AccountType.AWS_ACCOUNT,
        displayName: "Account one",
        ingestionPeriod: "30",
        createdAt: "2026-01-15T00:00:00.000Z",
    },
    {
        id: "account-2",
        connectionId: "connection-2",
        accountType: AccountType.AWS_ACCOUNT,
        displayName: "Account two",
        ingestionPeriod: "30",
        createdAt: "2026-02-20T00:00:00.000Z",
    },
    {
        id: "account-3",
        connectionId: "connection-3",
        accountType: AccountType.AWS_ACCOUNT,
        displayName: "Account three",
        ingestionPeriod: "7",
        createdAt: "2026-03-10T00:00:00.000Z",
    },
];

export const MOCK_RESOURCES: Record<string, CloudResource[]> = {
    "account-1": [
        {
            id: "resource-1",
            accountId: "account-one",
            resourceType: "typeOne",
            resourceName: "Name one",
            status: ResourceStatus.ACTIVE,
            tags: { env: "production" },
            lastUpdated: "2026-08-01T00:00:00.000Z",
            createdAt: "2026-01-16T00:00:00.000Z",
        },
    ],
};

export const MOCK_BILLING_SUMMARY: BillingSummaryDto = {
    cumulativeBilling: 1567.04,
    projectedHorizonCost: 2348.86,
    forecastVariance: 67.98,

    dailyBurnRate: 67.91,
    primaryCostDriverId: "id-primary-cost-id",
    primaryCostDriverLabel: "primary-cost-label",

    highestCostAccelerationId: "id-cost-acc",
    highestCostAccelerationLabel: "cost-acc-label",
    currency: "ZAR",
};

export const MOCK_COST_BREAKDOWN: CostBreakdownItem[] = [
    {
        id: "1",
        chargeId: "chargeId1",
        label: "label1",
        percentage: 28,
        cost: 438.77,
        serviceType: "service one",
        resourceId: "resource one",
    },
    {
        id: "2",
        chargeId: "chargeId2",
        label: "label2",
        percentage: 21,
        cost: 155.46,
        serviceType: "service two",
        resourceId: "resource two",
    },
    {
        id: "3",
        chargeId: "chargeId3",
        label: "label3",
        percentage: 17,
        cost: 236.98,
        serviceType: "service three",
        resourceId: "resource three",
    },
    {
        id: "4",
        chargeId: "chargeId4",
        label: "label4",
        percentage: 14,
        cost: 324.87,
        serviceType: "service four",
        resourceId: "resource four",
    },
    {
        id: "5",
        chargeId: "chargeId5",
        label: "label5",
        percentage: 11,
        cost: 215.78,
        serviceType: "service five",
        resourceId: "resource five",
    },
    {
        id: "6",
        chargeId: "chargeId6",
        label: "label6",
        percentage: 9,
        cost: 698.23,
        serviceType: "service six",
        resourceId: "resource six",
    },
];

export const mockApiDelay = (delay: number = 500): Promise<void> => {
    return new Promise((resolve) => setTimeout(resolve, delay));
};

export const mockApiResponse = <T>(data: T, delay: number = 500): Promise<T> => {
    return new Promise((resolve) => {
        setTimeout(() => resolve(data), delay);
    });
};

export const getMockBillingData = (
    accountId: string,
    resourceId: string | null,
    pastTimeWindowDays: number,
    forecastTimeWindowDays: number
): {
    forSummary: BillingSummaryDto;
    forBreakdown: CostBreakdownItem[];
} => {
    const forMultiplier = pastTimeWindowDays / 30;

    let forBreakdown = MOCK_COST_BREAKDOWN;

    if (resourceId) {
        const forFiltered = MOCK_COST_BREAKDOWN.filter((item) => item.resourceId === resourceId);

        if (forFiltered.length > 0) {
            forBreakdown = forFiltered;
        }
    }

    return {
        forSummary: {
            ...MOCK_BILLING_SUMMARY,
            cumulativeBilling: MOCK_BILLING_SUMMARY.cumulativeBilling * forMultiplier,
            dailyBurnRate: MOCK_BILLING_SUMMARY.dailyBurnRate * forMultiplier,
            projectedHorizonCost:
                MOCK_BILLING_SUMMARY.projectedHorizonCost * (forecastTimeWindowDays / 7),
        },

        forBreakdown: forBreakdown.map((item) => ({
            ...item,
            percentage: Math.min(Math.round(item.percentage * forMultiplier), 100),
            cost: item.cost * forMultiplier,
        })),
    };
};
