import {
    InputGroup,
    InputGroupAddon,
    InputGroupButton,
    InputGroupTextarea,
} from "@/components/atoms/input-group";
import { useState } from "react";
import { Button } from "@/components/atoms/button";
import { Check, X, Loader2 } from "lucide-react";
import PresetPrompts from "@/features/dashboard/components/agenticdash/presetPrompts";
import { useDashboardStore } from "@/features/dashboard/stores/dashboard-store";

export default function GenerateDashInput() {
    const [prompt, setPrompt] = useState("");
    const isSessionActive = useDashboardStore((state) => state.isSessionActive);
    const isGenerating = useDashboardStore((state) => state.isGenerating);
    const { startSessionAndGenerate, sendPrompt, acceptDashboard, cancelSession } =
        useDashboardStore((state) => state.agenticActions);

    const handleGenerate = async () => {
        if (!prompt.trim() || isGenerating) return;

        if (isSessionActive) {
            await sendPrompt(prompt);
        } else {
            await startSessionAndGenerate(prompt);
        }

        setPrompt(""); //clear
    };

    return (
        <div className="h-full flex flex-col justify-end items-start gap-4">
            {!isSessionActive && (
                <div className="h-full flex-1 overflow-y-auto w-full">
                    <PresetPrompts />
                </div>
            )}
            <div className="shrink-0 w-full flex flex-col">
                {isSessionActive && (
                    <div className="flex items-center justify-between px-3 py-2 bg-muted/30 border border-b-0 rounded-t-lg transition-all animate-in fade-in slide-in-from-bottom-2">
                        <span className="text-muted-foreground text-xs font-medium">
                            Review AI layout draft...
                        </span>
                        <div className="flex items-center gap-1">
                            <Button
                                variant="ghost"
                                size="sm"
                                className="h-6 px-2 text-destructive hover:text-destructive hover:bg-destructive/10"
                                onClick={() => cancelSession()}
                            >
                                <X className="w-3 h-3 mr-1" /> Discard
                            </Button>
                            <Button
                                variant="outline"
                                size="sm"
                                className="h-6 px-2 border-primary/30 text-primary hover:bg-primary/10"
                                onClick={() => acceptDashboard()}
                            >
                                <Check className="w-3 h-3 mr-1" /> Accept
                            </Button>
                        </div>
                    </div>
                )}
                <div className="shrink-0 bg-background w-full">
                    <InputGroup>
                        <InputGroupTextarea
                            placeholder={
                                isSessionActive
                                    ? "Ask for layout changes (e.g., 'Make the charts wider')..."
                                    : "Write your prompt here..."
                            }
                            className="z-50 min-h-0"
                            value={prompt}
                            onChange={(e) => setPrompt(e.target.value)}
                            onKeyDown={(e) => {
                                if (e.key === "Enter" && !e.shiftKey) {
                                    e.preventDefault();
                                    handleGenerate();
                                }
                            }}
                            disabled={isGenerating}
                        />
                        <InputGroupAddon align="block-end">
                            <InputGroupButton
                                variant="default"
                                size="sm"
                                className="ml-auto"
                                onClick={handleGenerate}
                                disabled={isGenerating || !prompt.trim()}
                            >
                                {isGenerating ? (
                                    <Loader2 className="w-4 h-4 animate-spin" />
                                ) : (
                                    "Generate"
                                )}{" "}
                            </InputGroupButton>
                        </InputGroupAddon>
                    </InputGroup>
                </div>
            </div>
        </div>
    );
}
