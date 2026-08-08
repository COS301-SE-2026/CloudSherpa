import ChartConfigForm from "@/features/dashboard/components/widgetGrid/widgets/chart-config-form";
import { use } from "react";

interface EditMetricsPageProps {
    params: Promise<{
        id: string;
    }>;
}

export default function EditMetricsPage({ params }: Readonly<EditMetricsPageProps>) {
    const resolvedParams = use(params);
    return <ChartConfigForm ChartId={resolvedParams.id} />;
}
