"use client";

import { useCallback, useState } from "react";
import { KPIConfigTableRow } from "../columns";
import { KpiChargesRequestDto, KpiResourceResponseDto } from "../../dtos/kpi-dtos";
import apiClient from "@/lib/fetch/api-client";

export function useFetchTableResources() {
    const [tableResourcesLoading, setTableResourcesLoading] = useState(true);
    const [tableResourcesFetchError, setTableResourcesFetchError] = useState(false);
    const [tableResources, setTableResources] = useState<KPIConfigTableRow[]>();

    const fetchTableResources = useCallback(async (aggregationWindowDays: number) => {
        setTableResourcesLoading(true);

        try {
            const to = new Date();
            const from = new Date(to);
            from.setDate(from.getDate() - aggregationWindowDays);
            const payload: KpiChargesRequestDto = {
                from: from.toISOString(),
                to: to.toISOString(),
            };

            const resources: KpiResourceResponseDto = await apiClient<KpiResourceResponseDto>(
                "/billing/charges",
                {
                    method: "POST",
                    body: JSON.stringify(payload),
                }
            );
            setTableResources(
                resources.map((resource) => ({
                    chargeId: resource.chargeId,
                    resourceId: resource.resourceId,
                    service: resource.service,
                    provider: resource.provider,
                    resourceName: resource.resourceName,
                    chargeCost: resource.chargeCost,
                }))
            );
            setTableResourcesFetchError(false);
            setTableResourcesLoading(false);
        } catch (e) {
            if (e instanceof Error) {
                console.log(e.message);
            }
            setTableResourcesFetchError(true);
            setTableResourcesLoading(false);
        }
    }, []);

    return { fetchTableResources, tableResourcesLoading, tableResourcesFetchError, tableResources };
}
