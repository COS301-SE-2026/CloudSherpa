import { useEffect, useRef, useState } from "react";
import type { Alert } from "@/features/alerts/types/alertTypes";
import { ensureSessionRefreshed } from "@/lib/fetch/api-client";

const API_BASE = process.env["NEXT_PUBLIC_API_URL"];
const sseUrl = `${API_BASE}/stream`;

interface UseAlertStreamResult {
    error: Error | null;
}

export function useAlertStream(onAlert: (alert: Alert) => void): UseAlertStreamResult {
    const [error, setError] = useState<Error | null>(null);
    const hasRetriedRef = useRef(false);

    useEffect(() => {
        let isCleaningUp = false;
        let eventSource: EventSource;

        const handleAlert = (event: MessageEvent<string>) => {
            try {
                const alert = JSON.parse(event.data) as Alert;

                onAlert(alert);
            } catch {}
        };

        const connect = () => {
            eventSource = new EventSource(sseUrl, { withCredentials: true });

            eventSource.onopen = () => {
                hasRetriedRef.current = false;
                setError(null);
            };

            eventSource.addEventListener("alert", handleAlert);

            const handleStreamError = async () => {
                if (isCleaningUp) {
                    return;
                }

                eventSource.removeEventListener("alert", handleAlert);
                eventSource.close();

                if (hasRetriedRef.current) {
                    setError(new Error("Failed to open alert stream connection"));
                    return;
                }

                hasRetriedRef.current = true;

                const refreshed = await ensureSessionRefreshed();

                if (isCleaningUp) {
                    return;
                }

                if (refreshed) {
                    connect();
                    return;
                }

                setError(new Error("Failed to open alert stream connection"));
            };

            eventSource.onerror = () => {
                void handleStreamError();
            };
        };

        connect();

        return () => {
            isCleaningUp = true;
            eventSource.removeEventListener("alert", handleAlert);
            eventSource.close();
        };
    }, [onAlert]);

    return { error };
}
