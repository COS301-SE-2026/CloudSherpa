"use client";

import { useState, useEffect } from "react";
import { HeroAndNavBar } from "./navigationAndHero";
import { Problem } from "./problem";
import { Solution } from "./solution";
import { HowItWorks } from "./howItWorks";
import { WhoItsFor } from "./whoItsFor";
import { Features } from "./featureCards";
import dynamic from "next/dynamic";

/*
- combines all ages
*/

//used a similar approach as the login & register, skips server rendering and defers mounting authani until after the client loads
//will show the div as a placeholder in the meantime
const AuthAnimation = dynamic(() => import("@/features/authentication/components/authanimation"),{
    ssr : false,

    loading : () => (
        <div className = "absolute top-1/2 left-0 w-full h-[60%] -translate-y-1/2 bg-transparent"/>
    ),

});

export default function LandingPage() {
    const [scrolled, setScrolled] = useState(false);

    useEffect(() => {
        const handlingScrolling = () => setScrolled(window.scrollY > 24);
        window.addEventListener("scroll", handlingScrolling, { passive: true });

        return () => window.removeEventListener("scroll", handlingScrolling);
    }, []);

    return (
        <div className="relative min-h-screen bg-background text-foreground">

            <div className = "relative">
                <AuthAnimation/>
                <HeroAndNavBar scrolled = {scrolled}/>
            </div>

            <div className="relative z-10">
                <Problem />

                <Solution />
                <Features />

                <HowItWorks />
                <WhoItsFor />
            </div>
        </div>
    );
}
