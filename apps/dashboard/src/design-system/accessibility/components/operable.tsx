import SubSectionHeading from "@/design-system/shared/components/subsectionHeading";
import { Alert, AlertDescription, AlertTitle } from "@/components/atoms/alert";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/atoms/card";

export default function Operable() {
    return (
        <div className="flex flex-col gap-6">
            <SubSectionHeading
                title="2.Operable"
                description="Users must be able to operate the interface. The interface cannot require interaction that a user cannot perform."
            />

            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <Card>
                    <CardHeader>
                        <CardTitle className="text-lg">Keyboard Navigation</CardTitle>
                    </CardHeader>
                    <CardContent className="flex flex-col gap-4">
                        <p className="text-sm">
                            Every interactive element must be natively reachable via keyboard.
                            Components must not use tabindex values greater than 0, ensuring the tab
                            order flows logically with the DOM structure.
                        </p>
                    </CardContent>
                </Card>

                <Card>
                    <CardHeader>
                        <CardTitle className="text-lg">Focus States</CardTitle>
                    </CardHeader>
                    <CardContent className="flex flex-col gap-4">
                        <p className="text-sm">
                            Focus states are baked into Shadcn interactive components and help users
                            identify where they are on the page.
                        </p>
                    </CardContent>
                </Card>
            </div>
        </div>
    );
}
