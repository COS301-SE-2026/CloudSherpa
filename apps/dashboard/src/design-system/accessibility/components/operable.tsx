import SubSectionHeading from "@/design-system/shared/components/subsectionHeading";
import { Alert, AlertDescription, AlertTitle } from "@/components/atoms/alert";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/atoms/card";

export default function Operable() {
    return (
        <div className="flex flex-col gap-6">
            <SubSectionHeading
                title="Operable"
                description="Users must be able to operate the interface. The interface cannot require interaction that a user cannot perform."
            />

            <Alert>
                <AlertTitle className="text-base font-semibold">
                    Actionable Directive: Touch Targets
                </AlertTitle>
                <AlertDescription className="text-sm">
                    Automated testing flagged icon buttons (e.g., password input toggles) measuring
                    20x20px. All interactive touch targets in this design system must be updated to
                    have a minimum clickable area of 24x24px to comply with WCAG spacing rules.
                </AlertDescription>
            </Alert>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <Card>
                    <CardHeader>
                        <CardTitle className="text-lg">Keyboard Navigation</CardTitle>
                    </CardHeader>
                    <CardContent className="flex flex-col gap-4">
                        <p className="text-sm">
                            Every interactive element must be natively reachable via keyboard.
                            Components must not use tabindex values greater than 0, ensuring the tab
                            order flows logically with the DOM structure. Keyboard traps are
                            strictly forbidden.
                        </p>
                    </CardContent>
                </Card>

                <Card>
                    <CardHeader>
                        <CardTitle className="text-lg">Focus States</CardTitle>
                    </CardHeader>
                    <CardContent className="flex flex-col gap-4">
                        <p className="text-sm">
                            Highly visible focus rings are mandated across all interactive
                            components so keyboard users know exactly where they are on the page at
                            all times.
                        </p>
                    </CardContent>
                </Card>
            </div>
        </div>
    );
}
