import { useEffect, useRef, useState } from "react";
import type { Alert } from "@/features/alerts/types/alertTypes";

const API_BASE = process.env["NEXT_PUBLIC_API_URL"];
const sseUrl = `${API_BASE}/stream`;

async function refreshAuthSession(): Promise<boolean> {
    if (!API_BASE) return false;

    try {
        const response = await fetch(`${API_BASE}/auth/refresh`, {
            method: "POST",
            credentials: "include",
        });
        return response.ok;
    } catch {
        return false;
    }
}

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

            eventSource.onerror = () => {
                if (isCleaningUp) return;

                eventSource.removeEventListener("alert", handleAlert);
                eventSource.close();

                if (!hasRetriedRef.current) {
                    hasRetriedRef.current = true;
                    refreshAuthSession().then((refreshed) => {
                        if (isCleaningUp) return;

                        if (refreshed) {
                            connect();
                        } else {
                            setError(new Error("Failed to open alert stream connection"));
                        }
                    });
                    return;
                }

                setError(new Error("Failed to open alert stream connection"));
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
