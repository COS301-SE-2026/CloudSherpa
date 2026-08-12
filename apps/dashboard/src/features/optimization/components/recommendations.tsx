import ConnectionGroup from "@/features/optimization/components/connectionGroups";

const mockRecommendations = [
    {
        recommendation_id: "a1b2c3d4",
        connection: "minecraft",
        resource_id: "hypixel-prod-01",
        provider: "AWS",
        action_type: "DOWNSIZE",
        status: "ACTIVE",
        current_configuration: {
            sku: "t3.2xlarge",
        },
        target_configuration: {
            sku: "t3.xlarge",
        },
        estimated_monthly_savings: 120.5,
        currency: "USD",
        evidence: {
            cpu_percent_p95_30d: 18.4,
            completeness_ratio: 0.99,
        },
    },
    {
        recommendation_id: "b2c3d4e5",
        connection: "minecraft",
        resource_id: "abandoned-dev-db",
        provider: "AWS",
        action_type: "TERMINATE",
        status: "ACTIVE",
        current_configuration: {
            sku: "m5.large",
        },
        target_configuration: null,
        estimated_monthly_savings: 75.0,
        currency: "USD",
        evidence: {
            network_out_p95_30d: 0.0,
            cpu_percent_p95_30d: 1.2,
            completeness_ratio: 1.0,
        },
    },
];

export default function Recommendations() {
    return (
        <div className="flex flex-col h-full w-full p-6 gap-4">
            <header>
                <h1 className="text-3xl font-semibold">Optimization Recommendations</h1>
            </header>

            {mockRecommendations.map((recommendation) => (
                <ConnectionGroup
                    provider={recommendation.provider}
                    connection={recommendation.connection}
                    estSumSavings={123}
                    recommendationsCount={2}
                />
            ))}
        </div>
    );
}
