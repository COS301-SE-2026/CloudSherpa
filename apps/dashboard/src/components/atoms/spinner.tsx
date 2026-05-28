import { LoaderCircle } from "lucide-react";

import { cn } from "@/lib/utils";

function Spinner({ className, ...props }: React.ComponentProps<typeof LoaderCircle>) {
  return (
    <LoaderCircle
      data-slot="spinner"
      className={cn("animate-spin text-muted-foreground", className)}
      {...props}
    />
  );
}

export { Spinner };
