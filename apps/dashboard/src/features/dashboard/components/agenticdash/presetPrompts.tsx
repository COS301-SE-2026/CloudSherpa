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
        prompt: "Create a dashboard showing CPU Utilization and Memory Utilization charts for active instances.",
    },
    {
        id: "cost-optimization",
        title: "Cost Analysis",
        description: "Track daily spend and identify resource cost anomalies",
        prompt: "Create a cost dashboard showing total daily charges for each provider.",
    },
    {
        id: "network-traffic",
        title: "Network Traffic",
        description: "Analyze inbound/outbound bandwidth and load balancer metrics",
        prompt: "Create a dashboard showing NetworkIn and NetworkOut metrics for instances.",
    },
    {
        id: "system-reliability",
        title: "System Reliability",
        description: "Overview of error rates, uptime, and failing requests",
        prompt: "Create a dashboard tracking 5xx error counts and total HTTP request volume.",
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
