"use client";

import { LayoutDashboard, Moon, Sun} from "lucide-react";
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

const navItems = [
  { title: "Dashboard", icon: LayoutDashboard, url: "/dashboard" },
];

export function AppSidebar() {
  const { theme, setTheme } = useTheme();
  const isMounted = useSyncExternalStore(
    () => () => {}, 
    () => true,    
    () => false    
  );

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
                    <a href={item.url}>
                      <item.icon className="h-4 w-4" />
                      <span>{item.title}</span>
                    </a>
                  </SidebarMenuButton>
                </SidebarMenuItem>
              ))}
            </SidebarMenu>
          </SidebarGroupContent>
        </SidebarGroup>
      </SidebarContent>

      <SidebarFooter>
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
      </SidebarFooter>
    </Sidebar>
  );
}
