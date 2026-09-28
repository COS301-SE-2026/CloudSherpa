import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/atoms/card";
import { Sparkles } from "lucide-react";
import { Button } from "@/components/atoms/button";
import { useState, useEffect, useRef } from "react";
import { Tabs, TabsList, TabsTrigger, TabsContent } from "@/components/atoms/tabs";
import GenerateDashInput from "@/features/dashboard/components/agenticdash/generate";
import History from "@/features/dashboard/components/agenticdash/history";
import {
    Tooltip,
    TooltipContent,
    TooltipProvider,
    TooltipTrigger,
} from "@/components/atoms/tooltip";
import { Kbd, KbdGroup } from "@/components/atoms/kbd";
import { useToolbar } from "../toolbar/toolbarProvider";
import { useDashboardStore } from "@/features/dashboard/stores/dashboard-store";
import { cn } from "@/lib/utils";
import StructuredRequest from "@/features/dashboard/components/agenticdash/structured";

export default function AgenticDashCard() {
    const [open, setOpen] = useState(false);
    const popupRef = useRef<HTMLDivElement>(null);
    const [showPresets, setShowPresets] = useState(false);
    const buttonRef = useRef<HTMLButtonElement>(null);
    const { isEditMode } = useToolbar();
    const isSessionActive = useDashboardStore((state) => state.isSessionActive);
    const [activeTab, setActiveTab] = useState("generate");
    const isGenerating = useDashboardStore((state) => state.isGenerating);

    const handleClick = () => {
        setOpen(!open);
    };

    useEffect(() => {
        const handleKeyDown = (e: KeyboardEvent) => {
            const target = e.target as HTMLElement;
            if (
                target.tagName === "INPUT" ||
                target.tagName === "TEXTAREA" ||
                target.isContentEditable
            ) {
                return;
            }

            if (e.shiftKey && e.key.toLowerCase() === "d" && !isEditMode) {
                e.preventDefault();
                setOpen((prev) => !prev);
            }
        };

        window.addEventListener("keydown", handleKeyDown);

        return () => {
            window.removeEventListener("keydown", handleKeyDown);
        };
    }, [isEditMode]);

    return (
        <TooltipProvider>
            <Tooltip>
                <TooltipTrigger asChild>
                    <Button
                        ref={buttonRef}
                        variant="default"
                        onClick={handleClick}
                        disabled={isEditMode}
                        className={cn(isGenerating && "animate-pulse")}
                    >
                        <Sparkles className="text-primary-foreground" />
                    </Button>
                </TooltipTrigger>
                <TooltipContent className="flex flex-row justify-center items-center">
                    <span className="text-sm">Generate a dashboard</span>
                    <KbdGroup>
                        <Kbd>Shift</Kbd>
                        <span>+</span>
                        <Kbd>D</Kbd>
                    </KbdGroup>
                </TooltipContent>
            </Tooltip>
            {open && (
                <div ref={popupRef} className="fixed bottom-6 right-6 z-50 flex flex-col items-end">
                    <Card className="h-[640px] w-[480px] flex flex-col shadow-xl">
                        <CardHeader>
                            <CardTitle>Dashboard Constructor</CardTitle>
                            <CardDescription>
                                Describe what you want to monitor and which resource to pull the
                                data from
                            </CardDescription>
                        </CardHeader>
                        <CardContent className="h-full">
                            <Tabs
                                defaultValue="generate"
                                value={activeTab}
                                onValueChange={setActiveTab}
                                className="flex flex-col h-full w-full"
                            >
                                <div className="flex flex-row justify-between">
                                    <TabsList>
                                        <TabsTrigger value="generate">Generate</TabsTrigger>
                                        <TabsTrigger value="structured">Structured</TabsTrigger>
                                        <TabsTrigger value="history">History</TabsTrigger>
                                    </TabsList>
                                    {isSessionActive && activeTab === "generate" && (
                                        <Button
                                            variant="secondary"
                                            className={cn(
                                                showPresets && "bg-card/50 text-red-500",
                                                "text-muted-foreground"
                                            )}
                                            onClick={() => setShowPresets((prev) => !prev)}
                                        >
                                            Presets Prompts
                                        </Button>
                                    )}
                                </div>
                                <div className="flex-1 min-h-0 w-full relative">
                                    <TabsContent
                                        value="generate"
                                        className="absolute inset-0 m-0 data-[state=active]:flex flex-col"
                                    >
                                        <GenerateDashInput showPresets={showPresets} />
                                    </TabsContent>
                                    <TabsContent
                                        value="structured"
                                        className="absolute inset-0 m-0 data-[state=active]:flex flex-col"
                                    >
                                        <StructuredRequest />
                                    </TabsContent>
                                    <TabsContent
                                        value="history"
                                        className="absolute inset-0 m-0 data-[state=active]:flex flex-col"
                                    >
                                        <History />
                                    </TabsContent>
                                </div>
                            </Tabs>
                        </CardContent>
                    </Card>
                </div>
            )}
        </TooltipProvider>
    );
}
