"use client";

import { useState } from "react";
import { useAuthContext } from "../providers/AuthContext";
import { useRouter } from "next/navigation";

export function useLogout() {
    const authContext = useAuthContext();
    const router = useRouter();

    const [logoutError, setLogoutError] = useState(false);

    async function logout() {
        const logoutStatus = await authContext.logout();
        setLogoutError(!logoutStatus);
        router.push("/login");
    }

    return { logout, logoutError };
}
