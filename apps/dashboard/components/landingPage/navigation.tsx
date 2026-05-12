'use client';
import { Button } from "@/components/atoms/button";

interface forNavigationProps{
  clickOnFeatures?: () => void;
}

export default function forNavigation({ clickOnFeatures }: forNavigationProps){
  return(
    <nav className="flex justify-between items-center px-10 py-[18px] relative z-10">
      <div className="flex items-center gap-2.5 text-[15px] font-semibold">
        <div className="w-7 h-7 rounded-md bg-[#374151]" />
        CloudSherpa
      </div>

      <div className="flex items-center gap-8">
        <a 
          href="#"
          onClick={(click) => {
            click.preventDefault();
            clickOnFeatures?.();
          }}
          className="text-[#CBD5E1] no-underline text-sm opacity-80 hover:opacity-100 transition cursor-pointer"
        >
          features
        </a>

        <Button 
          className="bg-blue-600 hover:bg-blue-700 text-white text-sm px-4 py-2 border border-black"
        >
          Get Started
        </Button>

      </div>
    </nav>
  );
}