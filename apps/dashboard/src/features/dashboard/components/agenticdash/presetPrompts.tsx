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
            "Create a compute health dashboard for my AWS, GCP and Azure accounts. " +
            "For each resource, add the CPU utilization metric charts. Similarly " +
            "add charts for the network in and network out metrics as well as any " +
            "health check metrics.",
    },
    {
        id: "network-traffic",
        title: "Network Traffic",
        description: "Analyze inbound/outbound bandwidth and load balancer metrics",
        prompt:
            "Create a network traffic dashboard for  AWS, GCP and Azure. " +
            "Find resources for those accounts and find the available network metrics. " +
            "Add a line chart for each available network metric. Skip a resource if no network metrics are available.",
    },
    {
        id: "total-spend",
        title: "Total Spend",
        description: "Track total cloud spending over the last 30 days",
        prompt:
            "Create a billing dashboard detailing different types of spend within my AWS, GCP and Azure accounts. " +
            "Create billing KPIs for total monthly spend for each account type as well as for " +
            "monthly storage costs for each account.",
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
