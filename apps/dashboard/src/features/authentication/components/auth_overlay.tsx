"use client";

import { Button } from "@/components/atoms/button";
import { cn } from "@/lib/utils";

interface OverlayProps {
  isSignUp: boolean;
  toggle: (val: boolean) => void;
}

export function AuthOverlay({ isSignUp, toggle }: Readonly<OverlayProps>) {
  return (
    <div className={cn(
      // Hidden on mobile, visible and 50% width on medium screens and larger
      "hidden md:block absolute top-0 left-1/2 w-1/2 h-full overflow-hidden transition-transform duration-700 ease-in-out z-100",
      isSignUp ? "-translate-x-full" : ""
    )}>
      <div className={cn(
        "relative -left-full h-full w-[200%] transform transition-transform duration-700 ease-in-out bg-secondary",
        isSignUp ? "translate-x-1/2" : "translate-x-0"
      )}>
        
        {/* Go to Login Panel */}
        <div className={cn(
          "absolute top-0 flex flex-col items-center justify-center px-20 text-center h-full w-1/2 transition-transform duration-700 ease-in-out",
          isSignUp ? "translate-x-0" : "translate-x-[20%]"
        )}>
          <h1 className="text-5xl font-bold tracking-tight mb-6">Welcome Back</h1>
          <p className="text-lg font-light mb-10 max-w-md opacity-90">
            Continue managing your cloud costs with precision.
          </p>
          
          <Button 
            variant="outline" 
            className="w-40  text-primary-foreground hover:bg-primary-foreground hover:text-primary hover:border-none transition-all duration-300" 
            onClick={() => toggle(false)}
          >
            Sign In
          </Button>
        </div>

        {/* Go to Register Panel */}
        <div className={cn(
          "absolute top-0 right-0 flex flex-col items-center justify-center px-20 text-center h-full w-1/2 transition-transform duration-700 ease-in-out",
          isSignUp ? "translate-x-[20%]" : "translate-x-0"
        )}>
          <h1 className="text-5xl font-bold tracking-tight mb-6">CloudSherpa</h1>
          <p className="text-lg font-light mb-10 max-w-md opacity-90">
            Start your journey toward automated FinOps and cloud savings.
          </p>
          
          <Button 
            variant="outline" 
            className="w-40  text-primary-foreground hover:bg-primary-foreground hover:text-primary hover:border-none  transition-all duration-300" 
            onClick={() => toggle(true)}
          >
            Get Started
          </Button>
        </div>
      </div>
    </div>
  );
}