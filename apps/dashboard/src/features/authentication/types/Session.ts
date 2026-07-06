import { LoginRequestDto } from "./dtos/auth/LoginRequestDto";

export type User = {
    userId: string;
    email: string;
    username: string;
};

export type SessionState = {
    user: User | null;
    isAuthReady: boolean;
    isAuthenticated: boolean;
    // Maybe some standardized login response could work better?
    login: (loginPayload: LoginRequestDto) => Promise<boolean>;
    logout: () => Promise<boolean>;
};
