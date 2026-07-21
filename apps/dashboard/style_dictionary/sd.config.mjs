import { formats } from "style-dictionary/enums";
import chroma from "chroma-js";

const extractValue = (t) =>
    t.value ?? t.$value ?? t.original?.value ?? t.original?.$value ?? "Missing";

const normalizedDocsFormat = {
    name: "json/normalized-docs",
    format: function ({ dictionary }) {
        const docs = {
            colors: [],
            spacing: [],
            borders: [],
            breakpoints: [],
            radii: [],
            typography: { family: [], size: [], weight: [], lineHeight: [] },
        };

        const palettes = {};

        dictionary.allTokens.forEach((token) => {
            const category = token.path[0];
            if (category === "color" && token.path.length === 3 && /^\d+$/.test(token.path[2])) {
                const paletteName = token.path[1];
                const position = Number.parseInt(token.path[2], 10);
                const hex = token.value || token.$value;
                if (!hex || typeof hex !== "string") return;

                const chromaColor = chroma(hex);

                if (!palettes[paletteName]) {
                    palettes[paletteName] = { name: paletteName, shades: [] };
                }

                palettes[paletteName].shades.push({
                    position,
                    hex,
                    rgb: chromaColor.css("rgb"),
                    hsl: chromaColor.css("hsl").replace(/,/g, ""),
                });
            } else if (category === "spacing") {
                docs.spacing.push({ name: token.path[1], value: extractValue(token) });
            } else if (category === "border") {
                const name = token.path[1] === "width" ? token.path[2] : token.path[1];
                docs.borders.push({ name: name, value: extractValue(token) });
            } else if (category === "breakpoint") {
                docs.breakpoints.push({ name: token.path[1], value: extractValue(token) });
            } else if (category === "radius") {
                docs.radii.push({ name: token.path[1], value: extractValue(token) });
            } else if (category === "font") {
                const type = token.path[1];
                if (docs.typography[type]) {
                    docs.typography[type].push({ name: token.path[2], value: extractValue(token) });
                }
            }
        });

        // cleanup
        Object.values(palettes).forEach((p) => p.shades.sort((a, b) => a.position - b.position));
        docs.colors = Object.values(palettes);
        docs.spacing.sort((a, b) => Number.parseFloat(a.name) - Number.parseFloat(b.name));
        docs.borders.sort((a, b) => Number.parseFloat(a.name) - Number.parseFloat(b.name));

        return JSON.stringify(docs, null, 2);
    },
};

const styleDictionaryConfig = {
    source: ["style_dictionary/**/*.json"],

    hooks: {
        formats: {
            "json/normalized-docs": normalizedDocsFormat.format,
        },
        transforms: {
            "name/shadcn": {
                type: "name",
                transitive: true,
                transform: (token) => {
                    if (token.path[0] === "theme") {
                        return token.path.slice(2).join("-");
                    }
                    return token.path.join("-");
                },
            },
        },
        filters: {
            "is-primitive": (token) => token.filePath.includes("primitives"),
            "is-light": (token) => token.filePath.includes("theme-light"),
            "is-dark": (token) => token.filePath.includes("theme-dark"),
        },
    },

    platforms: {
        css: {
            transforms: ["name/shadcn", "attribute/cti", "color/css", "size/rem"],
            buildPath: "src/app/tokens/",
            files: [
                {
                    destination: "primitives.css",
                    format: formats.cssVariables,
                    filter: "is-primitive",
                    options: {
                        outputReferences: true,
                    },
                },
                {
                    destination: "light.css",
                    format: formats.cssVariables,
                    filter: "is-light",
                    options: {
                        outputReferences: true,
                        selector: ":root",
                    },
                },
                {
                    destination: "dark.css",
                    format: formats.cssVariables,
                    filter: "is-dark",
                    options: {
                        outputReferences: true,
                        selector: ".dark",
                    },
                },
            ],
        },
        echarts: {
            transforms: ["name/shadcn", "color/hex"],
            buildPath: "src/app/tokens/",
            files: [
                {
                    destination: "chart-light.json",
                    format: formats.jsonFlat,
                    filter: "is-light",
                },
                {
                    destination: "chart-dark.json",
                    format: formats.jsonFlat,
                    filter: "is-dark",
                },
            ],
        },
        docs: {
            buildPath: "src/app/tokens/docs/",
            files: [
                {
                    destination: "design-tokens.json",
                    format: "json/normalized-docs",
                    filter: "is-primitive",
                },
            ],
        },
    },
};

export default styleDictionaryConfig;
