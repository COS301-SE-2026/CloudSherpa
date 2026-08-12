import RecommendationGroup from "@/features/optimization/components/recommendationGroup";
import { Recommendation } from "@/features/optimization/types/recommendations";

const mockRecommendations: Recommendation[] = [
    {
        recommendation_id: "a1b2c3d4",
        resource_id: "hypixel-prod-01",
        provider: "AWS",
        action_type: "DOWNSIZE",
        status: "ACTIVE",
        current_configuration: "t3.xlarge",
        target_configuration: "t3.medium",
        estimated_monthly_savings: 120.5,
        currency: "USD",
        evidence: 18,
        completenessRatio: 0.99,
    },
    {
        recommendation_id: "b2c3d4e5",
        resource_id: "abandoned-dev-db",
        provider: "AWS",
        action_type: "TERMINATE",
        status: "ACTIVE",
        current_configuration: "m5.large",
        target_configuration: null,
        estimated_monthly_savings: 75.0,
        currency: "USD",
        evidence: 12,
        completenessRatio: 0.99,
    },
];

export default function Recommendations() {
    return (
        <div className="flex flex-col h-full w-full p-6 gap-4">
            <header>
                <h1 className="text-3xl font-semibold">Optimization Recommendations</h1>
            </header>

            <RecommendationGroup connection="mock" recommendations={mockRecommendations} />
        </div>
    );
}
