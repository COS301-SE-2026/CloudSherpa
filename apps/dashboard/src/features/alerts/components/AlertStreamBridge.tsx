"use client";

import { useCallback } from "react";
import type { Alert } from "@/features/alerts/types/alertTypes";
import { useAlertStream } from "@/features/alerts/services/sse/alert-stream";
import { useAlertStore } from "@/features/alerts/stores/alert-store";

export function AlertStreamBridge() {
    const upsertAlert = useAlertStore((s) => s.upsertAlert);

    const onAlert = useCallback(
        (incoming: Alert) => {
            upsertAlert(incoming);
        },
        [upsertAlert]
    );

    useAlertStream(onAlert);

    return null;
}
