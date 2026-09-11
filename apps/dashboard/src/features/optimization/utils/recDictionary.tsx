import { Recommendation } from "@/features/optimization/types/recommendations";

const parseEvidenceKey = (key: string) => {
    const parts = key.split("_");
    if (parts.length < 3) return null;

    const timeframe = parts.pop()!;
    const aggregation = parts.pop()!;
    const metricName = parts.join(" ");

    if (!timeframe.endsWith("d")) return null;
    return { metricName, aggregation, timeframe };
};

const formatValue = (metricName: string, value: number): string => {
    const nameLower = metricName.toLowerCase();
    const isPercentage =
        nameLower.includes("utilization") ||
        nameLower.includes("percentage") ||
        nameLower.includes("pressure");

    return isPercentage ? `${value.toFixed(2)}%` : value.toFixed(2);
};

// extract format specific metrics
const extractMetricDetails = (evidence: Record<string, number>, keyword: string) => {
    const key = Object.keys(evidence).find((k) => k.includes(keyword));
    if (!key) return null;

    const parsed = parseEvidenceKey(key);
    return {
        formattedValue: formatValue(parsed?.metricName || keyword, evidence[key]),
        days: parsed?.timeframe.replace("d", "") || "4",
    };
};

export default function RecommendationReasoning({
    recommendation,
}: Readonly<{
    recommendation: Recommendation;
}>) {
    const { ruleId, actionType, evidence } = recommendation;

    if (!evidence || Object.keys(evidence).length === 0) {
        return (
            <span>
                We recommend you <strong>{actionType.toLowerCase()}</strong> this resource based on
                current usage policies.
            </span>
        );
    }

    // dictionary templates
    switch (ruleId) {
        case "COMPUTE-DOWNSIZE": {
            const cpu = extractMetricDetails(evidence, "CPU");
            if (cpu) {
                return (
                    <span>
                        Consider <strong className="text-warning">downsizing</strong> this compute
                        instance. Over the last <strong>{cpu.days} days</strong> , 95% of the time
                        your <strong>CPU utilization</strong> stayed below{" "}
                        <strong>{cpu.formattedValue}</strong>, indicating the resource is
                        over-provisioned.
                    </span>
                );
            }
            break;
        }
        case "COMPUTE-DOWNSIZE-MEMORY": {
            const memory = extractMetricDetails(evidence, "Memory");
            if (memory) {
                return (
                    <span>
                        Consider <strong className="text-warning">downsizing</strong> this compute
                        instance. Over the last {memory.days} days, 95% of the time your{" "}
                        <strong>Memory utilization</strong> stayed below{" "}
                        <strong>{memory.formattedValue}</strong>, suggesting excess memory
                        allocation.
                    </span>
                );
            }
            break;
        }
        case "COMPUTE-TERMINATE-IDLE": {
            const cpu = extractMetricDetails(evidence, "CPU");
            const network = extractMetricDetails(evidence, "Network");
            const days = cpu?.days || network?.days || "4";

            return (
                <span>
                    This resource appears to be <strong>completely idle</strong>. Over the last{" "}
                    {days} days
                    {cpu && (
                        <span>
                            , maximum <strong>CPU utilization</strong> was{" "}
                            <strong>{cpu.formattedValue}</strong>
                        </span>
                    )}
                    {network && (
                        <span>
                            {" "}
                            {cpu ? "and" : ","} maximum <strong>Network In</strong> was{" "}
                            <strong>{network.formattedValue}</strong>
                        </span>
                    )}
                    .<strong className="text-destructive"> Terminating</strong> it is recommended to
                    reduce costs.
                </span>
            );
        }
        case "COMPUTE-SUSPEND-IDLE": {
            const cpu = extractMetricDetails(evidence, "CPU");
            const network = extractMetricDetails(evidence, "Network");
            const days = cpu?.days || network?.days || "4";

            return (
                <span>
                    This resource shows <strong>minimal active usage</strong>. Over the last {days}{" "}
                    days
                    {cpu && (
                        <span>
                            , 95% of the time your <strong>CPU utilization</strong> was only{" "}
                            <strong>{cpu.formattedValue}</strong>
                        </span>
                    )}
                    {network && (
                        <span>
                            {" "}
                            {cpu ? "with" : ","} maximum <strong>Network In</strong> at{" "}
                            <strong>{network.formattedValue}</strong>
                        </span>
                    )}
                    .<strong className="text-yellow-600"> Suspending</strong> it during off-hours is
                    recommended.
                </span>
            );
        }
    }

    // fallback future rules
    const evidenceFallbackSentence = Object.entries(evidence)
        .map(([key, value]) => {
            const parsed = parseEvidenceKey(key);
            if (!parsed) return "";

            const formattedVal = formatValue(parsed.metricName, value);
            const days = parsed.timeframe.replace("d", "");
            const agg = parsed.aggregation.toUpperCase();

            return (
                <span key={key}>
                    the ${agg} ${parsed.metricName} was ${formattedVal} over the last ${days} days
                </span>
            );
        })
        .filter(Boolean);

    const joinedEvidence = evidenceFallbackSentence.join(" and ");

    switch (actionType) {
        case "TERMINATE":
            return (
                <span>
                    We recommend <strong className="text-destructive">terminating</strong> this
                    resource because ${joinedEvidence}.
                </span>
            );
        case "SUSPEND":
            return (
                <span>
                    We recommend <strong className="text-yellow-600">suspending</strong> this
                    resource because ${joinedEvidence}.
                </span>
            );
        case "DOWNSIZE":
            return (
                <span>
                    `We recommend <strong className="text-warning">downsizing</strong> this resource
                    because ${joinedEvidence}.
                </span>
            );
        default:
            return (
                <span>
                    Action recommended based on the following monitored metrics: ${joinedEvidence}.
                </span>
            );
    }
}
