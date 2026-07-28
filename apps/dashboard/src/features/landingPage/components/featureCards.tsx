"use client";

import { useState } from "react";
import {
    BrainCircuit,
    ChartBar,
    Wallet,
    Cloud,
    Zap,
    ArrowLeft,
    type LucideIcon,
} from "lucide-react";
import { Button } from "@/components/atoms/button";

/*
- has all features of cloudsherpa
- gives detailed insights of the feature and how cloudshepra helps with it
- is as required by the demo 2 specs
*/

type Features = {
    icons: LucideIcon;
    name: string;
    description: string;
    subDescription: string;
    benefit: { label: string; details: string }[];
};

const FEATURES: Features[] = [
    {
        icons: BrainCircuit,
        name: "AI forecasting",
        subDescription: "Predict cloud costs before they increase",
        description:
            "Enables proactive financial decisions and better cost control. It moves beyond reacting to cloud bills",
        benefit: [
            {
                label: "Prevent unexpected costs",
                details: "Estimate your cloud costs early on which reduces unexpected expenses",
            },

            {
                label: "Take action early",
                details:
                    "Identify costs in advance which gives your team time to optimize their resources before spending grows",
            },

            {
                label: "Improve financial planning",
                details:
                    "Provides your teams with reliable cost projections which increases the accuracy of budgeting and forecasting",
            },

            {
                label: "Replace reactive cost management",
                details:
                    "Spend more time proactively optimizing your cloud environments rather than investigating unexpected costs",
            },
        ],
    },

    {
        icons: ChartBar,
        name: "Deep analytics",
        subDescription: "Get complete visibility into your cloud spend",
        description:
            "Empowers every team to make informed decisions by understanding exactly where their cloud budget is being spent",
        benefit: [
            {
                label: "Improve accountability",
                details:
                    "Allocate cloud costs by teams with tag based cost allocation, which ensures clear ownership and transparency",
            },

            {
                label: "Identify wasted resources",
                details:
                    "Automatically find idle resources which you can eliminate without spending valuable time",
            },

            {
                label: "Support every stakeholder",
                details:
                    "Dashboards can be optimized according to the users desires. They can monitor what they want to on each dashboard",
            },

            {
                label: "Make financial reporting simpler",
                details:
                    "Data is accurate and organised, making financial reporting processes easier",
            },
        ],
    },

    {
        icons: Wallet,
        name: "Budget control",
        subDescription: "Stay in control of your cloud spending",
        description:
            "CloudSherpa allows you to set budgets in confidence and prevents you from overspending",
        benefit: [
            {
                label: "Prevents exceeding your budget",
                details:
                    "Can scale down or pause resources when spending reaches predefined thresholds which avoids unexpected costs",
            },

            {
                label: "Gives budget ownership to teams",
                details:
                    "Budgets can be assigned to teams while maintaining visibility and control",
            },

            {
                label: "Protect your financial goals",
                details:
                    "Enforce spending limits that keep cloud costs aligned with your financial objectives",
            },

            {
                label: "Improves spending accountability",
                details:
                    "Tracks budget changes with clear audit trails, ensures transparency and accountability",
            },
        ],
    },

    {
        icons: Cloud,
        name: "Multi-cloud environments",
        subDescription: "Manages AWS",
        description:
            "CloudSherpa collapses multiple dashboards and cloud bills into one uniform picture",
        benefit: [
            {
                label: "Only one source of truth available",
                details: "View costs of multiple cloud providers on one platform",
            },

            {
                label: "Comparison of cloud costs",
                details:
                    "Most effective and informed decisions can be made as all data is displayed in the same way",
            },

            {
                label: "Better resource management",
                details: "Improves cost allocation, reporting of results and resource management",
            },

            {
                label: "Simplify multiple cloud platforms",
                details:
                    "Instead of haveing multiple platforms to be monitored, all can be replaced with CloudSherpa",
            },
        ],
    },

    {
        icons: Zap,
        name: "Quick setup",
        subDescription: "Begin optimizing your cloud costs sooner",
        description:
            "Get up and running quickly with a simple onboarding process. Connect your cloud accounts and being monitoring your resources immediately",
        benefit: [
            {
                label: "Simple startup",
                details:
                    "CloudSherpa has minimal configuration and process to go through to begin use of it",
            },

            {
                label: "Historical costs",
                details:
                    "Will be used to determine optimized opportunities on how to improves your costs instantly",
            },

            {
                label: "Gain insights from the first day",
                details:
                    "Dashboards can be populated with widgets in which the user desires to monitor. They can be added and removed and any time",
            },

            {
                label: "Security",
                details:
                    "All of your credentials are encrypted and will never be exposed with CloudSherpa",
            },
        ],
    },
];

