"use client";

import * as React from "react";
import { ThemeProvider as NextThemesProvider, useTheme } from "next-themes";
import { usePathname } from "next/navigation";
import { fetchUserTheme } from "@/lib/fetch/api-preferences";
import { useAuthContext } from "@/features/authentication/providers/AuthContext";

function ThemePersistenceEnforcer({ children }: Readonly<{ children: React.ReactNode }>) {
    const { setTheme } = useTheme();
    const { isAuthReady, isAuthenticated } = useAuthContext();

    React.useEffect(() => {
        if (!isAuthReady || !isAuthenticated) return;

        fetchUserTheme()
            .then((data) => {
                if (data?.theme === "light" || data?.theme === "dark") {
                    setTheme(data.theme);
                }
            })
            .catch((error) => console.error("Failed to fetch user theme:", error));
    }, [isAuthReady, isAuthenticated, setTheme]);

    return <>{children}</>;
}

export function ThemeProvider({
    children,
    ...props
}: Readonly<React.ComponentProps<typeof NextThemesProvider>>) {
    const pathname = usePathname();

    const forceDarkMode = pathname === "/" || pathname === "/login";

    return (
        <NextThemesProvider
            disableTransitionOnChange
            forcedTheme={forceDarkMode ? "dark" : undefined}
            {...props}
        >
            <ThemePersistenceEnforcer>{children}</ThemePersistenceEnforcer>
        </NextThemesProvider>
    );
}
