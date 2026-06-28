import { useEffect, useState } from "react";

export function useChartTheme() {
  const [colors, setColors] = useState<string[]>([]);

  useEffect(() => {
    const style = getComputedStyle(document.documentElement);

    const getThemeColor = (token: string) => {
      const val = style.getPropertyValue(token).trim();
      if (!val) return undefined;

      if (val.startsWith("#") || val.startsWith("rgb") || val.startsWith("hsl")) {
        return val;
      }
      return `hsl(${val})`;
    };

    const themeColors = [
      getThemeColor("--chart-1") || "#0f766e",
      getThemeColor("--chart-2") || "#1d4ed8",
      getThemeColor("--chart-3") || "#0369a1",
      getThemeColor("--chart-4") || "#4338ca",
      getThemeColor("--chart-5") || "#be185d",
    ];
    
    // eslint-disable-next-line react-hooks/set-state-in-effect
    setColors(themeColors);
  }, []);

  return { colors };
}
