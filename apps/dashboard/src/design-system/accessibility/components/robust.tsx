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

            <Alert>
                <AlertTitle className="text-base font-semibold">
                    Actionable Directive: Accessible Names
                </AlertTitle>
                <AlertDescription className="text-sm">
                    Several interactive triggers are missing discernible text. Any button that
                    relies entirely on an icon (e.g., Quick Navigate popover, HEX toggle, password
                    visibility toggle) must include an explicit aria-label attribute describing its
                    function.
                </AlertDescription>
            </Alert>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <Card>
                    <CardHeader>
                        <CardTitle className="text-lg">ARIA Standards</CardTitle>
                    </CardHeader>
                    <CardContent className="flex flex-col gap-4">
                        <p className="text-sm">
                            Components must strictly adhere to ARIA specifications. Roles must be
                            applied only to compatible elements, and all aria attributes must
                            contain valid, correctly formatted values.
                        </p>
                    </CardContent>
                </Card>

                <Card>
                    <CardHeader>
                        <CardTitle className="text-lg">Testing Protocol</CardTitle>
                    </CardHeader>
                    <CardContent className="flex flex-col gap-4">
                        <p className="text-sm">
                            While automated tools like Lighthouse provide a necessary baseline for
                            compliance, they only catch programmatic errors. Components must undergo
                            manual testing with a screen reader and keyboard-only navigation.
                        </p>
                    </CardContent>
                </Card>
            </div>
        </div>
    );
}
