"use client";

import { useState } from "react";
import { KPIConfigTableRow } from "../columns";
import { KpiResourceResponseDto } from "../../dtos/kpi-dtos";
import apiClient from "@/lib/fetch/api-client";

export function useFetchTableResources() {
    const [tableResourcesLoading, setTableResourcesLoading] = useState(true);
    const [tableResourcesFetchError, setTableResourcesFetchError] = useState(false);
    const [tableResources, setTableResources] = useState<KPIConfigTableRow[]>();

    async function fetchTableResources() {
        try {
            const resources: KpiResourceResponseDto =
                await apiClient<KpiResourceResponseDto>("/billing/resources");
            console.log(resources);
            setTableResources(
                resources.map((resource) => ({
                    resourceId: resource.resourceId,
                    service: resource.service,
                    provider: resource.provider,
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
    }

    return { fetchTableResources, tableResourcesLoading, tableResourcesFetchError, tableResources };
}
