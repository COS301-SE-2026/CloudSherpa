import { Line_Height } from "@/design-system/typography/types/typography";
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from "@/components/atoms/card";

interface LineHeightProps {
    LineHeights: Line_Height[];
}

export default function LineHeight({ LineHeights }: Readonly<LineHeightProps>) {
    return (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {LineHeights.map((lh) => (
                <Card key={lh.name} className="flex flex-col">
                    <CardHeader className="pb-3">
                        <CardTitle>
                            leading-{lh.name}
                        </CardTitle>
                        <CardDescription className="text-xs">{lh.value}</CardDescription>
                    </CardHeader>

                    <CardContent className="grow pt-0">
                        <p
                            className="text-sm text-muted-foreground"
                            style={{
                                lineHeight: `var(--font-lineHeight-${lh.name}, ${lh.value})`,
                            }}
                        >
                            This paragraph demonstrates the visual impact of the{" "}
                            <strong>{lh.name}</strong> line height token. Notice how the vertical
                            space between these sentences adjusts dynamically. Proper line height is
                            crucial for reading comprehension, accessibility, and overall UI
                            balance.
                        </p>
                    </CardContent>
                </Card>
            ))}
        </div>
    );
}
