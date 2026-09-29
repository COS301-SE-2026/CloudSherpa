import {
    InputGroup,
    InputGroupAddon,
    InputGroupButton,
    InputGroupTextarea,
} from "@/components/atoms/input-group";
import { useState, useRef, useEffect } from "react";
import { Loader2 } from "lucide-react";
import PresetPrompts from "@/features/dashboard/components/agenticdash/presetPrompts";
import { useDashboardStore } from "@/features/dashboard/stores/dashboard-store";

interface GenerateDashInputProps {
    showPresets: boolean;
    isApplying: boolean;
}

export default function GenerateDashInput({
    showPresets,
    isApplying,
}: Readonly<GenerateDashInputProps>) {
    const [prompt, setPrompt] = useState("");
    const textRef = useRef<HTMLTextAreaElement>(null);

    const isSessionActive = useDashboardStore((state) => state.isSessionActive);
    const isGenerating = useDashboardStore((state) => state.isGenerating);

    const { startSessionAndGenerate, sendPrompt } =
        useDashboardStore((state) => state.agenticActions);

    const handleGenerate = async () => {
        if (!prompt.trim() || isGenerating) {
            return;
        }

        if (isSessionActive || isGenerating) {
            await sendPrompt(prompt);
        } else {
            await startSessionAndGenerate(prompt);
        }

        setPrompt("");
    };

    useEffect(() => {
        textRef.current?.focus();
    }, []);

    const handleTextGroupClick = (e: React.MouseEvent<HTMLDivElement>) => {
        if ((e.target as HTMLElement).closest("button")) {
            return;
        }
        textRef.current?.focus();
    };

    const shouldShowPresets = !(isSessionActive || isGenerating) || showPresets;

    return (
        <div className="h-full flex flex-col justify-end items-start pt-4 gap-4">
            {shouldShowPresets && (
                <div className="h-full flex-1 overflow-y-auto w-full">
                    <PresetPrompts onSelect={setPrompt} />
                </div>
            )}

            <div className="shrink-0 w-full flex flex-col gap-2">
                <div className="shrink-0 bg-background w-full">
                    <InputGroup onClick={handleTextGroupClick}>
                        <InputGroupTextarea
                            ref={textRef}
                            placeholder={
                                isSessionActive
                                    ? "Ask for layout changes (e.g., 'Make the charts wider')..."
                                    : "Write your prompt here..."
                            }
                            className="z-50 min-h-0"
                            value={prompt}
                            onChange={(event) => setPrompt(event.target.value)}
                            onKeyDown={(event) => {
                                if (event.key === "Enter" && !event.shiftKey) {
                                    event.preventDefault();
                                    handleGenerate();
                                }
                            }}
                            disabled={isGenerating}
                        />

                        <InputGroupAddon align="block-end" className="flex justify-end">
                            <InputGroupButton
                                variant="default"
                                size="sm"
                                onClick={handleGenerate}
                                disabled={isGenerating || isApplying || !prompt.trim()}
                            >
                                {isGenerating ? (
                                    <Loader2 className="w-4 h-4 animate-spin" />
                                ) : (
                                    "Generate"
                                )}
                            </InputGroupButton>
                        </InputGroupAddon>
                    </InputGroup>
                </div>
            </div>
        </div>
    );
}
