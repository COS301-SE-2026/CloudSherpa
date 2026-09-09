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

export function getReasoning(recommendation: Recommendation): string {
    const { ruleId, actionType, evidence } = recommendation;

    if (!evidence || Object.keys(evidence).length === 0) {
        return `We recommend you ${actionType.toLowerCase()} this resource based on current usage policies.`;
    }

    // dictionary templates
    switch (ruleId) {
        case "COMPUTE-DOWNSIZE": {
            const cpu = extractMetricDetails(evidence, "CPU");
            if (cpu) {
                return `Consider downsizing this compute instance. Over the last ${cpu.days} days, the P95 CPU utilization peaked at only ${cpu.formattedValue}, indicating the resource is over-provisioned.`;
            }
            break;
        }
        case "COMPUTE-DOWNSIZE-MEMORY": {
            const memory = extractMetricDetails(evidence, "Memory");
            if (memory) {
                return `Consider downsizing this compute instance. Over the last ${memory.days} days, the P95 Memory utilization stayed below ${memory.formattedValue}, suggesting excess memory allocation.`;
            }
            break;
        }
        case "COMPUTE-TERMINATE-IDLE": {
            const cpu = extractMetricDetails(evidence, "CPU");
            const network = extractMetricDetails(evidence, "Network");
            const days = cpu?.days || network?.days || "4";

            let text = `This resource appears to be completely idle. Over the last ${days} days`;
            if (cpu) text += `, maximum CPU utilization was ${cpu.formattedValue}`;
            if (network) text += ` and maximum Network In was ${network.formattedValue}`;
            return text + `. Terminating it is recommended to reduce costs.`;
        }
        case "COMPUTE-SUSPEND-IDLE": {
            const cpu = extractMetricDetails(evidence, "CPU");
            const network = extractMetricDetails(evidence, "Network");
            const days = cpu?.days || network?.days || "4";

            let text = `This resource shows minimal active usage. Over the last ${days} days`;
            if (cpu) text += `, P95 CPU utilization was only ${cpu.formattedValue}`;
            if (network) text += ` with maximum Network In at ${network.formattedValue}`;
            return text + `. Suspending it during off-hours is recommended.`;
        }
    }

    // fallback future rules
    const evidenceSentences = Object.entries(evidence)
        .map(([key, value]) => {
            const parsed = parseEvidenceKey(key);
            if (!parsed) return "";

            const formattedVal = formatValue(parsed.metricName, value);
            const days = parsed.timeframe.replace("d", "");
            const agg = parsed.aggregation.toUpperCase();

            return `the ${agg} ${parsed.metricName} was ${formattedVal} over the last ${days} days`;
        })
        .filter(Boolean);

    const joinedEvidence = evidenceSentences.join(" and ");

    switch (actionType) {
        case "TERMINATE":
            return `We recommend terminating this resource because ${joinedEvidence}.`;
        case "SUSPEND":
            return `We recommend suspending this resource because ${joinedEvidence}.`;
        case "DOWNSIZE":
            return `We recommend downsizing this resource because ${joinedEvidence}.`;
        case "MODERNIZE":
            return `We recommend modernizing this resource because ${joinedEvidence}.`;
        default:
            return `Action recommended based on the following monitored metrics: ${joinedEvidence}.`;
    }
}
