import {
    Card,
    CardTitle,
    CardDescription,
    CardHeader,
    CardContent,
    CardFooter,
} from "@/components/atoms/card";
import SubSectionHeading from "@/design-system/shared/components/subsectionHeading";

export default function Cards() {
    return (
        <div className="space-y-6">
            <SubSectionHeading
                title="Cards"
                description="Cards are versatile. It's an easy way to create components with a formal and consistent structure since it makes use of
            header,title,description,content and footer card components for easy and clean implementation."
            />
            <Card className="w-50">
                <CardHeader>
                    <CardTitle>Card Title Section</CardTitle>
                    <CardDescription>Card Description Section</CardDescription>
                </CardHeader>
                <CardContent>
                    <p>Card content section</p>
                </CardContent>
                <CardFooter>Card Footer section</CardFooter>
            </Card>
        </div>
    );
}
