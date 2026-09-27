import {
    InputGroup,
    InputGroupAddon,
    InputGroupButton,
    InputGroupTextarea,
} from "@/components/atoms/input-group";
import { useState, useRef, useEffect } from "react";
import { Button } from "@/components/atoms/button";
import { Loader2, X } from "lucide-react";
import PresetPrompts from "@/features/dashboard/components/agenticdash/presetPrompts";
import ApplyDashboardDialog from "@/features/dashboard/components/agenticdash/applyDashboardDialog";
import { useDashboardStore } from "@/features/dashboard/stores/dashboard-store";
import type { AiDashboardApplyMode } from "@/features/dashboard/types/agentic";
import { useToolbar } from "@/features/dashboard/components/toolbar/toolbarProvider";

interface GenerateDashInputProps {
    showPresets: boolean;
}

export default function GenerateDashInput({ showPresets }: Readonly<GenerateDashInputProps>) {
    const [prompt, setPrompt] = useState("");
    const { isApplyDialogOpen, setIsApplyDialogOpen } = useToolbar();
    const [isApplying, setIsApplying] = useState(false);
    const textRef = useRef<HTMLTextAreaElement>(null);

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

        const succeeded = isSessionActive
            ? await sendPrompt(prompt)
            : await startSessionAndGenerate(prompt);

        if (succeeded) {
            setPrompt("");
        }
    };

    useEffect(() => {
        textRef.current?.focus();
    }, []);

    const handleApply = async (mode: AiDashboardApplyMode): Promise<void> => {
        if (!currentVersionId) {
            return;
        }

        setIsApplying(true);

        try {
            console.log("is applying ai dashboard");
            const applied = await applyDashboard(currentVersionId, mode);

            if (applied) {
                console.log("applied dashboard");
                setIsApplyDialogOpen(false);
            }
        } finally {
            setIsApplying(false);
        }
    };

    const handleTextGroupClick = (e: React.MouseEvent<HTMLDivElement>) => {
        if ((e.target as HTMLElement).closest("button")) {
            return;
        }
        textRef.current?.focus();
    };

    const shouldShowPresets = !(isSessionActive || isGenerating) || showPresets;

    return (
        <>
            <div className="h-full flex flex-col justify-end items-start gap-4">
                {shouldShowPresets && (
                    <div className="h-full flex-1 overflow-y-auto w-full">
                        <PresetPrompts onSelect={setPrompt} />
                    </div>
                )}

                <div className="shrink-0 w-full flex flex-col gap-2">
                    {isSessionActive && (
                        <div className="flex items-center justify-between px-3 py-2 bg-muted/30 border rounded-md transition-all animate-in fade-in slide-in-from-bottom">
                            <span className="text-muted-foreground text-xs font-medium">
                                {currentVersion?.version === 0
                                    ? "Viewing the dashboard you started with"
                                    : `Viewing version v${currentVersion?.version ?? ""}`}
                            </span>

                            <div className="flex items-center gap-1">
                                <Button
                                    variant="destructive"
                                    className="h-7"
                                    size="sm"
                                    onClick={() => cancelSession()}
                                    disabled={isGenerating || isApplying}
                                >
                                    <X />
                                    End
                                </Button>

                                {canApplyCurrentVersion && (
                                    <Button
                                        size="sm"
                                        className="h-7"
                                        onClick={() => setIsApplyDialogOpen(true)}
                                        disabled={isGenerating || isApplying}
                                    >
                                        Apply
                                    </Button>
                                )}
                            </div>
                        </div>
                    )}

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
                                disabled={isGenerating || isApplying}
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

            {canApplyCurrentVersion && currentVersion && (
                <ApplyDashboardDialog
                    open={isApplyDialogOpen}
                    dashboardName={currentVersion.title}
                    isApplying={isApplying}
                    onOpenChange={setIsApplyDialogOpen}
                    onApply={handleApply}
                />
            )}
        </>
    );
}
