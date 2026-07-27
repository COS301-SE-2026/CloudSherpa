"use client";

import { useState } from "react";
import { useAuthContext } from "../providers/AuthContext";
import { useRouter } from "next/navigation";
import { useDashboardStore } from "@/features/dashboard/stores/dashboard-store";
import { useWindowStore } from "@/features/dashboard/stores/window-store";
import { useResourceNameStore } from "@/features/dashboard/stores/resource-store";

export function useLogout() {
    const authContext = useAuthContext();
    const router = useRouter();
    const resetDashboardStore = useDashboardStore((state) => state.actions.reset);
    const resetWindowStore = useWindowStore((state) => state.clear);
    const resetResourceStore = useResourceNameStore((state) => state.reset);
    const [logoutError, setLogoutError] = useState(false);

    function clearStores() {
        resetDashboardStore();
        resetWindowStore();
        resetResourceStore();
    }

    async function logout() {
        const logoutStatus = await authContext.logout();
        setLogoutError(!logoutStatus);
        router.push("/login");
        clearStores();
    }

    return { logout, logoutError };
}
