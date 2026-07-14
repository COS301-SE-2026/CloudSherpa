import { formats } from "style-dictionary/enums";

const styleDictionaryConfig = {
    source: ["style_dictionary/**/*.json"],

    hooks: {
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
    },
};

export default styleDictionaryConfig;
