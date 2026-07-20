import { CurrencyEnum } from "@/features/dashboard/types/currency";
import { CloudProviderEnum } from "@/features/dashboard/types/provider";

export type KpiPreviewRequestDto = {
    title: string;
    resourceIds: string[];
    from: string;
    to: string;
    aggregation: string;
};

export type KpiPreviewResponseDto = {
    title: string;
    value: number;
    currency: CurrencyEnum;
    selectedResourcecount: number;
    timeLabel: string;
    updatedAt: string;
};

export type KpiResource = {
    resourceId: string;
    service: string;
    provider: CloudProviderEnum;
};

export type KpiResourceResponseDto = KpiResource[];
