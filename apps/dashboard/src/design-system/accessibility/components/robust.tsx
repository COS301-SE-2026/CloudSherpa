import SubSectionHeading from "@/design-system/shared/components/subsectionHeading";
import { Alert, AlertDescription, AlertTitle } from "@/components/atoms/alert";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/atoms/card";

export default function Robust() {
    return (
        <div className="flex flex-col gap-6">
            <SubSectionHeading
                title="4. Robust"
                description="Content must be robust enough that it can be interpreted reliably by a wide variety of user agents, including assistive technologies."
            />

            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <Card>
                    <CardHeader>
                        <CardTitle className="text-lg">ARIA</CardTitle>
                    </CardHeader>
                    <CardContent className="flex flex-col gap-4">
                        <p className="text-sm">
                            Strive to add things like aria-labels to as much components as possible
                            to so assistive technologies can be used to navigate the application
                        </p>
                    </CardContent>
                </Card>
            </div>
        </div>
    );
}
