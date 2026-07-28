import SubSectionHeading from "@/design-system/shared/components/subsectionHeading";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/atoms/card";

export default function Understandable() {
    return (
        <div className="flex flex-col gap-6">
            <SubSectionHeading
                title="3. Understandable"
                description="Users must be able to understand the information as well as the operation of the user interface."
            />

            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <Card>
                    <CardHeader>
                        <CardTitle className="text-lg">Form Labels</CardTitle>
                    </CardHeader>
                    <CardContent className="flex flex-col gap-4">
                        <p className="text-sm">
                            Explicit label tags are required on all form inputs. Placeholder text is
                            transient and cannot be used as a substitute for a permanent, visible
                            label.
                        </p>
                    </CardContent>
                </Card>

                <Card>
                    <CardHeader>
                        <CardTitle className="text-lg">Document Language</CardTitle>
                    </CardHeader>
                    <CardContent className="flex flex-col gap-4">
                        <p className="text-sm">
                            The root HTML document must declare a valid language attribute to ensure
                            screen readers apply the correct pronunciation and dialect rules.
                        </p>
                    </CardContent>
                </Card>
            </div>
        </div>
    );
}
