export interface ResourceMeta {
    id: string;
    name: string;
}

export interface ThemeMeta {
    id: string;
    label: string;
    metricNames: string[];
}

export interface generatePromptProps {
    taskDescription: string;
    providers: string[];
    connections: {
        id: string;
        name: string;
    }[];
    resources: ResourceMeta[];
    themes: ThemeMeta[];
    metricsByResource: Record<string, string[]>;
}

export const generatePrompt = ({
    taskDescription,
    connections,
    resources,
    themes,
    metricsByResource,
}: generatePromptProps): string => {
    const themeMetricsByResource = themes.map((theme) => {
        const resourceMatches = resources
            .map((resource) => {
                const availableMetrics = metricsByResource[resource.id] ?? [];

                const relevantMetrics = availableMetrics.filter((metric) =>
                    theme.metricNames.includes(metric)
                );

                return {
                    resource,
                    metrics: relevantMetrics,
                };
            })
            .filter(({ metrics }) => metrics.length > 0);

        return {
            theme,
            resourceMatches,
        };
    });

    let prompt = `### USER REQUEST\n${taskDescription}\n\n`;

    prompt += `### SELECTED SOURCES\n`;

    connections.forEach((connection) => {
        prompt += `- ${connection.name} (ID: ${connection.id})\n`;
    });

    prompt += `\n### SELECTED RESOURCES\n`;

    resources.forEach((resource) => {
        prompt += `- **${resource.name}** (ID: ${resource.id})`;

        prompt += `\n`;

        const metrics = metricsByResource[resource.id] ?? [];

        if (metrics.length > 0) {
            prompt += `  Available metrics: ${metrics.join(", ")}\n`;
        }
    });

    prompt += `\n### SELECTED THEMES\n`;

    themeMetricsByResource.forEach(({ theme, resourceMatches }) => {
        prompt += `- **${theme.label}**\n`;

        resourceMatches.forEach(({ resource, metrics }) => {
            prompt += `  - ${resource.name}: ${metrics.join(", ")}\n`;
        });
    });

    prompt += `\n### DASHBOARD CONTEXT\n`;
    prompt += `Use the selected sources, resources, and theme-relevant metrics when constructing the dashboard. Only use metrics that are available for the selected resources.\n`;

    return prompt.trim();
};
