"use client";
import * as React from "react";
import { LayoutDashboard, Network, Moon, Sun } from "lucide-react";
import { useTheme } from "next-themes";
import Link from "next/link";
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
  const [mounted, setMounted] = React.useState(false);

  React.useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    setMounted(true);
  }, []);

  return (
    <Sidebar collapsible="icon" {...props}>
      <SidebarHeader className="h-16 flex flex-row items-center justify-between px-4 group-data-[collapsible=icon]:justify-center group-data-[collapsible=icon]:px-0">
        {/*open*/}
        <div className="flex items-center gap-3 group-data-[collapsible=icon]:hidden">
          <Image src="/CloudSherpaFavicon.svg" alt="CS" width={30} height={30} priority />
          <span className="font-bold text-lg">CloudSherpa</span>
        </div>

        {/*closed*/}
        <div className="hidden group-data-[collapsible=icon]:flex relative h-8 w-8 items-center justify-center group/logo">
          <div className="transition-opacity duration-200 group-hover/logo:opacity-0 flex items-center justify-center">
            <Image src="/CloudSherpaFavicon.svg" alt="CS" width={30} height={30} priority />
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
          <SidebarGroupLabel>Categories</SidebarGroupLabel>
          <SidebarGroupContent>
            <SidebarMenu>
              <SidebarMenuItem>
                <SidebarMenuButton asChild tooltip="Colours">
                  <Link href="/colours">
                    <Network />
                    <span>Colours</span>
                  </Link>
                </SidebarMenuButton>
              </SidebarMenuItem>

              <SidebarMenuItem>
                <SidebarMenuButton asChild tooltip="Typography">
                  <Link href="/typography">
                    <Network />
                    <span>typography</span>
                  </Link>
                </SidebarMenuButton>
              </SidebarMenuItem>

              <SidebarMenuItem>
                <SidebarMenuButton asChild tooltip="Layout and Spacing">
                  <Link href="/layout-and-spacing">
                    <Network />
                    <span>Layout and Spacing</span>
                  </Link>
                </SidebarMenuButton>
              </SidebarMenuItem>

              <SidebarMenuItem>
                <SidebarMenuButton asChild tooltip="Logo and Icons">
                  <Link href="/logo-and-icons">
                    <Network />
                    <span>Logo and Icons</span>
                  </Link>
                </SidebarMenuButton>
              </SidebarMenuItem>

              <SidebarMenuItem>
                <SidebarMenuButton asChild tooltip="Voice and Tone">
                  <Link href="/voice-and-tone">
                    <Network />
                    <span>Voice and Tone</span>
                  </Link>
                </SidebarMenuButton>
              </SidebarMenuItem>

              <SidebarMenuItem>
                <SidebarMenuButton asChild tooltip="Accessibility">
                  <Link href="/accessibility">
                    <Network />
                    <span>Accessibility</span>
                  </Link>
                </SidebarMenuButton>
              </SidebarMenuItem>

              <SidebarMenuItem>
                <SidebarMenuButton asChild tooltip="Comoponents">
                  <Link href="/components">
                    <Network />
                    <span>Components</span>
                  </Link>
                </SidebarMenuButton>
              </SidebarMenuItem>

              <SidebarMenuItem>
                <SidebarMenuButton asChild tooltip="Design Tokens">
                  <Link href="/design-tokens">
                    <Network />
                    <span>Design Tokens</span>
                  </Link>
                </SidebarMenuButton>
              </SidebarMenuItem>

              <SidebarMenuItem>
                <SidebarMenuButton asChild tooltip="Changelog">
                  <Link href="/changelog">
                    <Network />
                    <span>Changelog</span>
                  </Link>
                </SidebarMenuButton>
              </SidebarMenuItem>
            </SidebarMenu>
          </SidebarGroupContent>
        </SidebarGroup>
      </SidebarContent>

      <SidebarFooter>
        <SidebarMenu>
          {mounted && (
            <SidebarMenuItem>
              <SidebarMenuButton onClick={() => setTheme(theme === "dark" ? "light" : "dark")} tooltip="Toggle Theme">
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
