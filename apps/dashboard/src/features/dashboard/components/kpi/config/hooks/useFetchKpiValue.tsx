"use client";

import { KpiWidgetConfig } from "@/features/dashboard/types/widgets";
import apiClient from "@/lib/fetch/api-client";
import { useEffect, useState } from "react";
import { KpiPreviewRequestDto, KpiPreviewResponseDto } from "../../dtos/kpi-dtos";

export function useFetchKpiValue(config: KpiWidgetConfig) {
    const [loadingKpiValue, setLoadingKpiValue] = useState(true);
    const [fetchKpiValueError, setFetchKpiValueError] = useState(false);
    const [kpiPreview, setKpiPreview] = useState<KpiPreviewResponseDto>();

    useEffect(() => {
        async function fetchKpiValue() {
            setLoadingKpiValue(true);

            try {
                const payload: KpiPreviewRequestDto = {
                    title: config.title,
                    chargeIds: config.chargeIds,
                    from: new Date(
                        new Date().setDate(new Date().getDate() - config.aggregationWindowDays)
                    ).toISOString(),
                    to: new Date().toISOString(),
                    aggregation: "sum",
                };

                const preview: KpiPreviewResponseDto = await apiClient("/billing/kpis/preview", {
                    method: "POST",
                    body: JSON.stringify(payload),
                });

                setKpiPreview(preview);
                setLoadingKpiValue(false);
                setFetchKpiValueError(false);
            } catch (e) {
                if (e instanceof Error) {
                    console.log(e.message);
                }

                setLoadingKpiValue(false);
                setFetchKpiValueError(true);
            }
        }

        fetchKpiValue();
    }, [config.chargeIds, config.aggregationWindowDays]);

    return { loadingKpiValue, fetchKpiValueError, kpiPreview };
}
