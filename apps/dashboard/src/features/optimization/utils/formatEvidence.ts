export const formatEvidenceText = (key: string, value: number): string => {
    if (key === "completenessRatio") return "";

    const metricName = key.split("_")[0];

    const isPercentage = metricName.toLowerCase().includes("utilization") || value < 1;

    if (isPercentage) {
        const percentage = (value * 100).toFixed(1);
        return `${metricName}: ${percentage}%`;
    }

    const cleanValue = Number.isInteger(value) ? value : Number(value.toFixed(1));
    return `${metricName}: ${cleanValue}`;
};
