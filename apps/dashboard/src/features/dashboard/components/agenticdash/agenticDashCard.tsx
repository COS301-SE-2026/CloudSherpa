import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/atoms/card";
import { Sparkles } from "lucide-react";
import { Button } from "@/components/atoms/button";
import { useState } from "react";
import { Tabs, TabsList, TabsTrigger, TabsContent } from "@/components/atoms/tabs";
import GenerateDashInput from "@/features/dashboard/components/agenticdash/generate";
import History from "@/features/dashboard/components/agenticdash/history";

export default function AgenticDashCard() {
    const [open, setOpen] = useState(false);

    const handleClick = () => {
        setOpen(!open);
    };

    return (
        <>
            <Button variant="default" onClick={handleClick}>
                <Sparkles className="text-foreground" />
            </Button>

            {open && (
                <div className="fixed bottom-6 right-6 z-50 flex flex-col items-end">
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
        </>
    );
}
