import { Skeleton } from "@/components/atoms/skeleton";
import { Card } from "@/components/atoms/card";
import { cn } from "@/lib/utils";

interface AppSkeletonsProps {
    variant?: "card" | "button";
    className?: string;
}

export default function AppSkeleton({ variant, className }: Readonly<AppSkeletonsProps>) {
    if (variant) {
        if (variant === "card") {
            return <Skeleton className={cn(className, "rounded-xl")} />;
        }
        if (variant === "button") {
            return <Skeleton className={cn(className, "rounded-md")} />;
        }
    } else {
        return <div></div>;
    }
}
