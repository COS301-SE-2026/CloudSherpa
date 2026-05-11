import { create } from 'zustand'
import { MetricStore } from '@/types/metric'

/*
    ====EXAMPLE USAGE====
    const cpuMetrics = useMetricStore(
        (state) => {
            state.seriesByKey[`${resourceId}:cpu`] ?? []
        }
    );
*/

 export const useMetricStore = create<MetricStore>(
    (set) => ({
        seriesByKey: {},

        addMetric: (metric) => {
            const key = `${metric.resource_id}:${metric.metricType}`;

            set((state) => ({
                seriesByKey: {
                    ...state.seriesByKey,

                    [key]: [
                        ...(state.seriesByKey[key] ?? []),
                        metric,
                    ]
                },
            }));
        }
    })
 )