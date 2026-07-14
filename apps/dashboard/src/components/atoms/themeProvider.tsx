"use client"

import * as React from "react"
import { ThemeProvider as NextThemesProvider, useTheme } from "next-themes"
import { useAuthContext } from "@/features/authentication/providers/AuthContext"
import apiClient from "@/lib/fetch/api-client"

export const fetchUserTheme = async () => {
  return apiClient<{ theme: 'light' | 'dark' }>('/preferences/theme', {
    method: 'GET',
  });
};

export const updateUserTheme = async (theme: 'light' | 'dark'): Promise<void> => {
  await apiClient<void>('/preferences/theme', {
    method: 'POST',
    body: JSON.stringify({ theme }),
  });
};

function ThemePersistenceEnforcer({ children }: { children: React.ReactNode }) {
  const { setTheme } = useTheme();
  const authContext = useAuthContext();

  React.useEffect(() => {
    // if (authContext?.isAuthReady && authContext?.user) {
      fetchUserTheme()
        .then((data) => {
          if (data?.theme === 'light' || data?.theme === 'dark') {
            setTheme(data.theme);
          }
        })
        .catch((error) => console.error("Failed to fetch user theme:", error));
    // }
  }, [setTheme]);
  return <>{children}</>;
}

export function ThemeProvider({ children, ...props }: Readonly<React.ComponentProps<typeof NextThemesProvider>>) {
  return (
    <NextThemesProvider disableTransitionOnChange {...props}>\
      <ThemePersistenceEnforcer>
      {children}
      </ThemePersistenceEnforcer>
      </NextThemesProvider>
  )
}