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

export const getMetricUnit = (metricName: string): string => {
    const nameLower = metricName.toLowerCase();
    if (
        nameLower.includes("utilization") ||
        nameLower.includes("percentage") ||
        nameLower.includes("pressure")
    ) {
        return "%";
    }
    if (nameLower.includes("network") || nameLower.includes("bytes")) {
        return " MB";
    }
    if (nameLower.includes("memory") && !nameLower.includes("utilization")) {
        return " GB";
    }
    return "";
};

const formatNumber = (num: number): string => {
    return Number.isInteger(num)
        ? num.toLocaleString()
        : Number.parseFloat(num.toFixed(2)).toLocaleString();
};

export const formatValue = (metricName: string, value: number): string => {
    const nameLower = metricName.toLowerCase();

    //handle percentages
    if (
        nameLower.includes("utilization") ||
        nameLower.includes("percentage") ||
        nameLower.includes("pressure")
    ) {
        return `${formatNumber(value)}%`;
    }

    // handle bytes
    if (nameLower.includes("bytes") || nameLower.includes("network")) {
        if (value === 0) return "0 B";

        const k = 1024;
        const sizes = ["B", "KB", "MB", "GB", "TB", "PB"];
        const i = Math.floor(Math.log(Math.abs(value)) / Math.log(k));

        const calculatedSize = value / Math.pow(k, i);
        return `${formatNumber(calculatedSize)} ${sizes[i]}`;
    }

    // assume base GB if not utilization
    if (nameLower.includes("memory")) {
        if (value >= 1024) {
            const tbValue = value / 1024;
            return `${formatNumber(value)} GB`;
        }
        const formattedNum = Number.isInteger(value)
            ? value.toLocaleString()
            : Number.parseFloat(value.toFixed(2)).toLocaleString();
        return `${formattedNum} GB`;
    }

    // fallback for requests connections and IOPs
    return formatNumber(value);
};

// extract format specific metrics
const extractMetricDetails = (evidence: Record<string, number>, keyword: string) => {
    const key = Object.keys(evidence).find((k) => k.toLowerCase().includes(keyword.toLowerCase()));
    if (!key) return null;

    const parsed = parseEvidenceKey(key);
    return {
        formattedValue: formatValue(parsed?.metricName || keyword, evidence[key]),
        days: parsed?.timeframe.replace("d", "") || "4",
    };
};

