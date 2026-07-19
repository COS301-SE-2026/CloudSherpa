"use client";

import { Button } from "@/components/atoms/button";
import { Card, CardTitle } from "@/components/atoms/card";
import { FieldSeparator } from "@/components/atoms/field";
import { useState } from "react";
import { KpiFormDetails } from "./kpi-form-details";
import { KpiFormResources } from "./kpi-form-resources";
import { KpiFormTimePeriod } from "./kpi-form-time-period";
import { KPIWidget } from "../../kpi-widget";
import { KpiWidgetConfig } from "@/features/dashboard/types/widgets";
import { KpiConfigSummary } from "../kpi-config-summary";

export function KpiConfigForm() {
    const [startDate, setStartDate] = useState<Date>();
    const [endDate, setEndDate] = useState<Date>();

    const config: KpiWidgetConfig = {
        id: "123",
        aggregationWindowDays: 30,
        resourceIds: ["1", "2", "3"],
        title: "Some Title",
        widgetType: "kpi",
    };

    return (
        <main className="flex flex-1 flex-col gap-6 p-6 lg:p-8 w-full mx-auto">
            <div className="flex flex-row gap-6">
                <h1 className="text-2xl">KPI Configuration</h1>
                <Button variant={"default"}>Save KPI</Button>
                <Button variant={"secondary"}>Cancel</Button>
            </div>
            <div className="grid grid-cols-[2fr_1fr] gap-4 h-full">
                <Card className="p-6">
                    <KpiFormDetails />
                    <FieldSeparator></FieldSeparator>
                    <KpiFormResources />
                    <FieldSeparator />
                    <KpiFormTimePeriod
                        startDate={startDate}
                        setStartDate={setStartDate}
                        endDate={endDate}
                        setEndDate={setEndDate}
                    />
                </Card>

                <Card className="p-6">
                    <CardTitle>Preview</CardTitle>
                    <KPIWidget config={config} />

                    <KpiConfigSummary />
                </Card>
            </div>
        </main>
    );
}
