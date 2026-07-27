import SubSectionHeading from "@/design-system/shared/components/subsectionHeading";
import { Card, CardContent, CardFooter, CardHeader, CardTitle } from "@/components/atoms/card";

export default function Variations() {
    return (
        <div className="space-y-6">
            <SubSectionHeading
                title="Variations"
                description="Our logo adapts to different contexts. Always use the provided SVG assets. The logo should never be stretched or warped in any way."
            />
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <Card className="light flex flex-col">
                    <CardHeader>
                        <CardTitle>Primary (Full Colour) </CardTitle>
                        <span className="text-small">
                            This is our primary logo, used for navbars, hero sections and brand
                            identity sections.
                        </span>
                    </CardHeader>
                    <CardContent className="flex flex-col justify-center! items-center!">
                        <img
                            src="/CloudSherpaLogo.svg"
                            alt="Primary CloudSherpa Logo"
                            className="h-16 w-auto mb-4"
                        />
                    </CardContent>
                </Card>

                <Card className="dark flex flex-col">
                    <CardHeader>
                        <CardTitle>Inverted (For Dark Backgrounds)</CardTitle>
                        <span className="text-small">This is our primary logo for dark mode.</span>
                    </CardHeader>
                    <CardContent className="flex flex-col justify-center! items-center!">
                        <img
                            src="/CloudSherpaLogo.svg"
                            alt="Primary CloudSherpa Logo"
                            className="h-16 w-auto mb-4"
                        />
                    </CardContent>
                </Card>

                <Card className="light flex flex-col">
                    <CardHeader>
                        <CardTitle>Icon Only (Restricted Space)</CardTitle>
                        <span className="text-small">
                            This is variant is used for navbars and as our favicon.
                        </span>
                    </CardHeader>
                    <CardContent className="flex flex-col justify-center! items-center!">
                        <img
                            src="/CloudSherpaFavicon.svg"
                            alt="Primary CloudSherpa Logo"
                            className="h-16 w-auto mb-4"
                        />
                    </CardContent>
                </Card>

                <Card className="light flex flex-col">
                    <CardHeader>
                        <CardTitle>Monotone (Watermarks / Print)</CardTitle>
                        <span className="text-small">
                            This logo is used as a watermark for CloudSherpa specific content.
                        </span>
                    </CardHeader>
                    <CardContent className="flex flex-col justify-center! items-center!">
                        <img
                            src="/CloudSherpaLogo.svg"
                            alt="Primary CloudSherpa Logo"
                            className="h-16 w-auto mb-4"
                        />
                    </CardContent>
                </Card>
            </div>
        </div>
    );
}
