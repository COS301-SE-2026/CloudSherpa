import { Alert, AlertDescription } from "@/components/atoms/alert";
import { AlertCircleIcon } from "lucide-react";
import { UsageError } from "../../types/errors";

interface UsageErrorProps {
    readonly usageError: UsageError;
}

export function UsageErrorAlert({ usageError }: UsageErrorProps) {
    if (usageError.item === "forecast" || usageError.item == "usage") {
        return (
            <section>
                <Alert className="bg-warning/40 border-warning">
                    <AlertCircleIcon className="text-warning-foreground" />
                    <AlertDescription className="text-warning-foreground">
                        {usageError.errorMessage}
                    </AlertDescription>
                </Alert>
            </section>
        );
    }

    return (
        <section>
            <Alert className="bg-destructive/40 border-destructive">
                <AlertCircleIcon className="text-destructive-foreground" />
                <AlertDescription className="text-destructive-foreground">
                    {usageError.errorMessage}
                </AlertDescription>
            </Alert>
        </section>
    );
}
