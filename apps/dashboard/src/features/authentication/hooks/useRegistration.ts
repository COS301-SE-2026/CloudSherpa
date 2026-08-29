"use client";

import apiClient from "@/lib/fetch/api-client";
import { RegisterRequestDto } from "@/features/authentication/types/dtos/auth/RegisterRequestDto";
import { useState } from "react";
import { useRouter } from "next/navigation";
import { useLogin } from "./useLogin";
import { toast } from "sonner";

export function useRegistration() {
    const [registrationFailure, setRegistrationFailure] = useState(false);
    const [registrationSuccess, setRegistrationSuccess] = useState(false);

    const router = useRouter();
    const { login, loginFailure } = useLogin();

    async function register(registerPayload: RegisterRequestDto) {
        try {
            await apiClient("/auth/register", {
                method: "POST",
                body: JSON.stringify(registerPayload),
            });

            await login(
                {
                    email: registerPayload.email,
                    password: registerPayload.password,
                },
                false
            );

            if (loginFailure) {
                throw new Error("Login after registration failed");
            }

            setRegistrationFailure(false);
            setRegistrationSuccess(true);

            setTimeout(() => {
                toast.success("Account successfully created!");
            }, 1000);
            router.push("/dashboard");
        } catch (error) {
            if (error instanceof Error) {
                console.warn(`Registration failed: ${error.message}`);
            }
            if (!(error instanceof Error)) {
                console.error("Unknown error has occured");
            }

            setRegistrationFailure(true);
            setRegistrationSuccess(false);
        }
    }

    return { register, registrationFailure, registrationSuccess };
}
