const API_BASE = process.env["NEXT_PUBLIC_API_URL"];

let refreshPromise: Promise<boolean> | null = null;
let isLoggingOut = false;

export function startLogout() {
    isLoggingOut = true;
}

export function finishLogin() {
    isLoggingOut = false;
}

async function refreshSession(): Promise<boolean> {
    if (!API_BASE) return false;

    // satisfy sonar warning: ??= will only execute the fetch if refreshPromise is currently null
    refreshPromise ??= fetch(`${API_BASE}/auth/refresh`, {
        method: "POST",
        credentials: "include",
    })
        .then((response) => response.ok)
        .catch(() => false)
        .finally(() => {
            refreshPromise = null;
        });

    return refreshPromise;
}

/* params:
 *   - path
 *       expects initial slash, i.e. for path /api/some-endpoint is valid,
 *       api/some-endpoint not valid
 *   - options
 *       of type RequestInit, object with fields: method, headers and body
 *   - throws
 *       callers need to handle exception, this is intentional behavior, lets caller
 *       decide how to handle failed request
 */
export default async function apiClient<T>(
    path: string,
    options?: RequestInit,
    refreshOnUnauthorized = true
): Promise<T> {
    if (!API_BASE) {
        throw new Error("NEXT_PUBLIC_API_URL is not configured");
    }

    const normalizedPath = path.startsWith("/") ? path : `/${path}`;

    options = {
        ...options,
        credentials: "include",
        headers: {
            ...options?.headers,
            "Content-Type": "application/json",
        },
    };

    // Attempt the initial API request with the user's current session/token.
    let response = await fetch(`${API_BASE}${normalizedPath}`, options);

    // Determine if we should attempt to refresh the user's session.
    const canRefresh =
        !isLoggingOut &&
        response.status === 401 &&
        normalizedPath !== "/auth/refresh" &&
        normalizedPath !== "/auth/login";

    if (refreshOnUnauthorized && canRefresh && (await refreshSession())) {
        // If refreshSession() returns true (the token was successfully renewed),
        // we retry the exact same API request as previously
        response = await fetch(`${API_BASE}${normalizedPath}`, options);
    }

    if (response.status === 204) {
        return [] as T;
    }

    if (!response.ok) {
        throw new Error(`Request failed with status code ${response.status}`);
    }

    const text = await response.text();
    if (!text) {
        return [] as T;
    }

    const result = JSON.parse(text) as T;
    return result;
}
