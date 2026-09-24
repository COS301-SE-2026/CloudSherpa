"use client";

import { useState } from "react";
import { AlertDialog } from "@/features/alerts/components/alertDialog";
import type { Alert } from "@/features/alerts/types/alertTypes";
import { AlertTable } from "@/features/alerts/components/alertTable";

interface PropsForAlerts {
    alerts: Alert[];
    disable: (alertId: string) => Promise<void>;
    enable: (alertId: string) => Promise<void>;
}

export function AlertsList({ alerts, disable, enable }: Readonly<PropsForAlerts>) {
    const [selected, setSelected] = useState<Alert | null>(null);

    return (
        <>
            <AlertTable
                alerts={alerts}
                onToggle={(alert, checked) =>
                    checked ? enable(alert.alertId) : disable(alert.alertId)
                }
                info={setSelected}
            />

            <AlertDialog
                alert={selected}
                open={selected !== null}
                onClose={() => setSelected(null)}
                disable={disable}
                enable={enable}
            />
        </>
    );
}
