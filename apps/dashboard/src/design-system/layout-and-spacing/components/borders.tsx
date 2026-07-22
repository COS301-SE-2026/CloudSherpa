import { Border } from "@/design-system/layout-and-spacing/types/layoutAndSpacing";

interface BordersProps {
    borders: Border[];
}

export default function Borders({ borders }: Readonly<BordersProps>) {
    return (
        <div className="space-y-6">
            <div>
                <h2 className="text-2xl font-bold tracking-tight mb-2">Border Widths</h2>
                <p className="text-muted-foreground max-w-2xl">
                    The structural scale of border thicknesses used to define component boundaries
                    and dividers.
                </p>
            </div>

            <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-6 md:gap-8">
                {borders.map((r) => {
                    const tailwindClass =
                        r.name === "base" || r.name === "DEFAULT" ? "border" : `border-${r.name}`;

                    return (
                        <div key={r.name} className="flex flex-col gap-3">
                            <div
                                className="h-24 w-full bg-primary/10 border-primary rounded-md "
                                style={{
                                    borderWidth: `var(--border-width-${r.name}, ${r.value})`,
                                }}
                            />
                            <div>
                                <h3 className="font-mono text-sm font-bold text-foreground">
                                    {tailwindClass}
                                </h3>
                                <p className="text-xs font-mono text-muted-foreground">{r.value}</p>
                            </div>
                        </div>
                    );
                })}
            </div>
        </div>
    );
}
