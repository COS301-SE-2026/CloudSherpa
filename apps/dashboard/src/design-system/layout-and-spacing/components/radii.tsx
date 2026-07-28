import { Radius } from "@/design-system/layout-and-spacing/types/layoutAndSpacing";
import SubSectionHeading from "@/design-system/shared/components/subsectionHeading";

interface RadiiProps {
    radii: Radius[];
}

export default function Radii({ radii }: Readonly<RadiiProps>) {
    return (
        <div className="space-y-6">
            <SubSectionHeading
                title="Radii"
                description="Corner rounding is a tactful way of reducing the harshness of the sharp corners in a ui as well as making it feel more modern.
            Our system uses minimal rounding in our corners like rounded-md to help CloudSherpa feel modern, but keep it's professional appeal, since corners that are rounded too much might com across as slightly informal
            and won't fit our brand."
            />
            <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-6 md:gap-8">
                {radii.map((r) => (
                    <div key={r.name} className="flex flex-col gap-3">
                        <div
                            className="h-24 w-full bg-primary/10 border-2 border-primary "
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
