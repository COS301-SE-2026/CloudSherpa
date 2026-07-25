import { Border } from "@/design-system/layout-and-spacing/types/layoutAndSpacing";
import SubSectionHeading from "@/design-system/shared/components/subsectionHeading";

interface BordersProps {
    borders: Border[];
}

export default function Borders({ borders }: Readonly<BordersProps>) {
    return (
        <div className="space-y-6">
            <SubSectionHeading
                title="Borders"
                description="Since we use Shadcn as our component library, borders are important, because the creator of shadcn much preferred a flat look for the ui and preferred borders over
            drop shadows. This sits well with our use case since a flashy ui that pops out to the user could potentially be distracting for some users, this is a minor inconvenience but affects the feel of CloudSherpa.
            We want the attention to be on the charts without having the ui feel 'muddy' thats why the use of borders are important. Luckily they are baked into the shadcn/ui components and don't need to be added manually
            The most commonly used borders in our project is border-0 and border-1. "
            />

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
