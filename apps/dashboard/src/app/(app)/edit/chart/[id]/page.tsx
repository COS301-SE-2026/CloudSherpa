"use server";

import { ChartConfigForm } from "@/features/dashboard/components/widgetGrid/widgets/chartConfigForm";

interface ChartConfigPageProps {
    readonly params: Promise<{ id: string }>;
}

export default async function EditChartPage({ params }: ChartConfigPageProps) {
    const { id } = await params;

    return <ChartConfigForm chartId={id} />;
}
