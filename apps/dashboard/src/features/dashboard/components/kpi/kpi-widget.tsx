import { Card } from "@/components/atoms/card";
import { KpiWidgetConfig } from "../../types/widgets";

interface WidgetProps {
    readonly config: KpiWidgetConfig;
}

export function KPIWidget({ config }: WidgetProps) {
    return (
        <Card className="flex flex-col gap-4 p-6 bg-muted/40">
            <h1 className="text-lg font-bold">Card title</h1>
            <h1 className="text-xl">$10.00</h1>
            <p>Accros 3 selected resources</p>
            <div className="flex flex-row justify-between">
                <p>Last 30 days</p>
                <p>Updated now</p>
            </div>
        </Card>
    );
}
