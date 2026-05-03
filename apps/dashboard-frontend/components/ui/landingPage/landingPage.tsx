'use client';
import { useState, useEffect, useRef } from "react";
import Navigation from "./navigation";
import View from "./view";
import FeatureCards from "./featureCards";

//this is for the main landing page components
export default function forLandingPage(){
  const [showFeatures, setShowFeatures] = useState(false);
  const featuresRef = useRef<HTMLElement>(null);
  const [hasAnimated, setHasAnimated] = useState(false);

  useEffect(() => {
    const forScrollHandling = () => {
      if(featuresRef.current && !hasAnimated){
        const forBlock = featuresRef.current.getBoundingClientRect();
        const forVisability = forBlock.top<=window.innerHeight*0.7;
        
        if(forVisability){
          setShowFeatures(true);
          setHasAnimated(true);
        }

      }
    };

    window.addEventListener('scroll', forScrollHandling);
    forScrollHandling();
    
    return () => window.removeEventListener('scroll', forScrollHandling);
  }, [hasAnimated]);

  //this is called when the user clicks on the discover more button
  const handlesDiscoverMoreClick = () => {
    setShowFeatures(true);
    setHasAnimated(true);

    featuresRef.current?.scrollIntoView({ behavior: 'smooth', block: 'start' });
  };

  //will scroll down to the features of CloudSherpa when the features button is pressed
  const handlesFeaturesClick = () => {
    setShowFeatures(true);
    setHasAnimated(true);

    featuresRef.current?.scrollIntoView({ behavior: 'smooth', block: 'start' });
  };

  return(
    <div className="min-h-screen bg-[#030712] text-[#CBD5E1]">
      <Navigation clickOnFeatures={handlesFeaturesClick} />
      <View onDiscoverMoreClick={handlesDiscoverMoreClick} />
      <FeatureCards 
        ref={featuresRef}
        showingFeatureCards={showFeatures} 
      />
    </div>
  );
}