function FeaturesDetails({
    card,
    onBack,
}: Readonly<{
    card: Features;
    onBack: () => void;
}>) {
    const Icons = card.icons;

    return (
        <div style={{ width: "100%", maxWidth: 1100, animation: "slideInRight 0.3s ease" }}>
            <Button
                onClick={onBack}
                className="mb-7 transition-all hover:scale-[1.02]"
                variant="default"
                size="default"
            >
                {" "}
                <ArrowLeft className="mr-2 h-4 w-4" /> Back to features{" "}
            </Button>

            <div className="p-10 rounded-2xl border border-border bg-card">
                <div className="flex items-center gap-5 mb-6">
                    <div className="w-14 h-14 rounded-xl flex items-center justify-center flex-shrink-0 bg-muted/20">
                        {" "}
                        <Icons size={28} className="text-muted-foreground" />{" "}
                    </div>

                    <div>
                        <h2 className="text-2xl font-bold text-foreground mb-1"> {card.name} </h2>

                        <p className="text-sm text-muted-foreground opacity-70">
                            {" "}
                            {card.subDescription}{" "}
                        </p>
                    </div>
                </div>

                <div className="border-t border-border mb-7" />

                <p className="text-sm text-muted-foreground leading-relaxed mb-8 max-w-2xl">
                    {" "}
                    {card.description}{" "}
                </p>

                <div className="text-xs font-semibold tracking-widest uppercase text-muted-foreground opacity-40 mb-5">
                    {" "}
                    Key Benefits{" "}
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    {" "}
                    {card.benefit.map((forBenefits) => (
                        <div
                            key={forBenefits.label}
                            className="p-5 rounded-xl border border-border bg-card/50"
                        >
                            <div className="flex items-center gap-2.5 mb-2.5">
                                <div className="text-sm font-semibold text-primary">
                                    {" "}
                                    {forBenefits.label}{" "}
                                </div>
                            </div>

                            <p className="text-sm text-muted-foreground leading-relaxed m-0 opacity-70">
                                {" "}
                                {forBenefits.details}{" "}
                            </p>
                        </div>
                    ))}
                </div>
            </div>
        </div>
    );
}

export function Features() {
    const [selectedCardIndex, setSelectedCardIndex] = useState<number | null>(null);

    const handlingCardClicked = (selectedIndex: number) => {
        setSelectedCardIndex(selectedIndex);
    };

    const handlingBackButton = () => {
        setSelectedCardIndex(null);
    };

    return (
        <section id="features" className="relative overflow-hidden py-24">
            <div className="max-w-6xl mx-auto px-6">
                {selectedCardIndex === null ? (
                    <>
                        <div className="text-center mb-14">
                            <p className="text-xs font-semibold tracking-widest uppercase mb-4 text-muted-foreground">
                                {" "}
                                Core Features{" "}
                            </p>

                            <h2 className="text-3xl md:text-5xl font-bold text-foreground">
                                {" "}
                                Everything a FinOps team needs{" "}
                            </h2>
                        </div>

                        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                            {FEATURES.map((forFeatures, forIndex) => {
                                const Icons = forFeatures.icons;

                                return (
                                    <Button
                                        key={forFeatures.name}
                                        onClick={() => handlingCardClicked(forIndex)}
                                        variant="ghost"
                                        className="group relative p-6 rounded-2xl border border-border bg-card overflow-hidden hover:border-border/60 transition-all duration-300 hover:-translate-y-0.5 w-full flex flex-col items-center justify-center text-center h-auto"
                                    >
                                        <div className="relative flex flex-col items-center justify-center">
                                            <div className="w-10 h-10 rounded-xl flex items-center justify-center mb-4 bg-muted/20">
                                                {" "}
                                                <Icons
                                                    size={18}
                                                    className="text-muted-foreground"
                                                />{" "}
                                            </div>

                                            <h3 className="text-sm font-semibold text-primary mb-2">
                                                {" "}
                                                {forFeatures.name}{" "}
                                            </h3>

                                            <p className="text-sm text-muted-foreground leading-relaxed m-0">
                                                {" "}
                                                {forFeatures.subDescription}{" "}
                                            </p>
                                        </div>
                                    </Button>
                                );
                            })}
                        </div>
                    </>
                ) : (
                    <div className="flex justify-center">
                        {" "}
                        <FeaturesDetails
                            card={FEATURES[selectedCardIndex]}
                            onBack={handlingBackButton}
                        />{" "}
                    </div>
                )}
            </div>
        </section>
    );
}
