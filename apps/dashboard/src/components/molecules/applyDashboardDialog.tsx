import {
    Dialog,
    DialogContent,
    DialogDescription,
    DialogFooter,
    DialogHeader,
    DialogTitle,
} from "@/components/atoms/dialog";
import { Button } from "@/components/atoms/button";
import { Loader2 } from "lucide-react";
import type { AiDashboardApplyMode } from "@/features/dashboard/types/agentic";

interface ApplyDashboardDialogProps {
    open: boolean;
    dashboardName: string;
    isApplying: boolean;
    onOpenChange: (open: boolean) => void;
    onApply: (mode: AiDashboardApplyMode) => Promise<void>;
}

export default function ApplyDashboardDialog({
    open,
    dashboardName,
    isApplying,
    onOpenChange,
    onApply,
}: Readonly<ApplyDashboardDialogProps>) {
    return (
        <Dialog open={open} onOpenChange={onOpenChange}>
            <DialogContent>
                <DialogHeader>
                    <DialogTitle>Apply this dashboard?</DialogTitle>
                    <DialogDescription>
                        Choose how <strong>{dashboardName}</strong> should be added to your
                        dashboards.
                    </DialogDescription>
                </DialogHeader>

                <div className="grid gap-3">
                    <Button
                        variant="default"
                        disabled={isApplying}
                        onClick={() => onApply("REPLACE_STARTED_DASHBOARD")}
                        className="h-auto flex-col items-start gap-1 px-4 py-3 text-left"
                    >
                        <span>Replace the dashboard I started with</span>
                        <span className="text-xs font-normal opacity-80">
                            Keep the original dashboard and replace its widgets and settings with
                            this version.
                        </span>
                    </Button>

                    <Button
                        variant="outline"
                        disabled={isApplying}
                        onClick={() => onApply("CREATE_NEW_DASHBOARD")}
                        className="h-auto flex-col items-start gap-1 px-4 py-3 text-left"
                    >
                        <span>Create a new dashboard</span>
                        <span className="text-xs font-normal opacity-80">
                            Keep the dashboard you started with and create this version as a
                            separate dashboard.
                        </span>
                    </Button>
                </div>

                <DialogFooter>
                    <Button
                        variant="ghost"
                        disabled={isApplying}
                        onClick={() => onOpenChange(false)}
                    >
                        Cancel
                    </Button>
                    {isApplying && <Loader2 className="h-4 w-4 animate-spin self-center" />}
                </DialogFooter>
            </DialogContent>
        </Dialog>
    );
}
