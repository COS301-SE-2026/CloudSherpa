"use server";

import { KpiConfigForm } from "@/features/dashboard/components/kpi/config/form/kpi-config-form";

interface KpiConfigPageProps {
    readonly params: Promise<{ id: string }>;
}

export default async function EditKpiPage({ params }: KpiConfigPageProps) {
    const { id } = await params;

    return <KpiConfigForm kpiId={id} />;
}
