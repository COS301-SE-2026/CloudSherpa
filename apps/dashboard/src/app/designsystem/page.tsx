"use client";
import Colours from "@/design-system/colours/components/colours";

export default function DesignSystem() {
    return (
        <main className="min-h-screen bg-white dark:bg-neutral-950 text-neutral-900 dark:text-neutral-50">
            <div className="mx-auto w-full max-w-7xl px-4 sm:px-8 py-12 md:py-16">
                <header className="mb-12 md:mb-16">
                    <h1 className="text-4xl md:text-5xl font-black tracking-tight mb-4">
                        CloudSherpa Design System
                    </h1>
                    <p className="text-lg md:text-xl text-neutral-500 max-w-3xl leading-relaxed">
                        The Golden Thread connecting our brand values to our shipped code. This
                        living document serves as the single source of truth for our visual
                        identity, ensuring a cohesive, accessible, and scalable experience across
                        the entire platform.
                    </p>
                </header>

                <div className="flex flex-col gap-16">
                    <section>
                        <div className="border-b border-neutral-200 dark:border-neutral-800 pb-4 mb-8">
                            <h2 className="text-2xl md:text-3xl font-bold">1. Colour Palette</h2>
                            <p className="text-neutral-500 mt-2">
                                Our refined colour system, built for WCAG 2.2 AA compliance and
                                semantic clarity.
                            </p>
                        </div>
                        <Colours />
                    </section>
                </div>
            </div>
        </main>
    );
}
