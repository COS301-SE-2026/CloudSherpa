"use client";
import { Button } from "@/components/atoms/button";
import Link from "next/link";

interface ForViewingItems {
    onDiscoverMoreClick: () => void; //this is the function that is called when the discover more button is clicked
}

export default function viewing({ onDiscoverMoreClick }: ForViewingItems) {
    return (
        <section
            className="relative h-[calc(100vh-65px)] flex flex-col items-center justify-center text-center overflow-hidden px-10 pt-15 pb-0"
            style={{
                background: `
                radial-gradient(ellipse 40% 50% at 0% 0%, #000 0%, transparent 60%), radial-gradient(ellipse 40% 50% at 100% 0%, #000 0%, transparent 60%),
                radial-gradient(ellipse 40% 50% at 0% 100%, #000 0%, transparent 60%), radial-gradient(ellipse 40% 50% at 100% 100%, #000 0%, transparent 60%),
                radial-gradient(ellipse 70% 50% at 50% 50%, #0D1633 0%, #030712 100%)`,
            }}
        >
            <div className="relative z-20 max-w-[560px] flex-1 flex flex-col items-center justify-center">
                <h1 className="text-5xl font-bold text-white leading-tight mb-3 tracking-tight">
                    Cloud Costs {/**/}
                    <span className="block text-4xl bg-gradient-to-r from-[#2F2FE4] to-[#162E93] bg-clip-text text-transparent">
                        Eliminated
                    </span>
                </h1>

                <p className="text-base text-[#CBD5E1] mb-8 opacity-75">
                    AI-powered optimization for multi cloud environments
                </p>

                <Button className="bg-blue-600 hover:bg-blue-700 text-white px-7 py-3 text-base border border-black">
                    <Link href="/login">Start saving now</Link>
                </Button>
            </div>

            {/*this is for the discover more button (scrolls)*/}
            <button
                onClick={onDiscoverMoreClick}
                className="flex flex-col items-center gap-1.5 text-sm cursor-pointer pb-8 transition-opacity duration-200 z-20 opacity-60 hover:opacity-100"
            >
                Discover more
                <svg
                    width="16"
                    height="16"
                    viewBox="0 0 16 16"
                    fill="none"
                    className="animate-bounce"
                >
                    <path
                        d="M8 3v10M3 8l5 5 5-5"
                        stroke="currentColor"
                        strokeWidth="1.5"
                        strokeLinecap="round"
                        strokeLinejoin="round"
                    />
                </svg>
            </button>
        </section>
    );
}
