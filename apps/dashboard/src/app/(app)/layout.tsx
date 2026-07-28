import { SidebarProvider, SidebarInset } from "@/components/atoms/sidebar";
import { TooltipProvider } from "@/components/atoms/tooltip";
import { AppSidebar } from "@/components/molecules/app-sidebar";

export default function AppWrapper({ children }: Readonly<{ children: React.ReactNode }>) {
    return (
                <TooltipProvider>
                    <SidebarProvider>
                        <AppSidebar />
                        <SidebarInset className="flex flex-col flex-1 overflow-hidden">
                            <div className="flex flex-1 flex-col gap-4">{children}</div>
                        </SidebarInset>
                    </SidebarProvider>
                </TooltipProvider>
    );
}
