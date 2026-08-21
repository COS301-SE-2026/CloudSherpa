"use client";
import * as React from "react";
import { LayoutDashboard, Network, Moon, Sun, HelpCircle, Telescope } from "lucide-react";
import { useTheme } from "next-themes";
import Link from "next/link";
import { useAuthContext } from "@/features/authentication/providers/AuthContext";
import { updateUserTheme } from "@/lib/fetch/api-preferences";
import { useLogout } from "@/features/authentication/hooks/useLogout";
import Image from "next/image";

import { Tooltip, TooltipContent, TooltipTrigger } from "@/components/atoms/tooltip";

import {
    Sidebar,
    SidebarContent,
    SidebarFooter,
    SidebarGroup,
    SidebarGroupContent,
    SidebarGroupLabel,
    SidebarHeader,
    SidebarMenu,
    SidebarMenuButton,
    SidebarMenuItem,
    SidebarTrigger,
} from "@/components/atoms/sidebar";

const navItems = [{ title: "Dashboard", icon: LayoutDashboard, url: "/dashboard" }];

export function AppSidebar({ ...props }: React.ComponentProps<typeof Sidebar>) {
    const { theme, setTheme } = useTheme();
    const authContext = useAuthContext();
    const { logout } = useLogout();

    const [mounted, setMounted] = React.useState(false);

    const handleThemeToggle = async () => {
        const newTheme = theme === "dark" ? "light" : "dark";
        setTheme(newTheme);

        if (authContext?.user) {
            try {
                await updateUserTheme(newTheme);
            } catch (error) {
                console.error("Failed to save theme preference:", error);
            }
        }
    };

    React.useEffect(() => {
        // eslint-disable-next-line react-hooks/set-state-in-effect
        setMounted(true);
    }, []);

    return (
        <Sidebar collapsible="icon" {...props}>
            <SidebarHeader
                id="navBar"
                className="h-16 flex flex-row items-center justify-between px-4 group-data-[collapsible=icon]:justify-center group-data-[collapsible=icon]:px-0"
            >
                {/*open*/}
                <div className="flex items-center gap-3 group-data-[collapsible=icon]:hidden">
                    <Image src="/CloudSherpaFavicon.svg" alt="CS" width={30} height={30} priority />
                    <span className="font-bold text-lg">CloudSherpa</span>
                </div>

                {/*closed*/}
                <div className="hidden group-data-[collapsible=icon]:flex relative h-8 w-8 items-center justify-center group/logo">
                    <div className="transition-opacity duration-200 group-hover/logo:opacity-0 flex items-center justify-center">
                        <Image
                            src="/CloudSherpaFavicon.svg"
                            alt="CS"
                            width={30}
                            height={30}
                            priority
                        />
                    </div>
                    <div className="absolute inset-0 flex items-center justify-center opacity-0 transition-opacity duration-200 group-hover/logo:opacity-100 z-10 cursor-pointer">
                        <Tooltip>
                            <TooltipTrigger asChild>
                                <div>
                                    <SidebarTrigger />
                                </div>
                            </TooltipTrigger>
                            <TooltipContent side="right">
                                <p>Open</p>
                            </TooltipContent>
                        </Tooltip>
                    </div>
                </div>
                <div className="group-data-[collapsible=icon]:hidden">
                    <SidebarTrigger />
                </div>
            </SidebarHeader>

            <SidebarContent>
                <SidebarGroup>
                    <SidebarGroupLabel>Analytics</SidebarGroupLabel>
                    <SidebarGroupContent>
                        <SidebarMenu>
                            {navItems.map((item) => (
                                <SidebarMenuItem key={item.title}>
                                    <SidebarMenuButton asChild tooltip={item.title}>
                                        <Link href={item.url}>
                                            <item.icon />
                                            <span>{item.title}</span>
                                        </Link>
                                    </SidebarMenuButton>
                                </SidebarMenuItem>
                            ))}
                        </SidebarMenu>
                    </SidebarGroupContent>
                </SidebarGroup>
                <SidebarGroup>
                    <SidebarGroupLabel>Add Connection</SidebarGroupLabel>
                    <SidebarGroupContent>
                        <SidebarMenu>
                            <SidebarMenuItem>
                                <SidebarMenuButton asChild tooltip="Add AWS connection">
                                    <Link href="/addConnection/aws">
                                        <Network />
                                        <span>AWS</span>
                                    </Link>
                                </SidebarMenuButton>
                            </SidebarMenuItem>
                            <SidebarMenuItem>
                                <SidebarMenuButton asChild tooltip="Google Cloud Platform">
                                    <Link href="/addConnection/gcp">
                                        <Network />
                                        <span>Google Cloud Platform</span>
                                    </Link>
                                </SidebarMenuButton>
                            </SidebarMenuItem>
                            {/* <SidebarMenuItem>
                                <SidebarMenuButton asChild tooltip="Google Cloud Platform">
                                    <Link href="/addConnection/gcp">
                                        <Network />
                                        <span>Google Cloud Platform</span>
                                    </Link>
                                </SidebarMenuButton>
                            </SidebarMenuItem>

                            <SidebarMenuItem>
                                <SidebarMenuButton asChild tooltip="Azure">
                                    <Link href="/addConnection/azure">
                                        <Network />
                                        <span>Azure</span>
                                    </Link>
                                </SidebarMenuButton>
                            </SidebarMenuItem> */}
                        </SidebarMenu>
                    </SidebarGroupContent>
                </SidebarGroup>
                <SidebarGroup>
                    <SidebarGroupLabel>Manage Connections</SidebarGroupLabel>
                    <SidebarGroupContent>
                        <SidebarMenu>
                            <SidebarMenuItem>
                                <SidebarMenuButton asChild tooltip="Manage Connections">
                                    <Link href="/manageConnections">
                                        <Network />
                                        <span>Connections</span>
                                    </Link>
                                </SidebarMenuButton>
                            </SidebarMenuItem>
                        </SidebarMenu>
                    </SidebarGroupContent>
                </SidebarGroup>

                <SidebarGroup>
                    <SidebarGroupLabel>Intelligence</SidebarGroupLabel>
                    <SidebarGroupContent>
                        <SidebarMenu>
                            <SidebarMenuItem>
                                <SidebarMenuButton asChild tooltip="Usage Forecasts">
                                    <Link href="/intelligence/usage">
                                        <Telescope />
                                        <span>Usage Forecasts</span>
                                    </Link>
                                </SidebarMenuButton>
                            </SidebarMenuItem>
                            <SidebarMenuItem>
                                <SidebarMenuButton asChild tooltip="Billing Forecasts">
                                    <Link href="/intelligence/billing">
                                        <Telescope />
                                        <span>Billing Forecasts</span>
                                    </Link>
                                </SidebarMenuButton>
                            </SidebarMenuItem>
                        </SidebarMenu>
                    </SidebarGroupContent>
                </SidebarGroup>

                <SidebarGroup>
                    <SidebarGroupLabel>Optimization</SidebarGroupLabel>
                    <SidebarGroupContent>
                        <SidebarMenu>
                            <SidebarMenuItem>
                                <SidebarMenuButton asChild tooltip="AWS">
                                    <Link href="/recommendations">
                                        <Network />
                                        <span>Recommendations</span>
                                    </Link>
                                </SidebarMenuButton>
                            </SidebarMenuItem>
                        </SidebarMenu>
                    </SidebarGroupContent>
                </SidebarGroup>

                <SidebarGroup>
                    <SidebarGroupLabel> Support </SidebarGroupLabel>

                    <SidebarGroupContent>
                        <SidebarMenu>
                            <SidebarMenuItem>
                                <SidebarMenuButton asChild tooltip="Help Center">
                                    <Link href="/helpMenu/documentsAndTutorials">
                                        {" "}
                                        <HelpCircle />
                                        <span> Help Center </span>
                                    </Link>
                                </SidebarMenuButton>
                            </SidebarMenuItem>
                        </SidebarMenu>
                    </SidebarGroupContent>
                </SidebarGroup>
            </SidebarContent>

            <SidebarFooter>
                <SidebarMenu>
                    <SidebarMenuItem>
                        <div className="px-2 py-1.5 text-xs text-muted-foreground group-data-[collapsible=icon]:hidden">
                            {authContext?.isAuthReady
                                ? authContext.user?.email || "Not logged in"
                                : "Loading..."}
                        </div>
                    </SidebarMenuItem>

                    {authContext.isAuthReady && (
                        <SidebarMenuItem>
                            <SidebarMenuButton
                                onClick={() => logout()}
                                tooltip="Logout"
                                aria-label="logout button"
                            >
                                <span>Logout</span>
                            </SidebarMenuButton>
                        </SidebarMenuItem>
                    )}
                    {mounted && (
                        <SidebarMenuItem>
                            <SidebarMenuButton onClick={handleThemeToggle} tooltip="Toggle Theme">
                                {theme === "dark" ? (
                                    <>
                                        <Sun />
                                        <span>Light Mode</span>
                                    </>
                                ) : (
                                    <>
                                        <Moon />
                                        <span>Dark Mode</span>
                                    </>
                                )}
                            </SidebarMenuButton>
                        </SidebarMenuItem>
                    )}
                </SidebarMenu>
            </SidebarFooter>
        </Sidebar>
    );
}
