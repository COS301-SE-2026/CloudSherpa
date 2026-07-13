"use client"

import apiClient from "@/lib/fetch/api-client"
import { RegisterRequestDto } from "@/features/authentication/types/dtos/auth/RegisterRequestDto";
import { useState } from "react";
import { useRouter } from "next/navigation";
import { useLogin } from "./useLogin";

export function useRegistration() {

    const [registrationFailure, setRegistrationFailure] = useState(false);
    const [registrationSuccess, setRegistrationSuccess] = useState(false);
    // In seconds
    const [redirectCountdown, setRedirectCountdown] = useState(3);

    const router = useRouter();
    const { login, loginFailure } = useLogin();

    async function register(registerPayload: RegisterRequestDto) {
        try {
            await apiClient("/auth/register", {
                method: "POST",
                body: JSON.stringify(registerPayload)
            })

            await login({
                email: registerPayload.email,
                password: registerPayload.password
            }, false);

            if (loginFailure) {
                throw new Error("Login after registration failed");
            }

            setRegistrationFailure(false);
            setRegistrationSuccess(true);

            const countDownId = setInterval(() => {
                setRedirectCountdown((countdown) => countdown - 1);
            }, 1000);

            setTimeout(async () => {
                clearInterval(countDownId);
                router.push("/dashboard");
            }, redirectCountdown * 1000)

        } catch (error) {
            if (error instanceof Error) {
                console.warn(`Registration failed: ${error.message}`);
            }
            if (!(error instanceof Error)) {
                console.error("Unknown error has occured")
            }

            setRegistrationFailure(true);
            setRegistrationSuccess(true);
        }
    }

    return { register, registrationFailure, registrationSuccess, redirectCountdown };
}