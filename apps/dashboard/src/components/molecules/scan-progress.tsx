import { Progress } from "../atoms/progress";

interface ScanProgressProps {
    progress: number;
    currentScanningService?: string | null;
}

export function ScanProgress({ progress, currentScanningService }: ScanProgressProps) {
    return (
        <div className="space-y-2 w-full pt-4">
            <div className="flex justify-between text-sm text-muted-foreground font-medium">
                <span>
                    {currentScanningService
                        ? `Scanning ${currentScanningService.toUpperCase()}...`
                        : "Preparing scan..."}
                </span>

                <span>{Math.round(progress)}%</span>
            </div>

            <Progress value={progress} className="w-full h-2" />
        </div>
    );
}
