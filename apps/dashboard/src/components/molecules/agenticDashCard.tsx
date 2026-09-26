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
import { useToolbar } from "@/features/dashboard/components/toolbar/toolbarProvider";
import { useDashboardStore } from "@/features/dashboard/stores/dashboard-store";

export default function AgenticDashCard() {
    const [open, setOpen] = useState(false);
    const popupRef = useRef<HTMLDivElement>(null);
    const buttonRef = useRef<HTMLButtonElement>(null);
    const { isEditMode } = useToolbar();
    const isSessionActive = useDashboardStore((state) => state.isSessionActive);

    const handleClick = () => {
        setOpen(!open);
    };

    useEffect(() => {
        if (!isSessionActive && open) {
            setOpen(false);
        }
    }, [isSessionActive, open]);

    useEffect(() => {
        const handleClickOutside = (event: MouseEvent | TouchEvent) => {
            //check click outside popup and btn
            if (
                open &&
                popupRef.current &&
                !popupRef.current.contains(event.target as Node) &&
                buttonRef.current &&
                !buttonRef.current.contains(event.target as Node)
            ) {
                setOpen(false);
            }
        };

        //attach listners on open
        if (open) {
            document.addEventListener("mousedown", handleClickOutside);
            document.addEventListener("touchstart", handleClickOutside);
        }

        //clean lisners
        return () => {
            document.removeEventListener("mousedown", handleClickOutside);
            document.removeEventListener("touchstart", handleClickOutside);
        };
    }, [open]);

    return (
        <TooltipProvider>
            <Tooltip>
                <TooltipTrigger asChild>
                    <Button
                        ref={buttonRef}
                        variant="default"
                        onClick={handleClick}
                        disabled={isEditMode}
                    >
                        <Sparkles className="text-primary-foreground" />
                    </Button>
                </TooltipTrigger>
                <TooltipContent className="flex flex-row justify-center items-center">
                    <span className="text-sm">Generate a dashboard</span>
                    <KbdGroup>
                        <Kbd>Ctrl</Kbd>
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
                            <Tabs defaultValue="generate" className="flex flex-col h-full w-full">
                                <TabsList>
                                    <TabsTrigger value="generate">Generate</TabsTrigger>
                                    <TabsTrigger value="history">History</TabsTrigger>
                                </TabsList>
                                <div className="flex-1 min-h-0 w-full relative">
                                    <TabsContent
                                        value="generate"
                                        className="absolute inset-0 m-0 data-[state=active]:flex flex-col"
                                    >
                                        <GenerateDashInput />
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
