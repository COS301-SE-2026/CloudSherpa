import { Alert, AlertDescription } from "@/components/atoms/alert";
import { AlertCircleIcon } from "lucide-react";

interface RecommendationErrorProps {
    recError: string;
}

export function RecommendationErrorAlert({ recError }: Readonly<RecommendationErrorProps>) {
    return (
        <section>
            <Alert className="bg-destructive/40 border-destructive">
                <AlertCircleIcon className="text-destructive-foreground" />
                <AlertDescription className="text-destructive-foreground">
                    {recError}
                </AlertDescription>
            </Alert>
        </section>
    );
}
