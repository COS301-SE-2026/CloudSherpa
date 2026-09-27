import { CurrencyEnum } from "@/features/dashboard/types/currency";
import { CloudProviderEnum } from "@/features/dashboard/types/provider";

export type KpiPreviewRequestDto = {
    title: string;
    chargeIds: string[];
    from: string;
    to: string;
    aggregation: string;
};

export type KpiPreviewResponseDto = {
    title: string;
    value: number;
    currency: CurrencyEnum;
    selectedChargeCount: number;
    timeLabel: string;
    updatedAt: string;
    previousValue: number | null;
    resourceName: string | null;
};

export type KpiCharge = {
    chargeId: string;
    resourceId: string;
    service: string;
    provider: CloudProviderEnum;
    resourceName: string | null;
    chargeCost: number;
    metadata: Record<string, unknown> | null;
};

export type KpiResourceResponseDto = KpiCharge[];
