"use client";

import { useCallback, useEffect, useState } from "react";
import {
  disableAlert, enableAlert, fetchAlerts
} from "@/features/alerts/alerts";
import type { Alert, TypeForAlerts } from "@/features/alerts/types/alertTypes";

interface AlertsResult {
  alerts: Alert[];
  loading: boolean;
  forError: string | null;
  refreshing: () => Promise<void>;
  disable: (alertId: string) => Promise<void>;
  enable: (alertId: string) => Promise<void>;
}

export function useAlerts(typeForAlert?: TypeForAlerts): AlertsResult {
  const [alerts, setAlerts] = useState<Alert[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [forError, setForError] = useState<string | null>(null);

  const refreshing = useCallback(async () => {
    setLoading(true);
    setForError(null);

    try {
      const forData = await fetchAlerts(typeForAlert);
      setAlerts(forData);
    } catch (error) {
      setForError(error instanceof Error ? error.message : "Failed to load alerts");
    } finally {
      setLoading(false);
    }
  }, [typeForAlert]);

  useEffect(() => {
    let cancelled = false;

    (async () => {
      try {
        const forData = await fetchAlerts(typeForAlert);
        if (!cancelled) setAlerts(forData);
      } catch (error) {
        if (!cancelled) {
          setForError(error instanceof Error ? error.message : "Failed to load alerts");
        }
      } finally {
        if (!cancelled) setLoading(false);
      }
    })();

    return () => {
      cancelled = true;
    };
  }, [typeForAlert]);

  const disable = useCallback(async (alertId: string) => {
    await disableAlert(alertId);
    setAlerts((previous) =>
      previous.map((alert) =>
        alert.alertId === alertId ? { ...alert, status: "DISABLED" } : alert
      )
    );
  }, []);

  const enable = useCallback(async (alertId: string) => {
    await enableAlert(alertId);
    setAlerts((previous) =>
      previous.map((alert) =>
        alert.alertId === alertId ? { ...alert, status: "ACTIVE" } : alert
      )
    );
  }, []);

  return {
    alerts,
    loading,
    forError,
    refreshing,
    disable,
    enable
  };
}