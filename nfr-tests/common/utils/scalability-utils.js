export function handleDegradationSummary(data, baselineKey, loadKey) {
    console.log(JSON.stringify(data));
    const baseline = data.metrics[baselineKey];
    const load = data.metrics[loadKey];

    const baselineP95 = baseline.values['p(95)'];
    const loadP95 = load.values['p(95)'];

    const degradation = ((loadP95 - baselineP95) / baselineP95) * 100;

    console.log(`Baseline p95: ${baselineP95} ms`);
    console.log(`Load p95:     ${loadP95} ms`);
    console.log(`Degradation:  ${degradation.toFixed(2)}%`);

    return {};
}