const titleCaseMetric = (metricName: string) => {
    return metricName
        .split(" ")
        .map((word) => {
            const lower = word.toLowerCase();
            if (lower === "cpu") return "CPU";
            if (lower === "io") return "I/O";
            if (lower === "iops") return "IOPS";
            if (lower === "http") return "HTTP";
            if (lower === "db") return "DB";
            return lower.charAt(0).toUpperCase() + lower.slice(1);
        })
        .join(" ");
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
                        Consider <strong className="text-chart-3">downsizing</strong> this compute
                        instance. 95% of the last <strong>{cpu.days} days</strong>, your{" "}
                        <strong>CPU utilization</strong> stayed below{" "}
                        <strong>{cpu.formattedValue}</strong>, indicating the resource is
                        over-provisioned.
                    </span>
                );
            }
            break;
        }
        case "COMPUTE-DOWNSIZE-MEMORY": {
            const memory = extractMetricDetails(evidence, "Memory");
            return (
                <span>
                    Consider <strong className="text-chart-3">downsizing</strong> this compute
                    instance. 95% of the last <strong>{memory?.days || "4"} days</strong>, your{" "}
                    <strong>Memory utilization</strong> stayed below{" "}
                    <strong>{memory?.formattedValue}</strong>, suggesting excess memory allocation.
                </span>
            );
        }
        case "COMPUTE-DOWNSIZE-STORAGE-TIER": {
            const disk = extractMetricDetails(evidence, "disk space");
            return (
                <span>
                    Consider <strong className="text-chart-3">downsizing</strong> this storage
                    volume. 95% of the last <strong>{disk?.days || "4"} days</strong>, your{" "}
                    <strong>Disk Space Used</strong> stayed below{" "}
                    <strong>{disk?.formattedValue}</strong>.
                </span>
            );
        }
        case "RDS-DOWNSIZE-CPU-DB": {
            const cpu = extractMetricDetails(evidence, "CPU");
            const conn = extractMetricDetails(evidence, "connections");
            const days = cpu?.days || conn?.days || "4";

            return (
                <span>
                    Consider <strong className="text-chart-3">downsizing</strong> this database. 95%
                    of the last {days} days, the <strong>CPU utilization</strong> was below{" "}
                    <strong>{cpu?.formattedValue}</strong> with a maximum of{" "}
                    <strong>{conn?.formattedValue} Database Connections</strong>.
                </span>
            );
        }
        case "RDS-DOWNSIZE-CPU": {
            const cpu = extractMetricDetails(evidence, "cpu");
            return (
                <span>
                    Consider <strong className="text-chart-3">downsizing</strong> this database. 95%{" "}
                    of the last {cpu?.days || "4"} days, the
                    <strong>CPU utilization</strong> was below{" "}
                    <strong>{cpu?.formattedValue}</strong>.
                </span>
            );
        }
        case "RDS-DOWNSIZE-STORAGE-TIER": {
            const disk = extractMetricDetails(evidence, "disk space");
            return (
                <span>
                    Consider <strong className="text-chart-3">downsizing</strong> this database
                    storage. 95% of the last {disk?.days || "30"} days, the
                    <strong>Disk Space Used</strong> stayed below{" "}
                    <strong>{disk?.formattedValue}</strong>.
                </span>
            );
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
                            , this resource was observed to have a maximum of{" "}
                            <strong>{cpu.formattedValue}</strong> <strong>CPU utilization</strong>
                        </span>
                    )}
                    {network && (
                        <span>
                            {" "}
                            {cpu ? "and a" : ","} maximum <strong>Network In</strong> of{" "}
                            <strong>{network.formattedValue}</strong>
                        </span>
                    )}
                    .<strong className="text-destructive"> Terminating</strong> it is recommended to
                    reduce costs.
                </span>
            );
        }
        case "COMPUTE-TERMINATE-NO-DISK-IO": {
            const read = extractMetricDetails(evidence, "read");
            const write = extractMetricDetails(evidence, "write");
            const days = read?.days || write?.days || "4";

            return (
                <span>
                    This resource shows practically <strong>no disk activity</strong>. Over the last{" "}
                    {days} days, this resource had a maximum <strong>Disk Read</strong> of{" "}
                    <strong>{read?.formattedValue}</strong> and <strong>Disk Write</strong> of{" "}
                    <strong>{write?.formattedValue}</strong>.{" "}
                    <strong className="text-destructive">Terminating</strong> is recommended.
                </span>
            );
        }
        case "COMPUTE-TERMINATE-NO-NETWORK": {
            const netIn = extractMetricDetails(evidence, "network in");
            const netOut = extractMetricDetails(evidence, "network out");
            const days = netIn?.days || netOut?.days || "4";

            return (
                <span>
                    This resource shows <strong>no significant network traffic</strong>. Over the
                    last {days} days, this resource had a maximum <strong>Network In</strong> of{" "}
                    <strong>{netIn?.formattedValue}</strong> and a maximum{" "}
                    <strong>Network Out</strong> of <strong>{netOut?.formattedValue}</strong>.{" "}
                    <strong className="text-destructive">Terminating</strong> is recommended.
                </span>
            );
        }
        case "COMPUTE-TERMINATE-LOW": {
            const cpu = extractMetricDetails(evidence, "CPU");
            const memory = extractMetricDetails(evidence, "Memory");
            const days = cpu?.days || memory?.days || "4";

            return (
                <span>
                    This resource has <strong>exceptionally low overall usage</strong>. Over the
                    last {days} days, the maximum <strong>CPU</strong> for this resource was{" "}
                    <strong>{cpu?.formattedValue}</strong> and 95% of the time{" "}
                    <strong>Memory</strong> stayed below <strong>{memory?.formattedValue}</strong>.{" "}
                    <strong className="text-destructive">Terminating</strong> is recommended.
                </span>
            );
        }
        case "RDS-TERMINATE-IDLE": {
            const cpu = extractMetricDetails(evidence, "CPU");
            const conn = extractMetricDetails(evidence, "connections");
            const days = cpu?.days || conn?.days || "4";

            return (
                <span>
                    This database appears <strong>completely idle</strong>. Over the last {days}{" "}
                    days, this database had a maximum of <strong>{cpu?.formattedValue}</strong>{" "}
                    <strong>CPU utilization</strong> and a maximum of{" "}
                    <strong>{conn?.formattedValue}</strong> <strong>Database Connections</strong>.{" "}
                    <strong className="text-destructive">Terminating</strong> it is recommended.
                </span>
            );
        }
        case "RDS-TERMINATE-NO-CONNECTIONS": {
            const conn = extractMetricDetails(evidence, "connections");
            return (
                <span>
                    This database has <strong>no active connections</strong>. Over the last{" "}
                    {conn?.days || "4"} days, this database had a maximum of{" "}
                    <strong>{conn?.formattedValue}</strong> <strong>Database Connections</strong>.{" "}
                    <strong className="text-destructive">Terminating</strong> it is recommended.
                </span>
            );
        }
        case "RDS-TERMINATE-NO-IO": {
            const read = extractMetricDetails(evidence, "read");
            const write = extractMetricDetails(evidence, "write");
            const days = read?.days || write?.days || "4";

            return (
                <span>
                    This database shows <strong>no I/O operations</strong>. Over the last {days}{" "}
                    days, this database had a maximum of <strong>{read?.formattedValue}</strong>{" "}
                    <strong>Read IOPS</strong> and <strong>{write?.formattedValue}</strong>{" "}
                    <strong>Write IOPS</strong>.{" "}
                    <strong className="text-destructive">Terminating</strong> it is recommended.
                </span>
            );
        }
        case "RDS-TERMINATE-NO-NETWORK": {
            const netIn = extractMetricDetails(evidence, "network in");
            const netOut = extractMetricDetails(evidence, "network out");
            const days = netIn?.days || netOut?.days || "4";

            return (
                <span>
                    This database shows <strong>no network activity</strong>. Over the last {days}{" "}
                    days, the maximum <strong>Network In</strong> for this database was{" "}
                    <strong>{netIn?.formattedValue}</strong> and <strong>Network Out</strong> was{" "}
                    <strong>{netOut?.formattedValue}</strong>.{" "}
                    <strong className="text-destructive">Terminating</strong> it is recommended.
                </span>
            );
        }
        case "CLOUDRUN-TERMINATE-IDLE": {
            const req = extractMetricDetails(evidence, "requests");
            const cpu = extractMetricDetails(evidence, "cpu");
            const days = req?.days || cpu?.days || "4";
            return (
                <span>
                    This Cloud Run service appears <strong>idle</strong>. Over the last {days} days,
                    this service had a maximum of <strong>{req?.formattedValue}</strong>{" "}
                    <strong>HTTP Requests</strong> and 95% of the time the{" "}
                    <strong>Container CPU</strong> was below <strong>{cpu?.formattedValue}</strong>.{" "}
                    <strong className="text-destructive">Terminating</strong> is recommended.
                </span>
            );
        }
        case "CLOUDRUN-TERMINATE-NO-REQUESTS": {
            const req = extractMetricDetails(evidence, "requests");
            return (
                <span>
                    This Cloud Run service is receiving <strong>no traffic</strong>. Over the last{" "}
                    {req?.days || "4"} days, this service had a maximum of{" "}
                    <strong>{req?.formattedValue}</strong> <strong>HTTP Requests</strong>.{" "}
                    <strong className="text-destructive">Terminating</strong> is recommended.
                </span>
            );
        }
        case "CLOUDRUN-TERMINATE-LOW-CPU-MEM": {
            const cpu = extractMetricDetails(evidence, "cpu");
            const mem = extractMetricDetails(evidence, "memory");
            const days = cpu?.days || mem?.days || "4";
            return (
                <span>
                    This Cloud Run service has <strong>exceptionally low resource usage</strong>.
                    95% of the last <strong>{days} days</strong>, the <strong>CPU</strong> for this
                    service was below <strong>{cpu?.formattedValue}</strong> and the{" "}
                    <strong>Memory</strong> was below <strong>{mem?.formattedValue}</strong>.{" "}
                    <strong className="text-destructive">Terminating</strong> is recommended.
                </span>
            );
        }
        case "CLOUDRUN-TERMINATE-LOW-INSTANCES": {
            const instances = extractMetricDetails(evidence, "instances");
            return (
                <span>
                    This Cloud Run service has scaled to zero. Over the last{" "}
                    {instances?.days || "4"} days, this service had a maximum of{" "}
                    <strong>{instances?.formattedValue}</strong> <strong>Running Instances</strong>.{" "}
                    <strong className="text-destructive">Terminating</strong> is recommended.
                </span>
            );
        }
        case "COMPUTE-SUSPEND-IDLE": {
            const cpu = extractMetricDetails(evidence, "CPU");
            const network = extractMetricDetails(evidence, "Network");
            const days = cpu?.days || network?.days || "4";

            return (
                <span>
                    This resource shows <strong>minimal active usage</strong>. 95% of the last{" "}
                    <strong>{days} days</strong>
                    {cpu && (
                        <span>
                            , your <strong>CPU utilization</strong> was only{" "}
                            <strong>{cpu.formattedValue}</strong>
                        </span>
                    )}
                    {network && (
                        <span>
                            {" "}
                            {cpu ? "with the" : ","} maximum <strong>Network In</strong> at{" "}
                            <strong>{network.formattedValue}</strong>
                        </span>
                    )}
                    .<strong className="text-yellow-600"> Suspending</strong> it during off-hours is
                    recommended.
                </span>
            );
        }
        case "COMPUTE-SUSPEND-LOW-CPU-MEMORY": {
            const cpu = extractMetricDetails(evidence, "CPU");
            const memory = extractMetricDetails(evidence, "Memory");
            const days = cpu?.days || memory?.days || "4";

            return (
                <span>
                    This resource is consistently underutilized. 95% of the last{" "}
                    <strong>{days} days</strong>, the <strong>CPU</strong> was below{" "}
                    <strong>{cpu?.formattedValue}</strong> and the <strong>Memory</strong> was below{" "}
                    <strong>{memory?.formattedValue}</strong>.{" "}
                    <strong className="text-yellow-600">Suspending</strong> it during off-hours is
                    recommended.
                </span>
            );
        }
        case "COMPUTE-SUSPEND-LOW-NETWORK": {
            const netIn = extractMetricDetails(evidence, "network in");
            const netOut = extractMetricDetails(evidence, "network out");
            const days = netIn?.days || netOut?.days || "4";

            return (
                <span>
                    This resource shows minimal network traffic. Over the last {days} days, the
                    maximum <strong>Network In</strong> was <strong>{netIn?.formattedValue}</strong>{" "}
                    and <strong>Network Out</strong> was <strong>{netOut?.formattedValue}</strong>.{" "}
                    <strong className="text-yellow-600">Suspending</strong> it during off-hours is
                    recommended.
                </span>
            );
        }
        case "COMPUTE-SUSPEND-LOW-DISK-BYTES": {
            const read = extractMetricDetails(evidence, "read");
            const write = extractMetricDetails(evidence, "write");
            const days = read?.days || write?.days || "4";

            return (
                <span>
                    This resource shows minimal disk activity. Over the last {days} days, maximum{" "}
                    <strong>Disk Read</strong> was <strong>{read?.formattedValue}</strong> and{" "}
                    <strong>Disk Write</strong> was <strong>{write?.formattedValue}</strong>.{" "}
                    <strong className="text-yellow-600">Suspending</strong> it during off-hours is
                    recommended.
                </span>
            );
        }
        case "CLOUDRUN-SUSPEND-IDLE": {
            const req = extractMetricDetails(evidence, "requests");
            const cpu = extractMetricDetails(evidence, "CPU");
            const days = req?.days || cpu?.days || "4";

            return (
                <span>
                    This Cloud Run service is inactive. Over the last {days} days, this service had
                    a maximum of <strong>{req?.formattedValue}</strong>{" "}
                    <strong>HTTP Requests</strong> and its <strong>Container CPU</strong> was below{" "}
                    <strong>{cpu?.formattedValue}</strong> for 95% of the time.{" "}
                    <strong className="text-yellow-600">Suspending</strong> the service is
                    recommended.
                </span>
            );
        }
        case "RDS-SUSPEND-LOW-IO": {
            const read = extractMetricDetails(evidence, "read");
            const write = extractMetricDetails(evidence, "write");
            const days = read?.days || write?.days || "4";
            return (
                <span>
                    This database shows minimal I/O activity. Over the last {days} days, the
                    database had a maximum of <strong>{read?.formattedValue}</strong>{" "}
                    <strong>Read IOPS</strong> and <strong>{write?.formattedValue}</strong>{" "}
                    <strong>Write IOPS</strong>.{" "}
                    <strong className="text-yellow-600">Suspending</strong> it is recommended.
                </span>
            );
        }

        case "COMPUTE-UPSCALE-CPU": {
            const cpu = extractMetricDetails(evidence, "CPU");
            return (
                <span>
                    Consider <strong className="text-warning">upscaling</strong> this compute
                    instance. 95% of the last <strong>{cpu?.days || "4"} days</strong>, your{" "}
                    <strong>CPU utilization</strong> was above{" "}
                    <strong>{cpu?.formattedValue}</strong>, indicating the resource is
                    under-provisioned and may be bottlenecking performance.
                </span>
            );
        }
        case "RDS-UPSCALE-CPU": {
            const cpu = extractMetricDetails(evidence, "cpu");
            return (
                <span>
                    Consider <strong className="text-warning">upscaling</strong> this
                    database&apos;s compute tier. 95% of the last{" "}
                    <strong>{cpu?.days || "4"} days</strong>, your <strong>CPU utilization</strong>{" "}
                    was above <strong>{cpu?.formattedValue}</strong>, indicating a potential
                    performance bottleneck.
                </span>
            );
        }
        case "RDS-UPSCALE-MEMORY": {
            const mem = extractMetricDetails(evidence, "memory");
            return (
                <span>
                    Consider <strong className="text-warning">upscaling</strong> this
                    database&apos;s memory tier. 95% of the last{" "}
                    <strong>{mem?.days || "4"} days</strong>, your{" "}
                    <strong>Memory utilization</strong> was above{" "}
                    <strong>{mem?.formattedValue}</strong>, increasing the risk of Out-Of-Memory
                    events.
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
            const prettyMetric = titleCaseMetric(parsed.metricName);

            if (parsed.aggregation.toLowerCase() === "p95") {
                return `95% of the last ${days} days, the ${prettyMetric} stayed below ${formattedVal}`;
            } else {
                return `the maximum ${prettyMetric} was ${formattedVal} over the last ${days} days`;
            }
        })
        .filter(Boolean);

    const joinedEvidence = evidenceFallbackSentence.join(" and ");

    switch (actionType) {
        case "TERMINATE":
            return (
                <span>
                    We recommend <strong className="text-destructive">terminating</strong> this
                    resource because {joinedEvidence}.
                </span>
            );
        case "SUSPEND":
            return (
                <span>
                    We recommend <strong className="text-yellow-500">suspending</strong> this
                    resource because {joinedEvidence}.
                </span>
            );
        case "DOWNSIZE":
            return (
                <span>
                    We recommend <strong className="text-chart-3">downsizing</strong> this resource
                    because {joinedEvidence}.
                </span>
            );
        case "UPSCALE":
            return (
                <span>
                    We recommend <strong className="text-warning">upscaling</strong> this resource
                    because {joinedEvidence}.
                </span>
            );
        default:
            return (
                <span>
                    Action recommended based on the following monitored metrics: {joinedEvidence}.
                </span>
            );
    }
}
