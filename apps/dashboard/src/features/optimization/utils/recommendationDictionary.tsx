import { Recommendation } from "@/features/optimization/types/recommendations";

export function getRecommendationDictionary(rec: Recommendation) {
    const Highlight = ({ children }: { children: React.ReactNode }) => (
        <span className="font-semibold text-foreground">{children}</span>
    );

    switch (rec.action_type) {
        case "DOWNSIZE":
            return (
                <p className="text-sm text-muted-foreground leading-relaxed">
                    We observed a maximum utilization of <Highlight>{rec.evidence}%</Highlight> on
                    this resource over a monitored period. Which leads us to believe your resource
                    is underutilized, and we recommend downsizing
                </p>
            );

        case "TERMINATE":
            return (
                <p className="text-sm text-muted-foreground leading-relaxed">
                    This resource appears abandoned with an average utilization of{" "}
                    <Highlight>{rec.evidence}%</Highlight> over a monitored period. Since it is
                    incurring costs without providing value, we recommend permanently terminating
                    this resource.
                </p>
            );

        case "MODERNIZE":
            return (
                <p className="text-sm text-muted-foreground leading-relaxed">
                    You are currently running on legacy hardware. generation will provide superior
                    performance and better cost-efficiency for the exact same workload.
                </p>
            );

        case "SUSPEND":
            return (
                <p className="text-sm text-muted-foreground leading-relaxed">
                    We detected a predictable usage pattern with low utilization (
                    <Highlight>{rec.evidence}%</Highlight>) during off-hours. We recommend
                    implementing a power schedule to automatically suspend this resource when not in
                    active use.
                </p>
            );

        default:
            return (
                <p className="text-sm text-muted-foreground leading-relaxed">
                    Review this resource to ensure its configuration matches your current
                    operational requirements.
                </p>
            );
    }
}
