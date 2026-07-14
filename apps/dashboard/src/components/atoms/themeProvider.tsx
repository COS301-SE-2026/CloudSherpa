"use client";

import * as React from "react";
import { ThemeProvider as NextThemesProvider, useTheme } from "next-themes";
import { fetchUserTheme } from "@/lib/fetch/api-preferences";

function ThemePersistenceEnforcer({ children }: Readonly<{ children: React.ReactNode }>) {
    const { setTheme } = useTheme();

    React.useEffect(() => {
        fetchUserTheme()
            .then((data) => {
                if (data?.theme === "light" || data?.theme === "dark") {
                    setTheme(data.theme);
                }
            })
            .catch((error) => console.error("Failed to fetch user theme:", error));
    }, [setTheme]);
    return <>{children}</>;
}

export function ThemeProvider({
    children,
    ...props
}: Readonly<React.ComponentProps<typeof NextThemesProvider>>) {
    return (
        <NextThemesProvider disableTransitionOnChange {...props}>
            <ThemePersistenceEnforcer>{children}</ThemePersistenceEnforcer>
        </NextThemesProvider>
    );
}
