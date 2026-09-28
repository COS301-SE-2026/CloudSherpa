"use client";

import { useEffect, useState } from "react";
import apiClient from "@/lib/fetch/api-client";

interface Resources{
    resourceId : string;
    resourceName : string;
}

export function useResourceNames(): Record<string, string> {
    //copied from budget file to correct sonarqube error
    const [resourceNames, setResourceNames] = useState<Record<string, string>>({});

    useEffect(() => {
        let cancelled = false;

        (async () => {
            try {
                const names = await apiClient<Resources[]>("/analytics/resource-names", {
                    method: "GET",
                });

                if (!cancelled) {
                    const forMapping = Object.fromEntries(names.map((forResources) => [forResources.resourceId, forResources.resourceName])) as Record<string, string>;

                    setResourceNames(forMapping);
                }
            } catch {}
        })();

        return () => {
            cancelled = true;
        };
    }, []);

    return resourceNames;
}
