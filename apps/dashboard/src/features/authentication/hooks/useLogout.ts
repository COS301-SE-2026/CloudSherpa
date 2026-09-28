"use client";

import { useState } from "react";
import { useAuthContext } from "../providers/AuthContext";
import { useDashboardStore } from "@/features/dashboard/stores/dashboard-store";
import { useResourceNameStore } from "@/features/dashboard/stores/resource-store";
import { useUsageIntelligenceConfigStore } from "@/features/intelligence/stores/useUsageIntelligenceConfigStore";
import { useTheme } from "next-themes";

export function useLogout() {
    const { setTheme } = useTheme();
    const authContext = useAuthContext();
    const resetDashboardStore = useDashboardStore((state) => state.actions.reset);
    const resetWindowStore = useDashboardStore((state) => state.clear);
    const resetResourceStore = useResourceNameStore((state) => state.reset);
    const resetUsageIntelligenceConfigStore = useUsageIntelligenceConfigStore(
        (state) => state.reset
    );
    const [logoutError, setLogoutError] = useState(false);
    const isSessionActive = useDashboardStore((state) => state.isSessionActive);
    const { cancelSession } = useDashboardStore((state) => state.agenticActions);

    function clearStores() {
        resetDashboardStore();
        resetWindowStore();
        resetResourceStore();
        resetUsageIntelligenceConfigStore();
        if (isSessionActive) {
            cancelSession();
        }
    }

    async function logout() {
        const logoutStatus = await authContext.logout();
        setLogoutError(!logoutStatus);
        setTheme("dark");
        clearStores();
    }

    return { logout, logoutError };
}
