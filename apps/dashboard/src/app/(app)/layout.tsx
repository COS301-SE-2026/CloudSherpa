import { SidebarProvider, SidebarInset } from "@/components/atoms/sidebar";
import { TooltipProvider } from "@/components/atoms/tooltip";
import { AppSidebar } from "@/features/app/components/appSidebar";

export default function DashboardShell({ children }: Readonly<{ children: React.ReactNode }>) {
  return (
    <TooltipProvider>
      <SidebarProvider>
        <div className="flex min-h-screen w-full bg-background">
          <AppSidebar />
          <SidebarInset className="flex flex-col flex-1 overflow-hidden">
             {children}
          </SidebarInset>
        </div>
      </SidebarProvider>
    </TooltipProvider>
  );
}