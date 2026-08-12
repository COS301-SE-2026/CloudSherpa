import {
    Drawer,
    DrawerTrigger,
    DrawerContent,
    DrawerTitle,
    DrawerHeader,
    DrawerDescription,
} from "@/components/atoms/drawer";
import { Button } from "@/components/atoms/button";
import {
    Accordion,
    AccordionItem,
    AccordionTrigger,
    AccordionContent,
} from "@/components/atoms/accordion";

interface RecDrawer {
    title: string;
    recommendations: string[];
}

export default function RecDrawer({ title, recommendations }: Readonly<RecDrawer>) {
    return (
        <Drawer direction="right">
            <DrawerTrigger asChild>
                <Button variant="secondary">View</Button>
            </DrawerTrigger>
            <DrawerContent>
                <DrawerHeader>
                    <DrawerTitle>{title}</DrawerTitle>
                    <DrawerDescription>
                        some long and boring text to act as a short description for this component
                    </DrawerDescription>
                    {recommendations.map((recommendation) => (
                        <div key={recommendation}>{recommendation}</div>
                    ))}
                </DrawerHeader>
            </DrawerContent>
        </Drawer>
    );
}
