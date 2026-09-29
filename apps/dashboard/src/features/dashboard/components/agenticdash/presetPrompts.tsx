import { Button } from "@/components/atoms/button";

type Preset = {
    id: string;
    title: string;
    description: string;
    prompt: string;
};

const presets: Preset[] = [
    {
        id: "compute-health",
        title: "Compute Health",
        description: "Monitor CPU, memory, and disk utilization across instances",
        prompt:
            "Create a compute health dashboard for AWS only. " +
            "Find the first connected AWS account. " +
            "Find all active EC2 resources in that account. " +
            "For each EC2 resource, find the available CPU Utilization metric. " +
            "Add one line chart per EC2 resource using CPU Utilization, with no more than 4 charts total. " +
            "Skip resources where CPU Utilization is unavailable. " +
            "Do not use resources from other accounts or providers. " +
            "Once the requested charts are added, finalize the dashboard.",
    },
    {
        id: "network-traffic",
        title: "Network Traffic",
        description: "Analyze inbound/outbound bandwidth and load balancer metrics",
        prompt:
            "Create a network traffic dashboard for AWS only. " +
            "Find the first connected AWS account. " +
            "Find up to the first two active EC2 resources from that account. " +
            "For each of those resources, find the available network metrics. " +
            "Add a line chart for each available network metric, with no more than 4 charts total. " +
            "Skip a resource if neither network metric is available. " +
            "Do not use resources from other accounts or providers. " +
            "Once the requested charts are added, finalize the dashboard.",
    },
    {
        id: "system-reliability",
        title: "System Reliability",
        description: "Overview of error rates, uptime, and failing requests",
        prompt:
            "Create a system reliability dashboard for AWS only. " +
            "Find the first connected AWS account. " +
            "Find up to the first two active resources in that account that have an available HTTP Error 5xx metric. " +
            "For each of those resources, add one line chart using HTTP Error 5xx, with no more than 2 charts total. " +
            "Skip resources where HTTP Error 5xx is unavailable. " +
            "Do not substitute unrelated metrics and do not use resources from other accounts or providers. " +
            "Once the requested charts are added, finalize the dashboard.",
    },
    {
        //very consistent
        id: "total-spend",
        title: "Total Spend",
        description: "Track total cloud spending over the last 30 days",
        prompt:
            "Create a simple billing dashboard. " +
            "Find available billing charges and create exactly one KPI using the returned charge IDs. " +
            "Use a 30 day aggregation window and name the KPI Total Spend. " +
            "Commit the dashboard when complete.",
    },
];

interface PresetPromptsProps {
    onSelect: (prompt: string) => void;
}

export default function presetPrompts({ onSelect }: Readonly<PresetPromptsProps>) {
    return (
        <div className="h-full w-full flex flex-col gap-2">
            <span className="text-muted-foreground text-sm">Preset Prompts</span>

            <div className="h-fit w-full grid grid-cols-1 lg:grid-cols-1 gap-2 z-0">
                {presets.map((preset) => (
                    <Button
                        key={preset.id}
                        variant="outline"
                        onClick={() => onSelect(preset.prompt)}
                        className="h-auto w-full flex flex-col justify-start items-start overflow-hidden  text-left whitespace-normal p-3 gap-1"
                    >
                        <span className="flex items-center">{preset.title}</span>
                        <span className="text-muted-foreground line-clamp-2">
                            {preset.description}
                        </span>
                    </Button>
                ))}
            </div>
        </div>
    );
}
