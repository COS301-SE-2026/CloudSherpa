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
            "Create a concise compute health dashboard for active compute resources. " +
            "Use available CPU Utilization and Memory Utilization metrics. " +
            "Add no more than 4 charts and skip metrics that are unavailable.",
    },
    {
        id: "cost-optimization",
        title: "Cost Analysis",
        description: "Track daily spend and identify resource cost anomalies",
        prompt:
            "Create a concise cost dashboard using available billing charges. " +
            "Show total charges by connected cloud provider using no more than 3 KPI widgets.",
    },
    {
        id: "network-traffic",
        title: "Network Traffic",
        description: "Analyze inbound/outbound bandwidth and load balancer metrics",
        prompt:
            "Create a concise network dashboard for active resources. " +
            "Use available Network In and Network Out metrics. " +
            "Add no more than 4 charts and skip unavailable metrics.",
    },
    {
        id: "system-reliability",
        title: "System Reliability",
        description: "Overview of error rates, uptime, and failing requests",
        prompt:
            "Create a concise reliability dashboard for active resources. " +
            "Use available HTTP request and 5xx error metrics. " +
            "Add no more than 4 charts and skip unavailable metrics.",
    },
    {
        id: "total-spend",
        title: "Total Spend",
        description: "Track total cloud spending over the last 30 days",
        prompt:
            "Create one billing KPI showing total cloud spend for the last 30 days. " +
            "Use available billing charge IDs.",
    },
    {
        id: "provider-costs",
        title: "Provider Costs",
        description: "Compare spending across connected cloud providers",
        prompt:
            "Create a billing KPI for each connected cloud provider showing total spend over the last 30 days. " +
            "Use only available billing charge IDs and add no more than 3 KPIs.",
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
