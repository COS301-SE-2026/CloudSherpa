import { Card, CardHeader, CardTitle, CardFooter, CardContent } from "@/components/atoms/card";
import { Shade, ColorFormat } from "@/design-system/colours/types/colours";

interface ShadesProps {
    name: string;
    shades: Shade[];
    format: ColorFormat;
}

export default function Shades({ name, shades, format }: Readonly<ShadesProps>) {
    return (
        <div className="">
            <h2 className="capitalize">{name}</h2>
            <div className="grid grid-cols-2 sm:grid-cols-4 md:grid-cols-6 lg:grid-cols-11 gap-4">
                {shades.map((shade) => {
                    return (
                        <Card key={shade.position} style={{ backgroundColor: shade.hex }}>
                            <CardHeader className="p-3 pb-2">
                                <CardTitle className="text-sm font-medium">
                                    {shade.position}
                                </CardTitle>
                            </CardHeader>
                            <CardFooter className="">
                                <span
                                    className="text-xs text-neutral-500 font-mono lowercase truncate"
                                    title={shade[format]}
                                >
                                    {shade[format]}
                                </span>
                            </CardFooter>
                        </Card>
                    );
                })}
            </div>
        </div>
    );
}
