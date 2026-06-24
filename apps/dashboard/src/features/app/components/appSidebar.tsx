"use client";

import { LayoutDashboard, Network, Moon, Sun } from "lucide-react";
import {
  Sidebar,
  SidebarContent,
  SidebarGroup,
  SidebarGroupContent,
  SidebarGroupLabel,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
  SidebarHeader,
  SidebarFooter,
} from "@/components/atoms/sidebar";
import { useTheme } from "next-themes";
import { useSyncExternalStore } from "react";
import Link from "next/link";
import { useAuthContext } from "@/features/authentication/providers/AuthContext";
import { useLogout } from "@/features/authentication/hooks/useLogout";

const navItems = [
  { title: "Dashboard", icon: LayoutDashboard, url: "/dashboard" },
];

export function AppSidebar() {
  const { theme, setTheme } = useTheme();
  const isMounted = useSyncExternalStore(
    () => () => { },
    () => true,
    () => false
  );

  const authContext = useAuthContext();
  const { logout } = useLogout();

  if (!isMounted) return null;
  return (

    // if wnat to change how sidebar acts, change collapsible to "off-canvas" to hide completely, "icon" to just keep icon size
    <Sidebar collapsible="offcanvas" className="border-r border-border-subtle bg-card gap-4 ">
      <SidebarHeader className="h-18 flex items-start justify-end">
        <div className="flex items-center gap-2 group-data-[collapsible=offcanvas]:hidden">
          {/* <NextImage 
            src="/CloudSherpaLogo.svg" 
            alt="CS" 
            width={30} 
            height={30} 
            priority
          /> */}
        </div>

        <div className="hidden group-data-[collapsible=offcanvas]:flex items-center justify-center w-full">
          {/* <NextImage 
            src="/CloudSherpaFavoffcanvas.svg" 
            alt="CS" 
            width={24} 
            height={24} 
            priority
          /> */}
        </div>
      </SidebarHeader>

      <SidebarContent>
        <SidebarGroup>
          <SidebarGroupLabel className="text-[10px] uppercase tracking-widest text-muted-foreground font-bold pt-4">
            Analytics
          </SidebarGroupLabel>
          <SidebarGroupContent>
            <SidebarMenu>
              {navItems.map((item) => (
                <SidebarMenuItem key={item.title}>
                  <SidebarMenuButton
                    asChild
                    tooltip={item.title}
                    className="hover:bg-hover hover:text-secondary transition-button">
                    <Link href={item.url}>
                      <item.icon className="h-4 w-4" />
                      <span>{item.title}</span>
                    </Link>
                  </SidebarMenuButton>
                </SidebarMenuItem>
              ))}
            </SidebarMenu>
          </SidebarGroupContent>
        </SidebarGroup>
        <SidebarGroup>
          <SidebarGroupLabel className="text-[10px] uppercase tracking-widest text-muted-foreground font-bold pt-4">
            Add Connection
          </SidebarGroupLabel>

          <SidebarGroupContent>
            <SidebarMenu>
              <SidebarMenuItem>
                <SidebarMenuButton asChild>
                  <Link href="/addConnection/aws">
                    <Network className="h-4 w-4" />
                    <span>AWS</span>
                  </Link>
                </SidebarMenuButton>
              </SidebarMenuItem>

              <SidebarMenuItem>
                <SidebarMenuButton asChild>
                  <Link href="/addConnection/gcp">
                    <Network className="h-4 w-4" />
                    <span>Google Cloud Platform</span>
                  </Link>
                </SidebarMenuButton>
              </SidebarMenuItem>

              <SidebarMenuItem>
                <SidebarMenuButton asChild>
                  <Link href="/addConnection/azure">
                    <Network className="h-4 w-4" />
                    <span>Azure</span>
                  </Link>
                </SidebarMenuButton>
              </SidebarMenuItem>
            </SidebarMenu>
          </SidebarGroupContent>
        </SidebarGroup>
      </SidebarContent>

      <SidebarFooter>
        <SidebarMenu>
          <SidebarGroupLabel>
            {authContext?.user?.email}
          </SidebarGroupLabel>
          <SidebarMenuButton onClick={() => {
            logout();
          }}>
            Logout
          </SidebarMenuButton>
          <SidebarMenuButton
            onClick={() => setTheme(theme === "dark" ? "light" : "dark")}
            tooltip="Toggle Theme"
          >
            {theme === "dark" ? (
              <>
                <Sun className="h-4 w-4" />
                <span className="ml-2 group-data-[collapsible=offcanvas]:hidden">Light Mode</span>
              </>
            ) : (
              <>
                <Moon className="h-4 w-4" />
                <span className="ml-2 group-data-[collapsible=offcanvas]:hidden">Dark Mode</span>
              </>
            )}
          </SidebarMenuButton>
        </SidebarMenu>
      </SidebarFooter>
    </Sidebar>
  );
}
