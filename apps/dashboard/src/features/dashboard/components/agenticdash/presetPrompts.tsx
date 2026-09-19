import { Button } from "@/components/atoms/button";

type Preset = {
    id: string;
    title: string;
    description: string;
    prompt: string;
};

const presets: Preset[] = [
    {
        id: "compute",
        title: "Compute",
        description: "CPU and Memory utilization for instances",
        prompt: "Create a performance dashboard showing CPU and Memory utilization for all active instances, highlighting nodes exceeding 80% usage.",
    },
    {
        id: "compute2",
        title: "Compute",
        description: "CPU and Memory utilization for instances",
        prompt: "Create a performance dashboard showing CPU and Memory utilization for all active instances, highlighting nodes exceeding 80% usage.",
    },
    {
        id: "compute3",
        title: "Compute",
        description: "CPU and Memory utilization for instances",
        prompt: "Create a performance dashboard showing CPU and Memory utilization for all active instances, highlighting nodes exceeding 80% usage.",
    },
    {
        id: "compute4",
        title: "Compute",
        description: "CPU and Memory utilization for instances",
        prompt: "Create a performance dashboard showing CPU and Memory utilization for all active instances, highlighting nodes exceeding 80% usage.",
    },
];

export default function presetPrompts() {
    return (
        <div className="h-full w-full flex flex-col gap-2">
            <span className="text-muted-foreground text-sm">Preset Prompts</span>

            <div className="h-fit w-full grid grid-cols-1 lg:grid-cols-2 gap-2">
                {presets.map((preset) => (
                    <Button
                        key={preset.id}
                        variant="outline"
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
