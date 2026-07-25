export type ColorFormat = "hex" | "rgb" | "hsl";

export type Shade = {
    position: number;
    hex: string;
    rgb: string;
    hsl: string;
};

export type palette = {
    name: string;
    shades: Shade[];
};
