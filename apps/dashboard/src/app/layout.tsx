import type { Metadata } from "next";
import { Geist, Geist_Mono } from "next/font/google";
import { ThemeProvider } from "@/components/atoms/themeProvider";
import "./globals.css";
import { AuthProvider } from "@/features/authentication/providers/AuthContext";
import { cn } from "@/lib/utils";
import { Toaster } from "@/components/atoms/sonner";

const geistSans = Geist({
    subsets: ["latin"],
    variable: "--font-sans",
});

const geistHeading = Geist({
    subsets: ["latin"],
    variable: "--font-heading",
});

const geistMono = Geist_Mono({
    subsets: ["latin"],
    variable: "--font-mono",
});

export const metadata: Metadata = {
    title: "CloudSherpa",
    description: "Ai Cloud Analytics and Finops Platform",
};

export default function RootLayout({ children }: Readonly<{ children: React.ReactNode }>) {
    return (
        <html
            lang="en"
            suppressHydrationWarning
            className={cn(
                geistSans.variable,
                geistHeading.variable,
                geistMono.variable,
                "font-sans"
            )}
        >
            <body className="min-h-screen overflow-x-hidden">
                <AuthProvider>
                    <ThemeProvider attribute="class" defaultTheme="dark" enableSystem={false}>
                        {children}
                        <Toaster />
                    </ThemeProvider>
                </AuthProvider>
            </body>
        </html>
    );
}
