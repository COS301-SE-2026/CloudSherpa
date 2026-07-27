"use client";

import {useState} from "react";
import {ChevronDown, Menu} from "lucide-react";
import {Button} from "@/components/atoms/button";
import {Sheet, SheetContent} from "@/components/atoms/sheet";
import {NavigationMenu, NavigationMenuItem, NavigationMenuLink, NavigationMenuList} from "@/components/atoms/navigation-menu";
import Link from "next/link";
import Image from "next/image";

/*
- should have CloudSherpa and logo, features, hows it works, who its for and taglien
*/

interface PropsForNavBarHero{
  scrolled : boolean;
}

export function HeroAndNavBar({scrolled} : Readonly<PropsForNavBarHero>){
  const [open, setOpen] = useState(false);

  return(
    <>
      <nav className = {`fixed top-0 left-0 right-0 z-50 transition-all duration-300 ${scrolled ? "backdrop-blur-xl bg-background/92" : "bg-transparent"} border-b ${scrolled ? "border-border" : "border-transparent"}`}>
        <div className = "max-w-6xl mx-auto px-6 flex items-center justify-between h-16">
          <Link href = "/" className = "flex items-center gap-2.5">

            <Image src="/CloudSherpaFavicon.svg" alt="CloudSherpa Logo"
                    width={28} height={28}
                    className="rounded-md"
            />

            <span className = "text-base font-bold text-foreground tracking-tight"> CloudSherpa </span>

          </Link>

          <div className = "hidden md:flex items-center gap-8">
            <NavigationMenu>
              <NavigationMenuList>
                {["Features", "How it works", "Who it's for"].map((forList) => (
                  <NavigationMenuItem key = {forList}>
                    <NavigationMenuLink href = {`#${forList.toLowerCase().replace(/[\s']+/g, "-")}`} className = "text-sm text-muted-foreground hover:text-foreground transition-colors"> {forList} </NavigationMenuLink>
                  </NavigationMenuItem>
                ))}
              </NavigationMenuList>
            </NavigationMenu>
          </div>

          <Sheet open = {open} onOpenChange = {setOpen}>

            <Button variant = "ghost" size = "icon" className = "md:hidden" onClick = {() => setOpen(true)}> <Menu size = {20}/> </Button>

            <SheetContent side = "right">
              <div className = "flex flex-col gap-4 mt-8">
                {["Features", "How it works", "Who it's for"].map((forList) => (
                  //convert the name to a url friendly formts
                  <a key = {forList} href = {`#${forList.toLowerCase().replace(/[\s']+/g, "-")}`} className = "text-sm text-muted-foreground hover:text-foreground transition-colors" onClick = {() => setOpen(false)}> {forList} </a>
                ))}
              </div>
            </SheetContent>

          </Sheet>
        </div>
      </nav>

      <section className = "relative min-h-screen flex flex-col items-center justify-center text-center overflow-hidden pt-16 pb-8">
        <div className = "relative max-w-4xl mx-auto px-6 flex flex-col items-center justify-center py-10">

          <h1 className = "text-4xl md:text-6xl lg:text-7xl font-semibold leading-[1.04] tracking-tight mb-2 text-foreground text-center"> Cloud Costs </h1>

          <h1 className = "text-4xl md:text-6xl lg:text-7xl font-semibold leading-[1.04] tracking-tight mb-8 text-primary text-center"> Eliminated </h1>

          <p className = "text-lg md:text-xl max-w-xl mx-auto mb-10 leading-relaxed text-muted-foreground text-center"> AI powered optimization for multi-cloud environments. One unified picture across all your clouds before the bill shock hits </p>

          
          <Button asChild className = "mb-10 transition-transform hover:scale-[1.03]"> 
            <Link href = "/login" role = "button">
              Get Started 
            </Link>
          </Button>
          

          <a href = "#problem" className = "flex flex-col items-center gap-2 text-xs text-muted-foreground hover:text-foreground transition-colors"> Discover more <ChevronDown size = {16} className = "animate-bounce"/> </a>

        </div>
      </section>
    </>
  )
}