'use client';
import { Button } from "@/components/atoms/button";
import Link from "next/link";
import Image from "next/image";

interface ForNavigationProps{
  clickOnFeatures?: () => void;
}

export default function forNavigation({ clickOnFeatures }: ForNavigationProps){
  return(
    <nav className="flex justify-between items-center px-10 py-[18px] relative z-10">
      {/* icon place holder */}
      <div className="flex items-center gap-2.5 text-[15px] font-semibold">
        <Image src="/CloudSherpaFavicon.svg" alt="CloudSherpa Logo" width={28} height={28} className="rounded-md mb-2" />
        CloudSherpa
      </div>

      <div className="flex items-center gap-8">
        <button 
          onClick={(click) => {
            click.preventDefault();
            clickOnFeatures?.();
          }}
          className="text-[#CBD5E1] no-underline text-sm opacity-80 hover:opacity-100 transition cursor-pointer"
        >
          features
        </button>

{/* navigate to login/register page */}
        <Link href="/login">
          <Button 
            className="bg-blue-600 hover:bg-blue-700 text-white text-sm px-4 py-2 border border-black"
          >
            Get Started
          </Button>
        </Link>

      </div>
    </nav>
  );
}