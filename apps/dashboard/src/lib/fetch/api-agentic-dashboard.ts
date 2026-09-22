import apiClient from "@/lib/fetch/api-client";
import {
    AiSessionResponse,
    AiDashboardPlanRequest,
    AiDashboardPlanResponse,
    AiVersionSummary,
    AiVersionResponse,
} from "@/features/dashboard/types/agentic";

//create new session.
export async function createAiSession(): Promise<AiSessionResponse> {
    return await apiClient<AiSessionResponse>("/ai/sessions", {
        method: "POST",
    });
}

//delete all session related data.
export async function deleteAiSession(sessionId: string): Promise<void> {
    await apiClient<void>(`/ai/sessions/${sessionId}`, {
        method: "DELETE",
    });
}

//send prompt to create 'staged' dashboard
export async function generateDashboardPlan(
    payload: AiDashboardPlanRequest
): Promise<AiDashboardPlanResponse> {
    return await apiClient<AiDashboardPlanResponse>("/ai/dashboard/plan", {
        method: "POST",
        body: JSON.stringify(payload),
    });
}

//return all dashboards versions
export async function getAiDashboardVersions(sessionId: string): Promise<AiVersionSummary[]> {
    return await apiClient<AiVersionSummary[]>(`/ai/sessions/${sessionId}/versions`, {
        method: "GET",
    });
}

//return specific dashboard version.
export async function getAiDashboardVersion(
    sessionId: string,
    versionId: string
): Promise<AiVersionResponse> {
    return await apiClient<AiVersionResponse>(`/ai/sessions/${sessionId}/versions/${versionId}`, {
        method: "GET",
    });
}

//add ai dash to actual list of dashboards.
export async function applyAiDashboardVersion(sessionId: string, versionId: string): Promise<void> {
    await apiClient<void>(`/ai/sessions/${sessionId}/versions/${versionId}/apply`, {
        method: "POST",
    });
}
