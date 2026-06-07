"use client"

import { LoginRequestDto } from "@/features/authentication/types/dtos/auth/LoginRequestDto";
import { useState } from "react";
import { useRouter } from "next/navigation";
import { signIn } from "next-auth/react";
export function useLogin() {
    const [loginFailure, setLoginFailure] = useState(false);

    const router = useRouter();

    async function login(loginPayload: LoginRequestDto) {
        try {
            // Later on use response to store user state
            const result = await signIn("credentials", {
                email: loginPayload.email,
                password: loginPayload.password,
                redirect: false
            }) 

            if (!result?.ok) {
                throw new Error("Login failure")
            }

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