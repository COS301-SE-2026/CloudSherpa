import {useCallback, useEffect, useState} from "react";
import type {Alert, TypeForAlerts} from "@/features/alerts/types/alertTypes";
import {acknowledgeAlert, dismissAlert, fetchAlerts} from "@/features/alerts/alerts";

interface AlertsRequest{
    alerts : Alert[];
    loading : boolean;
    forError : string | null;
    refreshing : () => Promise<void>;
    acknowledge : (alertId : string) => Promise<void>;
    dismiss : (alertId : string) => Promise<void>;
}

export function useAlerts(typeForAlert?: TypeForAlerts) : AlertsRequest{
    const [alerts, setAlerts] = useState<Alert[]>([]);

    const [loading, setLoading] = useState(false);

    const [forError, setForError] = useState<string | null>(null);

    const refreshing = useCallback(async () => {
        setLoading(true);

        setForError(null);

        try{
            setAlerts(await fetchAlerts(typeForAlert));
        }catch(error){
            setForError(error instanceof Error ? error.message : "Failed to load alerts.");
        }finally{
            setLoading(false);
        }
    }, [typeForAlert]);

    useEffect(() => {
        void refreshing();
    }, [refreshing]);

    const acknowledge = useCallback(async (alertId : string) => {
        await acknowledgeAlert(alertId);

        setAlerts((previous) => previous.map((forAlerts) => forAlerts.alertId === alertId ? {...forAlerts, status : "ACKNOWLEDGED"} : forAlerts,),);
    }, []);

    const dismiss = useCallback(async (alertId : string) => {
        await dismissAlert(alertId);

        setAlerts((previous) => previous.map((forAlerts) => forAlerts.alertId === alertId ? {...forAlerts, status : "DISMISSED"} : forAlerts),);
    }, []);

    return {alerts, loading, forError, refreshing, acknowledge, dismiss};
}