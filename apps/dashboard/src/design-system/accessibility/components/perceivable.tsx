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
                        <CardTitle className="text-lg">Contrast Ratios</CardTitle>
                    </CardHeader>
                    <CardContent className="flex flex-col gap-4">
                        <p className="text-sm">
                            We strive to keep accessibility in mind when selecting colours for our
                            application theme. Thus our colours mostly comply to WCAG 2.2 standards.
                            Note: the colour semantic displaye at the top of the design system does
                            not adhere to this due to the nature of how it is setup, but the main
                            application should adhere to this standard as best as possible.
                        </p>
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
                            contrast requirements against their adjacent backgrounds.
                        </p>
                    </CardContent>
                </Card>
            </div>
        </div>
    );
}
