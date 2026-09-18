import { Card, CardDescription, CardHeader, CardTitle } from "@/components/atoms/card";
import { Sparkles } from "lucide-react";
import { Button } from "@/components/atoms/button";
import { useState } from "react";

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
                    <Card className="h-160 w-120">
                        <CardHeader>
                            <CardTitle>Dashboard Constructor</CardTitle>
                            <CardDescription>
                                Describe what you want to monitor and which resource to pull the
                                data from
                            </CardDescription>
                        </CardHeader>
                    </Card>
                </div>
            )}
        </>
    );
}
