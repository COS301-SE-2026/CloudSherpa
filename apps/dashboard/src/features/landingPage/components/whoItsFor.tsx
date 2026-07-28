"use client";

import { Card, CardContent } from "@/components/atoms/card";

/*
- specifies that cloudsherpa is for everyone
*/

type ListOfWho = {
    who: string;
    description: string;
};

const WHO: ListOfWho[] = [
    {
        who: "Anyone on any technical level",
        description:
            "CloudSherpa has intuitive dashboard and is structured in ways that anyone can decipher what is being displayed on the dashboard",
    },

    {
        who: "Cloud engineer",
        description:
            "On a more technical level users who want rightsizing recommendations and anomaly alerts immediately so that cloud costs can be reduced",
    },
];

export function WhoItsFor() {
    return (
        <section id="who-it-s-for" className="relative overflow-hidden py-24">
            <div className="max-w-6xl mx-auto px-6">
                <div className="text-center mb-14">
                    <p className="text-xs font-semibold tracking-widest uppercase mb-4 text-muted-foreground">
                        {" "}
                        Who Its For{" "}
                    </p>

                    <h2 className="text-3xl md:text-5xl font-bold text-foreground">
                        {" "}
                        Built for every stakeholder{" "}
                    </h2>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-4 max-w-2xl mx-auto">
                    {WHO.map((forWho) => (
                        <Card key={forWho.who} className="flex items-center justify-center">
                            <CardContent className="p-6 text-center flex flex-col items-center justify-center">
                                <p className="text-xs font-bold uppercase tracking-widest mb-3 text-primary">
                                    {" "}
                                    {forWho.who}{" "}
                                </p>

                                <p className="text-sm text-muted-foreground leading-relaxed">
                                    {" "}
                                    {forWho.description}{" "}
                                </p>
                            </CardContent>
                        </Card>
                    ))}
                </div>
            </div>
        </section>
    );
}
