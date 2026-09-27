"use client";

import { useEffect, useState } from "react";
import apiClient from "@/lib/fetch/api-client";

export function useResourceNames(): Record<string, string> {
    //copied from budget file to correct sonarqube error
    const [resourceNames, setResourceNames] = useState<Record<string, string>>({});

    useEffect(() => {
        let cancelled = false;

        (async () => {
            try {
                const names = await apiClient<Record<string, string>>("/analytics/resource-names", {
                    method: "GET",
                });

                if (!cancelled) {
                    setResourceNames(names);
                }
            } catch {}
        })();

        return () => {
            cancelled = true;
        };
    }, []);

    return resourceNames;
}
