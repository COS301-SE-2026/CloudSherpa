export function getArraySummary(values?: number[]) {
    if (!values || values.length === 0) {
        return { min: 0, max: 0, avg: 0 };
    }

    let min = Infinity;
    let max = -Infinity;
    let sum = 0;

    for (let i = 0; i < values.length; i++) {
        const val = values[i];
        if (val < min) min = val;
        if (val > max) max = val;
        sum += val;
    }

    return {
        min: Math.floor(min),
        max: Math.ceil(max),
        avg: Math.round(sum / values.length),
    };
}
