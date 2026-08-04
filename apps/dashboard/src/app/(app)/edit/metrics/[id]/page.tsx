import ChartConfigForm from "@/features/dashboard/components/widgetGrid/widgets/chart-config-form";

interface EditMetricsPageProps {
    readonly params: Promise<{ id: string }>;
}

export default function EditMetricsPage() {
    return <ChartConfigForm />;
}
