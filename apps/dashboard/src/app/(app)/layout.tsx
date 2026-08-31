import { SidebarProvider, SidebarInset, SidebarTrigger } from "@/components/atoms/sidebar";
import { TooltipProvider } from "@/components/atoms/tooltip";
import { AppSidebar } from "@/components/molecules/app-sidebar";
import Image from "next/image";

export default function AppWrapper({ children }: Readonly<{ children: React.ReactNode }>) {
    return (
        <TooltipProvider>
            <SidebarProvider>
                <AppSidebar />
                <SidebarInset className="flex flex-col flex-1 overflow-hidden">
                    <header className="flex h-16 shrink-0 items-center gap-3 border-b border-border px-4 md:hidden">
                        <SidebarTrigger className="-ml-1" />
                        <div className="flex items-center gap-2">
                            <Image
                                src="/CloudSherpaFavicon.svg"
                                alt="CloudSherpa Logo"
                                width={24}
                                height={24}
                                priority
                            />
                            <span className="font-bold text-md">CloudSherpa</span>
                        </div>
                    </header>
                    <div className="flex flex-1 flex-col gap-4">{children}</div>
                </SidebarInset>
            </SidebarProvider>
        </TooltipProvider>
    );
}
