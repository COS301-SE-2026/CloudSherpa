"use client";

import { useEffect } from "react";
import { useTheme } from "next-themes";
import { useAuth } from "@/features/authentication/providers/AuthContext";
import { fetchUserTheme } from "@/lib/fetch/api-preferences"

export function Preferences() {
  const { setTheme } = useTheme();
  const { authState } = useAuth();

  useEffect(() => {
    if (authState.isAuthenticated) {
      const loadTheme = async () => {
        try {
          const dbThemeData = await fetchUserTheme();
          
          // Access the .theme property from the JSON object
          if (dbThemeData.theme === 'light' || dbThemeData.theme === 'dark') {
            setTheme(dbThemeData.theme);
          }
        } catch (error) {
          console.error("Failed to sync theme from database", error);
        }
      };

      loadTheme();
    }
  }, [authState.isAuthenticated, setTheme]);

  return null; 
}