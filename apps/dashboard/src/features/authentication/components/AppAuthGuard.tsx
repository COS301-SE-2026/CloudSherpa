"use client";

import { useEffect } from "react";
import { usePathname, useRouter } from "next/navigation";
import { Spinner } from "@/components/atoms/spinner";
import { useAuthContext } from "@/features/authentication/providers/AuthContext";

type AppAuthGuardProps = {
    readonly children: React.ReactNode;
};

export function AppAuthGuard({ children }: AppAuthGuardProps) {
    const { isAuthReady, isAuthenticated } = useAuthContext();
    const router = useRouter();
    const pathname = usePathname();

    useEffect(() => {
        if (!isAuthReady) return;

        if (!isAuthenticated) {
            const next = pathname ? `?next=${encodeURIComponent(pathname)}` : "";

            router.replace(`/login${next}`);
        }
    }, [isAuthReady, isAuthenticated, pathname, router]);

    if (!isAuthReady) {
        return (
            <div className="flex min-h-screen items-center justify-center">
                <Spinner className="h-8 w-8" />
            </div>
        );
    }

    if (!isAuthenticated) {
        return null;
    }

    return <>{children}</>;
}
