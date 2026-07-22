import { Radius } from "@/design-system/layout-and-spacing/types/layoutAndSpacing";

interface RadiiProps {
    radii: Radius[];
}

export default function Radii({ radii }: Readonly<RadiiProps>) {
    return (
        <div className="space-y-6">
            <div>
                <h2 className="text-2xl font-bold tracking-tight mb-2">Radii</h2>
                <p className="text-muted-foreground max-w-2xl">Some description about our Radii</p>
            </div>
            <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-6 md:gap-8">
                {radii.map((r) => (
                    <div key={r.name} className="flex flex-col gap-3">
                        <div
                            className="h-24 w-full bg-primary/10 border-2 border-primary/30 "
                            style={{
                                borderRadius: `var(--radius-${r.name}, ${r.value})`,
                            }}
                        />

                        <div>
                            <h3 className="font-mono text-sm font-bold text-foreground">
                                rounded-{r.name === "base" ? "DEFAULT" : r.name}
                            </h3>
                            <p className="text-xs font-mono text-muted-foreground">{r.value}</p>
                        </div>
                    </div>
                ))}
            </div>
        </div>
    );
}
