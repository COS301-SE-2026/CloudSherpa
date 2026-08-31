"use client";

import { LoginRequestDto } from "@/features/authentication/types/dtos/auth/LoginRequestDto";
import { useState } from "react";
import { useRouter } from "next/navigation";
import { useAuthContext } from "../providers/AuthContext";

export function useLogin() {
    const [loginFailure, setLoginFailure] = useState(false);

    const [loginSuccess, setLoginSuccess] = useState(false);
    const [isLoading, setIsLoading] = useState(false);

    const authContext = useAuthContext();
    const router = useRouter();

    async function login(loginPayload: LoginRequestDto, redirect?: boolean) {
        setIsLoading(true);
        setLoginFailure(false);
        try {
            const loginResult = await authContext?.login(loginPayload);

            if (loginResult) {
                setLoginFailure(false);

                setLoginSuccess(true);

                if (redirect) {
                    router.push("/dashboard?new_login=true");
                }
            } else {
                setLoginFailure(true);
                setLoginSuccess(false);
                setIsLoading(false);
            }
        } catch (error) {
            if (error instanceof Error) {
                console.warn(`Login failed: ${error.message}`);
            }

            if (!(error instanceof Error)) {
                console.error("Unknown error has occurred");
            }

            setLoginFailure(true);
            setLoginSuccess(false);
        }
    }

    return { login, loginFailure, loginSuccess, isLoading };
}
