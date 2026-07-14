"use client";

import { LoginRequestDto } from "@/features/authentication/types/dtos/auth/LoginRequestDto";
import { useState } from "react";
import { useRouter } from "next/navigation";
import { useAuthContext } from "../providers/AuthContext";
export function useLogin() {
    const [loginFailure, setLoginFailure] = useState(false);
    const authContext = useAuthContext();
    const router = useRouter();

    async function login(loginPayload: LoginRequestDto, redirect?: boolean) {
        console.log(authContext);
        const loginResult = await authContext?.login(loginPayload);

        if (loginResult) {
            setLoginFailure(false);
            if (redirect) {
                router.push("/dashboard");
            }
        } else {
            setLoginFailure(true);
        }
    }

    return { login, loginFailure };
}
