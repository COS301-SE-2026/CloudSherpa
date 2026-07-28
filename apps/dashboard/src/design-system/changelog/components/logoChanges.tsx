import SubSectionHeading from "@/design-system/shared/components/subsectionHeading";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/atoms/card";

export default function LogoChanges() {
    return (
        <div className="space-y-6">
            <SubSectionHeading
                title="Logo Changes"
                description="Our logo had to change due to our new themes and we had to add a logo specifically for dark mode"
            />
            <div className="space-y-6 flex flex-col">
                <span className="text-base">Previous Logo</span>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                    <Card className="light flex flex-col">
                        <CardHeader>
                            <CardTitle>Primary (Full Colour) </CardTitle>
                        </CardHeader>
                        <CardContent className="flex flex-col justify-center! items-center!">
                            <img
                                src="/PrevCloudSherpaLogo.png"
                                alt="Primary CloudSherpa Logo"
                                className="h-16 w-auto mb-4"
                            />
                        </CardContent>
                    </Card>
                </div>
            </div>

            <div className="space-y-6 flex flex-col">
                <span className="text-base">Current Logo</span>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                    <Card className="light flex flex-col">
                        <CardHeader>
                            <CardTitle>Primary (Full Colour) </CardTitle>
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
                            <CardTitle>Primary (Dark) </CardTitle>
                        </CardHeader>
                        <CardContent className="flex flex-col justify-center! items-center!">
                            <img
                                src="/CloudSherpaLogoDark.svg"
                                alt="Primary CloudSherpa Logo"
                                className="h-16 w-auto mb-4"
                            />
                        </CardContent>
                    </Card>
                </div>
            </div>
        </div>
    );
}
