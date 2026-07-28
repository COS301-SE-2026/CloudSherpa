"use client";

import { LoginRequestDto } from "@/features/authentication/types/dtos/auth/LoginRequestDto";
import { useState } from "react";
import { useRouter } from "next/navigation";
import { useAuthContext } from "../providers/AuthContext";
export function useLogin() {
    const [loginFailure, setLoginFailure] = useState(false);

    const [loginSuccess, setLoginSuccess] = useState(false);

    const [redirectCountdown, setRedirectCountdown] = useState(0);

    const authContext = useAuthContext();
    const router = useRouter();

    async function login(loginPayload: LoginRequestDto, redirect?: boolean) {
        try {
            const loginResult = await authContext?.login(loginPayload);

            if (loginResult) {
                setLoginFailure(false);

                setLoginSuccess(true);
                setRedirectCountdown(3);

                if (redirect) {
                    const countDownId = setInterval(() => {
                        setRedirectCountdown((countdown) => countdown - 1);
                    }, 1000);

                    setTimeout(() => {
                        clearInterval(countDownId);

                        router.push("/dashboard");
                    }, 3000);
                }
            } else {
                setLoginFailure(true);

                setLoginSuccess(false);
                setRedirectCountdown(0);
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
            setRedirectCountdown(0);
        }
    }

    return { login, loginFailure, loginSuccess, redirectCountdown };
}
