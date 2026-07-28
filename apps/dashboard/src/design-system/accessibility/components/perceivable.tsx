import SubSectionHeading from "@/design-system/shared/components/subsectionHeading";
import { Badge } from "@/components/atoms/badge";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/atoms/card";

export default function Perceivable() {
    return (
        <div className="flex flex-col gap-6">
            <SubSectionHeading
                title="1. Perceivable"
                description="Users must be able to perceive the information being presented. It cannot be invisible to all of their senses."
            />

            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <Card>
                    <CardHeader>
                        <CardTitle className="text-lg">Text Contrast Ratios</CardTitle>
                    </CardHeader>
                    <CardContent className="flex flex-col gap-4">
                        <p className="text-sm">
                            Our text elements strictly adhere to WCAG 2.2 AA contrast requirements
                            to ensure legibility. Note: The raw hex values displayed in our
                            documentation color swatches are an intentional exception and are not
                            used in the main application UI.
                        </p>
                        <div className="flex flex-wrap gap-2">
                            <Badge variant="secondary">4.5:1 Standard Body Text</Badge>
                            <Badge variant="secondary">3:1 Large Text (18pt+)</Badge>
                        </div>
                    </CardContent>
                </Card>

                <Card>
                    <CardHeader>
                        <CardTitle className="text-lg">Non-Text Contrast Ratios</CardTitle>
                    </CardHeader>
                    <CardContent className="flex flex-col gap-4">
                        <p className="text-sm">
                            Visual boundaries and states that identify user interface components
                            (such as input borders, button edges, and focus rings) must also meet
                            strict contrast requirements against their adjacent backgrounds.
                        </p>
                        <div className="flex flex-wrap gap-2">
                            <Badge variant="secondary">3:1 UI Component Boundaries</Badge>
                            <Badge variant="secondary">3:1 Graphical Objects</Badge>
                        </div>
                    </CardContent>
                </Card>

                <Card className="md:col-span-2">
                    <CardHeader>
                        <CardTitle className="text-lg">Color Independence</CardTitle>
                    </CardHeader>
                    <CardContent className="flex flex-col gap-4">
                        <p className="text-sm">
                            Color alone must never be used to convey state, meaning, or system
                            errors. For example, a validation error cannot simply turn an input
                            border red; it must be accompanied by an icon and an explicitly linked
                            error message text.
                        </p>
                    </CardContent>
                </Card>
            </div>
        </div>
    );
}
