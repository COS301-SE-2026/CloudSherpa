"use client";
import {
    Drawer,
    DrawerTrigger,
    DrawerContent,
    DrawerTitle,
    DrawerHeader,
} from "@/components/atoms/drawer";
import { Button } from "@/components/atoms/button";
import { Recommendation } from "@/features/optimization/types/recommendations";
import {
    Accordion,
    AccordionTrigger,
    AccordionContent,
    AccordionItem,
} from "@/components/atoms/accordion";
import { Card } from "@/components/atoms/card";
import { useState } from "react";
import { X } from "lucide-react";

interface RecDrawer {
    connection: string;
    recommendations: Recommendation[];
}

export default function RecDrawer({ connection, recommendations }: Readonly<RecDrawer>) {
    const [isOpen, setIsOpen] = useState(false);
    return (
        <Drawer direction="right" dismissible={false} open={isOpen} onOpenChange={setIsOpen}>
            <DrawerTrigger asChild>
                <Button variant="secondary">View</Button>
            </DrawerTrigger>

            {/* drawer width could be volatile so will keep an eye on it */}
            <DrawerContent className="w-[90vw]! sm:w-[40vw]! sm:max-w-[1000px]!">
                <DrawerHeader className="flex flex-col">
                    <div className="flex flex-row lex-row justify-between items-center">
                        <DrawerTitle className="text-xl">{connection}</DrawerTitle>
                        <Button className="w-fit" variant="ghost" onClick={() => setIsOpen(false)}>
                            <X />
                        </Button>
                    </div>
                </DrawerHeader>
                <div className="p-4 overflow-y-auto">
                    <Accordion type="single" collapsible className="w-full flex flex-col gap-4">
                        {recommendations.map((rec) => (
                            <Card key={rec.recommendation_id} className="overflow-hidden shadow-sm">
                                <AccordionItem
                                    value={rec.recommendation_id}
                                    className="border-none"
                                >
                                    <AccordionTrigger className="hover:no-underline px-6 py-4">
                                        <div className="flex flex-row text-left gap-1">
                                            <span className="font-semibold text-base">
                                                {rec.resource_id}
                                            </span>
                                            <span className="text-sm text-muted-foreground font-normal">
                                                {rec.action_type}
                                            </span>
                                        </div>
                                    </AccordionTrigger>

                                    <AccordionContent className="px-6 pb-4">
                                        <div className="p-4 bg-muted/50 rounded-md">
                                            Details for this recommendation will go here later!
                                        </div>
                                    </AccordionContent>
                                </AccordionItem>
                            </Card>
                        ))}
                    </Accordion>
                </div>
            </DrawerContent>
        </Drawer>
    );
}
