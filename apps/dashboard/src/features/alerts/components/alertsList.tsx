"use client";

import {useState} from "react";
import {Card, CardContent} from "@/components/atoms/card";
import {AlertCard} from "@/features/alerts/components/alertCard";
import { AlertDialog } from "@/features/alerts/components/alertDialog";
import type {Alert} from "@/features/alerts/types/alertTypes";

interface PropsForAlerts{
    alerts : Alert[];
    disable : (alertId : string) => Promise<void>;
    enable : (alertId : string) => Promise<void>;
}

export function AlertsList({
    alerts, disable, enable,
} : Readonly<PropsForAlerts>){

    const [selected, setSelected] = useState<Alert | null>(null);

    if(alerts.length === 0){
        return(
            <Card>
                <CardContent className = "py-8 text-center text-sm text-muted-foreground"> No alerts found </CardContent>
            </Card>
        );
    }

    return(
        <>
            <div className = "grid grid-cols-1 gap-3 md:grid-cols-2 lg:grid-cols-3">
                {alerts.map((alert) => (
                    <AlertCard key = {alert.alertId} alert = {alert} open = {setSelected}/>
                ))}
            </div>

            <AlertDialog alert = {selected} open = {selected !== null} onClose = {() => setSelected(null)} disable = {disable} enable = {enable}/>
        </>
    );
}