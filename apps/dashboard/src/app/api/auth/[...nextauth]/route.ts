import NextAuth, { NextAuthOptions } from "next-auth"
import Credentials from "next-auth/providers/credentials"
import { LoginResponseDto } from "@/features/authentication/types/dtos/auth/LoginResponseDto"

export const authOptions: NextAuthOptions = {
    session: {
        strategy: "jwt",
    },

    providers: [
        Credentials({
            name: "Credentials",

            credentials: {
                email: { label: "Email", type: "email" },
                password: { label: "Password", type: "password" },
            },

            async authorize(credentials) {
                try {
                    const res = await fetch("http://service:8080/auth/login", {
                        method: "POST",
                        headers: {
                            "Content-Type": "application/json"
                        },
                        body: JSON.stringify({
                            email: credentials?.email,
                            password: credentials?.password
                        })
                    })
                    
                    if (!res.ok) {
                        throw new Error(`Request failed with status code ${res.status}`)
                    }

                    const result: LoginResponseDto = await res.json();

                    return ({
                        id: result.userId,
                        username: result.username,
                        email: result.email
                    });
                } catch (e) {
                    if (e instanceof Error) {
                        console.log(e);
                    }
                    return null;
                }
            }
        })
    ],

    callbacks: {
        async jwt({ token, user }) {
            if (user) {
                token.id = user.id;
                token.username = user.username;
                token.email = user.email;
            }

            return token;
        },
        async session({ session, token }) {
            session.user.id = token.id;
            session.user.username = token.username;
            session.user.email = token.email;
            return session;
        }
    },

    pages: {
        signIn: "/login"
    }
}

const handler = NextAuth(authOptions)

export { handler as GET, handler as POST }