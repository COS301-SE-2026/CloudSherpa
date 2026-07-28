import { Card, CardContent, CardHeader, CardTitle } from "@/components/atoms/card";
import SubSectionHeading from "@/design-system/shared/components/subsectionHeading";

interface ColourChangeRepresentationProps {
    title: string;
    value: string;
}

function ColourChangeRepresentation({ title, value }: Readonly<ColourChangeRepresentationProps>) {
    return (
        <div className="flex flex-col gap-2">
            <span className="text-small">{title}</span>
            <div className="flex flex-row gap-4">
                <div className="h-20 w-20 rounded-md border" style={{ backgroundColor: value }} />
            </div>
        </div>
    );
}

export default function ColourChanges() {
    return (
        <div className="space-y-6">
            <SubSectionHeading
                title="Colour Changes"
                description="A log of all changes brought about to our colour palette. Due to contrast issues we had to reimplement our colour palette. We went from a very blue UI to 
            a more muted palatable colour with a blue hue."
            />
            <Card className="w-full">
                <CardHeader>
                    <CardTitle>Previous Colour Palette</CardTitle>
                    <span className="text-small">
                        This is a summary of our previous colour palette
                    </span>
                </CardHeader>
                <CardContent className="flex wrap gap-4">
                    <ColourChangeRepresentation title="primary" value="#2F2FE4" />
                    <ColourChangeRepresentation title="secondary" value="#162E93" />
                    <ColourChangeRepresentation title="accent" value="#FFD700" />
                    <div className="flex flex-col gap-2">
                        <span className="text-small">Neutral min/max values</span>
                        <div className="flex flex-row gap-4">
                            <div className="h-20 w-20 rounded-md border bg-slate-50" />
                            <div className="h-20 w-20 rounded-md border bg-slate-800" />
                        </div>
                    </div>
                    <ColourChangeRepresentation title="accent" value="#FFD700" />
                    <ColourChangeRepresentation title="Success" value="#10B981" />
                    <ColourChangeRepresentation title="Error" value="#EF4444" />
                    <ColourChangeRepresentation title="Warning" value="#EA580C" />
                </CardContent>
            </Card>
            <Card className="w-full">
                <CardHeader>
                    <CardTitle>Current Colour Palette</CardTitle>
                    <span className="text-small">
                        This is a summary of our current colour palette. Note we ommited the
                        secondary and accent colours and rather use our neutral colours for those
                        applications instead in an attempt to make the ui less flashy. That way all
                        the attention can be on the charts and data with their more colorful UI.
                    </span>
                </CardHeader>
                <CardContent className="flex wrap gap-4">
                    <ColourChangeRepresentation title="primary" value="#2b7fff" />
                    <div className="flex flex-col gap-2">
                        <span className="text-small">Neutral mix/max values</span>
                        <div className="flex flex-row gap-4">
                            <div className="h-20 w-20 rounded-md border bg-foreground" />
                            <div className="h-20 w-20 rounded-md border bg-background" />
                        </div>
                    </div>
                    <ColourChangeRepresentation title="Success" value="#38d52a" />
                    <ColourChangeRepresentation title="Error" value="#d72828" />
                    <ColourChangeRepresentation title="Warning" value="#d98226" />
                    <div className="flex flex-col gap-2">
                        <span className="text-small">Chart Colours</span>
                        <div className="flex flex-row gap-4">
                            <div className="h-20 w-20 rounded-md border bg-chart-1" />
                            <div className="h-20 w-20 rounded-md border bg-chart-2" />
                            <div className="h-20 w-20 rounded-md border bg-chart-3" />
                            <div className="h-20 w-20 rounded-md border bg-chart-4" />
                            <div className="h-20 w-20 rounded-md border bg-chart-5" />
                        </div>
                    </div>
                </CardContent>
            </Card>
        </div>
    );
}
