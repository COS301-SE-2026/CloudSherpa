"use client";

import { useState } from "react";
import { useAuthContext } from "../providers/AuthContext";
import { useRouter } from "next/navigation";
import { useDashboardStore } from "@/features/dashboard/stores/dashboard-store";

export function useLogout() {
    const authContext = useAuthContext();
    const router = useRouter();
    const resetStore = useDashboardStore((state) => state.actions.reset);

    const [logoutError, setLogoutError] = useState(false);

    async function logout() {
        const logoutStatus = await authContext.logout();
        setLogoutError(!logoutStatus);
        router.push("/login");
        resetStore();
    }

    return { logout, logoutError };
}
