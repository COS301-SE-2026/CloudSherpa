import SummaryCard from "@/features/intelligence/components/usage/summaryCard";

export default function UsageIntelligence() {
    return (
        <div className="h-full w-full">
            <section className="grid grid-cols-1 lg:grid-cols-3">
                <SummaryCard
                    title="Max Usage"
                    unit="%"
                    pastUsage={60}
                    predictedUsage={90}
                    description="max usage over time period days"
                />
                <SummaryCard
                    title="Min Usage"
                    unit="%"
                    pastUsage={20}
                    predictedUsage={45}
                    description="max usage over time period days"
                />
                <SummaryCard
                    title="Average Usage"
                    unit="%"
                    pastUsage={50}
                    predictedUsage={60}
                    description="max usage over time period days"
                />
            </section>
        </div>
    );
}
