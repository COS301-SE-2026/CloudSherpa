"use client";

import {useState, useEffect} from "react";
import {HeroAndNavBar} from "./navigationAndHero";
import {Problem} from "./problem";
import {Solution} from "./solution";
import {HowItWorks} from "./howItWorks";
import {WhoItsFor} from "./whoItsFor";
import {Features} from "./featureCards";

/*
- combines all ages
*/

export default function LandingPage(){
  const [scrolled, setScrolled] = useState(false);

  useEffect(() => {
    const handlingScrolling = () => setScrolled(window.scrollY>24);
    window.addEventListener("scroll", handlingScrolling, {passive : true});

    return () => window.removeEventListener("scroll", handlingScrolling);
  }, []);

  return(
    <div className = "relative min-h-screen bg-background text-foreground">
      <div className = "relative z-10">

        <HeroAndNavBar scrolled = {scrolled}/>
        <Problem/>

        <Solution/>
        <Features/>

        <HowItWorks/>
        <WhoItsFor/>

      </div>
    </div>
  )
}