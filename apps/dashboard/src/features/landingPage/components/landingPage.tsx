"use client";
import { useState, useEffect, useRef } from "react";
import Navigation from "@/features/landingPage/components/navigation";
import View from "@/features/landingPage/components/view";
import FeatureCards from "@/features/landingPage/components/featureCards";

//this is for the main landing page components
export default function LandingPage() {
    const [showFeatures, setShowFeatures] = useState(false);
    const featuresRef = useRef<HTMLElement>(null);
    const [hasAnimated, setHasAnimated] = useState(false);

    useEffect(() => {
        const forScrollHandling = () => {
            if (featuresRef.current && !hasAnimated) {
                const forBlock = featuresRef.current.getBoundingClientRect();
                const forVisability = forBlock.top <= window.innerHeight * 0.7;

                if (forVisability) {
                    setShowFeatures(true);
                    setHasAnimated(true);
                }
            }
        };

        window.addEventListener("scroll", forScrollHandling);
        forScrollHandling();

        return () => window.removeEventListener("scroll", forScrollHandling);
    }, [hasAnimated]);

    //this is called when the user clicks on the discover more button
    const handlesScrollToFeatures = () => {
        setShowFeatures(true);
        setHasAnimated(true);

        featuresRef.current?.scrollIntoView({ behavior: "smooth", block: "start" });
    };

    return (
        <div className="min-h-screen bg-[#030712] text-[#CBD5E1]">
            <Navigation clickOnFeatures={handlesScrollToFeatures} />
            <View onDiscoverMoreClick={handlesScrollToFeatures} />
            <FeatureCards ref={featuresRef} showingFeatureCards={showFeatures} />
        </div>
    );
}
