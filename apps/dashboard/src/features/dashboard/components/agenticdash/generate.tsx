import {
    InputGroup,
    InputGroupAddon,
    InputGroupButton,
    InputGroupTextarea,
} from "@/components/atoms/input-group";
import { useState } from "react";
import { Button } from "@/components/atoms/button";
import { Loader2, X } from "lucide-react";
import PresetPrompts from "@/features/dashboard/components/agenticdash/presetPrompts";
import ApplyDashboardDialog from "@/features/dashboard/components/agenticdash/applyDashboardDialog";
import { useDashboardStore } from "@/features/dashboard/stores/dashboard-store";
import type { AiDashboardApplyMode } from "@/features/dashboard/types/agentic";

export default function GenerateDashInput() {
    const [prompt, setPrompt] = useState("");
    const [applyDialogOpen, setApplyDialogOpen] = useState(false);
    const [isApplying, setIsApplying] = useState(false);

    const isSessionActive = useDashboardStore((state) => state.isSessionActive);
    const isGenerating = useDashboardStore((state) => state.isGenerating);
    const currentVersionId = useDashboardStore((state) => state.currentVersionId);
    const versions = useDashboardStore((state) => state.versions);

    const { startSessionAndGenerate, sendPrompt, applyDashboard, cancelSession } =
        useDashboardStore((state) => state.agenticActions);

    const currentVersion = versions.find((version) => version.versionId === currentVersionId);

    const canApplyCurrentVersion =
        isSessionActive && currentVersion !== undefined && currentVersion.version > 0;

    const handleGenerate = async () => {
        if (!prompt.trim() || isGenerating) {
            return;
        }

        if (isSessionActive) {
            await sendPrompt(prompt);
        } else {
            await startSessionAndGenerate(prompt);
        }

        setPrompt("");
    };

    const handleApply = async (mode: AiDashboardApplyMode): Promise<void> => {
        if (!currentVersionId) {
            return;
        }

        setIsApplying(true);

        try {
            const applied = await applyDashboard(currentVersionId, mode);

            if (applied) {
                setApplyDialogOpen(false);
            }
        } finally {
            setIsApplying(false);
        }
    };

    return (
        <>
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
                                {currentVersion?.version === 0
                                    ? "Viewing the dashboard you started with"
                                    : `Viewing version v${currentVersion?.version ?? ""}`}
                            </span>

                            <div className="flex items-center gap-1">
                                <Button
                                    variant="ghost"
                                    size="sm"
                                    className="h-6 px-2 text-destructive hover:text-destructive hover:bg-destructive/10"
                                    onClick={() => cancelSession()}
                                    disabled={isGenerating || isApplying}
                                >
                                    <X className="w-3 h-3 mr-1" />
                                    End
                                </Button>

                                {canApplyCurrentVersion && (
                                    <Button
                                        variant="outline"
                                        size="sm"
                                        className="h-6 px-2 border-primary/30 text-primary hover:bg-primary/10"
                                        onClick={() => setApplyDialogOpen(true)}
                                        disabled={isGenerating || isApplying}
                                    >
                                        Apply
                                    </Button>
                                )}
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
                                onChange={(event) => setPrompt(event.target.value)}
                                onKeyDown={(event) => {
                                    if (event.key === "Enter" && !event.shiftKey) {
                                        event.preventDefault();
                                        handleGenerate();
                                    }
                                }}
                                disabled={isGenerating || isApplying}
                            />

                            <InputGroupAddon align="block-end">
                                <InputGroupButton
                                    variant="default"
                                    size="sm"
                                    className="ml-auto"
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

            {canApplyCurrentVersion && currentVersion && (
                <ApplyDashboardDialog
                    open={applyDialogOpen}
                    dashboardName={currentVersion.title}
                    isApplying={isApplying}
                    onOpenChange={setApplyDialogOpen}
                    onApply={handleApply}
                />
            )}
        </>
    );
}
