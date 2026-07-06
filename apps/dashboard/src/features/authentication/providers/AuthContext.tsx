"use client";

import { useState, createContext, useEffect, useContext, useMemo, useCallback } from "react";
import { SessionState, User } from "../types/Session";
import { LoginRequestDto } from "../types/dtos/auth/LoginRequestDto";
import { LoginResponseDto } from "../types/dtos/auth/LoginResponseDto";
import apiClient from "@/lib/fetch/api-client";

type AuthProps = {
    readonly children: React.ReactNode;
};

const AuthContext = createContext<SessionState | null>(null);

export function AuthProvider({ children }: AuthProps) {
    const [isAuthReady, setIsAuthReady] = useState(false);
    const [user, setUser] = useState<User | null>(null);

    useEffect(() => {
        async function loadAuthState() {
            // auth/me here, whatever needs to run on each protected page render goes here
            // if not redirect here? Maybe it would be more wise to do this in the proxy
            setIsAuthReady(true);
        }

        loadAuthState();
    }, []);

    const logout = useCallback(async (): Promise<boolean> => {
        // Want to attempt logout, attempt success => succesful logout
        // What to do on attempt failure? problably still clear session state ig => inconsistency client server, but
        // stateless

        let logoutSuccess;

        try {
            await apiClient("/auth/logout", {
                method: "POST",
            });
            logoutSuccess = true;
        } catch {
            logoutSuccess = false;
        } finally {
            setUser(null);
        }

        return logoutSuccess;
    }, []);

    const login = useCallback(async (loginPayload: LoginRequestDto): Promise<boolean> => {
        try {
            const response: LoginResponseDto = await apiClient("/auth/login", {
                method: "POST",
                body: JSON.stringify(loginPayload),
            });

            setUser({
                userId: response.userId,
                username: response.username,
                email: response.email,
            });

            return true;
        } catch (error) {
            if (!(error instanceof Error)) {
                console.error("Unknown error has occured");
            }

            return false;
        }
    }, []);

    const authContextValue = useMemo<SessionState>(
        () => ({
            isAuthReady: isAuthReady,
            isAuthenticated: user !== null,
            user: user,
            login: login,
            logout: logout,
        }),
        [isAuthReady, user, login, logout]
    );

    return <AuthContext value={authContextValue}>{children}</AuthContext>;
}

export function useAuthContext() {
    const context = useContext(AuthContext);

    if (!context) {
        throw new Error("Use AuthContext within child components wrapped with AuthProvider");
    }

    return context;
}
