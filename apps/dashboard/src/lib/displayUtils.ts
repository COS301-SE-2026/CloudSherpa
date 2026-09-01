import { timeMs } from "./timeUtils";

const GAP_THRESHOLD_MILLISECONDS = timeMs.minuteMs * 30;

export function sanitizeDisplaySeries<T>(
    dirtyValues: T[],
    timestampMs: (point: T) => number,
    buildPadFromCurrentPoint: (samplePoint: T) => T
): T[] {
    const sanatizedSeries: T[] = [];

    for (let i = 0; i < dirtyValues.length - 1; i++) {
        const current = dirtyValues[i];
        const next = dirtyValues[i + 1];

        sanatizedSeries.push(current);

        if (timestampMs(next) - timestampMs(current) > GAP_THRESHOLD_MILLISECONDS) {
            sanatizedSeries.push(buildPadFromCurrentPoint(current));
        }
    }

    const lastMetric = dirtyValues.at(-1);

    if (lastMetric !== undefined) {
        sanatizedSeries.push(lastMetric);
    }

    return sanatizedSeries;
}
