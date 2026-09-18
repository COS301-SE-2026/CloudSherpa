import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/atoms/card";
import { Sparkles } from "lucide-react";
import { Button } from "@/components/atoms/button";
import { useState } from "react";
import { Tabs, TabsList, TabsTrigger, TabsContent } from "@/components/atoms/tabs";
import StandardDashInput from "@/features/dashboard/components/agenticdash/standard";

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
                    <Card className="h-160 w-120 ">
                        <CardHeader>
                            <CardTitle>Dashboard Constructor</CardTitle>
                            <CardDescription>
                                Describe what you want to monitor and which resource to pull the
                                data from
                            </CardDescription>
                        </CardHeader>
                        <CardContent className="h-full">
                            <Tabs defaultValue="standard" className="flex flex-col h-full w-full">
                                <TabsList>
                                    <TabsTrigger value="standard">Standard</TabsTrigger>
                                    <TabsTrigger value="history">History</TabsTrigger>
                                </TabsList>
                                <div className="h-full w-full p-0 flex flex-col">
                                    <TabsContent value="standard">
                                        {/* component for standard input (ie. normal textbox with submit button) */}
                                        <StandardDashInput />
                                    </TabsContent>
                                    <TabsContent value="history">
                                        {/* table content of past dashbaords and date created */}
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
