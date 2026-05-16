const API_BASE = process.env["NEXT_PUBLIC_API_URL"];

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
export default async function apiClient<T>(path: string, options?: RequestInit): Promise<T> {

    if (!API_BASE) {
        throw new Error("NEXT_PUBLIC_API_URL is not configured");
    }

    const normalizedPath = path.startsWith("/") ? path : `/${path}`;


    if (options) {
        options.headers = { ...options.headers, "Content-Type": "application/json" };
    } else {
        options = {
            headers: {
                "Content-Type": "application/json"
            }
        };
    }
    const response = await fetch(`${API_BASE}${normalizedPath}`, options);

    if (!response.ok) {
        throw new Error(`Request failed with status code ${response.status}`);
    }

    const result = await response.json() as T;
    return result;
}