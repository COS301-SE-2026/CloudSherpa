"use client"

import apiClient from "@/lib/fetch/api-client";
import { LoginRequestDto } from "@/features/authentication/types/dtos/auth/LoginRequestDto";
import { LoginResponseDto } from "@/features/authentication/types/dtos/auth/LoginResponseDto";
import { useState } from "react";
import { useRouter } from "next/navigation";
export function useLogin() {
    const [loginFailure, setLoginFailure] = useState(false);

    const router = useRouter();

    async function login(loginPayload: LoginRequestDto) {
        try {
            // Later on use response to store user state
            const response: LoginResponseDto | null = await apiClient('/auth/login', {
                method: "POST",
                body: JSON.stringify(loginPayload)
            })

            router.push('/dashboard');
        } catch (error) {
            if (!(error instanceof Error)) {
                console.error("Unknown error has occured");
            } 

            setLoginFailure(true);
        }
    }

    return { login, loginFailure };
}