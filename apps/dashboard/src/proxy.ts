import { NextResponse, NextRequest } from "next/server";

export function proxy(request: NextRequest) {
    const hasSessionCookie =
        request.cookies.get("auth_token") || request.cookies.get("refresh_token");

    if (
        hasSessionCookie ||
        (process.env["NODE_ENV"] !== "production" && process.env["DISABLE_AUTH"] === "true")
    ) {
        return NextResponse.next();
    }

    // Redirect to login page if auth token not present
    return NextResponse.redirect(new URL("/login", request.url));
}

export const config = {
    matcher: [
        "/dashboard/:path*",
        "/alerts/:path*",
        "/addConnection/:path*",
        "/manageConnections/:path*",
        "/recommendations/:path*",
        "/thresholds/:path*",
        "/webhooks/:path*",
        "/intelligence/:path*",
        "/helpMenu/:path*",
        "/demo/:path*",
        "/edit/:path*",
    ],
};
