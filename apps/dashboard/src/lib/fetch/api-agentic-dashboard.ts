import apiClient from "@/lib/fetch/api-client";
import type {
    AiDashboardApplyRequest,
    AiSessionResponse,
    AiDashboardPlanRequest,
    AiDashboardPlanResponse,
    AiVersionSummary,
    AiVersionResponse,
} from "@/features/dashboard/types/agentic";
import type { DashboardDTO } from "@/lib/fetch/api-dashboard";

export class AiDashboardResponseError extends Error {
    constructor(message: string) {
        super(message);
        this.name = "AiDashboardResponseError";
    }
}

export class AiDashboardNoChangeError extends Error {
    constructor(message: string) {
        super(message);
        this.name = "AiDashboardNoChangeError";
    }
}

function validateDashboardPlanResponse(response: AiDashboardPlanResponse): AiDashboardPlanResponse {
    //check undefined so it doesn't behave unexpectedly
    if (
        typeof response.stageAttempted !== "boolean" ||
        typeof response.stageSucceeded !== "boolean"
    ) {
        console.error("Malformed AI dashboard response:", response);

        throw new AiDashboardResponseError(
            "The dashboard generation service returned an invalid response. Please try again."
        );
    }

    if (typeof response.assistantMessage !== "string" && response.assistantMessage !== null) {
        console.error("Invalid assistantMessage in AI dashboard response:", response);

        throw new AiDashboardResponseError(
            "The dashboard generation service returned an invalid response. Please try again."
        );
    }

    // if stagingattempted is false but it also says stage succeeded is true
    if (response.stageSucceeded && !response.stageAttempted) {
        console.error("Inconsistent AI dashboard response:", response);

        throw new AiDashboardResponseError(
            "The dashboard generation service returned an inconsistent response. Please try again."
        );
    }

    // completed without trying to stage dash
    if (!response.stageAttempted) {
        console.warn("AI completed without staging a dashboard:", response.assistantMessage);

        throw new AiDashboardNoChangeError(
            "The AI did not create a dashboard version. Please try rephrasing your request."
        );
    }

    return response;
}

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
    try {
        const response = await apiClient<AiDashboardPlanResponse>("/ai/dashboard/plan", {
            method: "POST",
            body: JSON.stringify(payload),
        });

        return validateDashboardPlanResponse(response);
    } catch (error) {
        if (
            error instanceof AiDashboardResponseError ||
            error instanceof AiDashboardNoChangeError
        ) {
            throw error;
        }
        console.error("Failed to generate AI dashboard plan:", error);

        throw error;
    }
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

//make a dashboard version the active AI context and parent for the next generated version.
export async function activateAiDashboardVersion(
    sessionId: string,
    versionId: string
): Promise<AiVersionResponse> {
    return await apiClient<AiVersionResponse>(
        `/ai/sessions/${sessionId}/versions/${versionId}/active`,
        {
            method: "POST",
        }
    );
}

//apply a dashboard version and close the AI session.
export async function applyAiDashboardVersion(
    sessionId: string,
    versionId: string,
    request: AiDashboardApplyRequest
): Promise<DashboardDTO[]> {
    return await apiClient<DashboardDTO[]>(
        `/ai/sessions/${sessionId}/versions/${versionId}/apply`,
        {
            method: "POST",
            body: JSON.stringify(request),
        }
    );
}
