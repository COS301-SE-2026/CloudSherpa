"use client";

import { useMemo, useState, type ComponentType } from "react";
import {
    HelpCircle,
    Search,
    BookOpen,
    PlayCircle,
    Command,
    Laptop,
    ArrowUpRight,
} from "lucide-react";
import { Button } from "@/components/atoms/button";
import { Input } from "@/components/atoms/input";
import { Popover, PopoverContent, PopoverTrigger } from "@/components/atoms/popover";
import {
    Accordion,
    AccordionContent,
    AccordionItem,
    AccordionTrigger,
} from "@/components/atoms/accordion";
import { Dialog, DialogHeader, DialogContent, DialogTitle } from "@/components/atoms/dialog";
import { useRouter } from "next/navigation";

/* 
- the page needs to provide info about how to nav around clousherpa
- should have a help center (should have docs and tuts)
- should have faqs
-
*/

interface LinksForHelp {
    id: string;
    label: string;
    description: string;
    icon: ComponentType<{ className?: string; strokeWidth?: number }>;
    href?: string;
    action?: "shortcut" | "first time user" | "tutorials";
}

const LINKS: LinksForHelp[] = [
    {
        id: "help center",
        label: "Help Center",
        description: "Browse documents and guides",
        icon: BookOpen,
        href: "/helpMenu/documentsAndTutorials",
    },

    {
        id: "tutorials",
        label: "Tutorials",
        description: "Step-by-step CloudSherpa walkthroughs",
        icon: PlayCircle,
        action: "tutorials",
    },

    {
        id: "shortcut",
        label: "Keyboard Shortcuts",
        description: "Navigate CloudSherpa faster",
        icon: Command,
        action: "shortcut",
    },
];

interface FaQuestion {
    id: string;
    question: string;
    answer: string;
}

const QUESTION: FaQuestion[] = [
    {
        id: "question1",
        question: "How do I connect my cloud provider?",
        answer: "Go to Connection Manager and click on Add. Follow the steps of the wizard by entering your credentials and selecting your services and resource to be monitored. You have then successfully connected your cloud provider!",
    },

    {
        id: "question2",
        question: "Can I have more than one connection?",
        answer: "Yes, you can add and remove connections via the Connection manager.",
    },

    {
        id: "question3",
        question: "Can I customize my dashboard?",
        answer: "Yes, you can add widgets that are displayed with charts that you prefer. You can add more than one dashboard. You can also choose what you would like to monitor on your dashboard.",
    },

    {
        id: "question4",
        question: "What are the main features of CloudSherpa?",
        answer: "CloudSherpa provides multi-cloud data ingestion, normalization and an interactive finOps dashboard.",
    },

    {
        id: "question5",
        question: "How does CloudSherpa handle data normalization for cloud providers like AWS?",
        answer: "CloudSherpa normalizes AWS data by ingesting and converting it into a standardized format, making it consistent with other cloud providers for unified analysis.",
    },
];

interface KeyboardShortcuts {
    key: string[];
    function: string;
}

const SHORTCUT: KeyboardShortcuts[] = [{ key: ["ENTER"], function: "Submit form" }];

export function HelpMenu() {
    const router = useRouter();

    const [open, setOpen] = useState(false);

    const [search, setSearch] = useState("");

    const [keyboardShortcutOpen, setKeyboardShortcutOpen] = useState(false);

    const searchLinks = useMemo(() => {
        if (!search.trim()) {
            return LINKS;
        }

        const searchQuery = search.toLowerCase();

        return LINKS.filter(
            (filteredLinks) =>
                filteredLinks.label.toLowerCase().includes(searchQuery) ||
                filteredLinks.description.toLowerCase().includes(searchQuery)
        );
    }, [search]);

    const searchFaQuestion = useMemo(() => {
        if (!search.trim()) {
            return QUESTION;
        }

        const searchQuery = search.toLowerCase();

        return QUESTION.filter(
            (filteredFaQuestions) =>
                filteredFaQuestions.question.toLowerCase().includes(searchQuery) ||
                filteredFaQuestions.answer.toLowerCase().includes(searchQuery)
        );
    }, [search]);

    function handlingLinks(link: LinksForHelp) {
        if (link.action === "shortcut") {
            setOpen(false);
            setKeyboardShortcutOpen(true);
            return;
        }

        if (link.action === "tutorials") {
            setOpen(false);

            router.push("helpMenu/documentsAndTutorials?tab=tutorials");
            return;
        }

        if (link.href) {
            setOpen(false);

            router.push(link.href);
        }
    }

    return (
        <>
            <Popover open={open} onOpenChange={setOpen}>
                <PopoverTrigger asChild>
                    <Button
                        variant="ghost"
                        size="icon"
                        className="h-8 w-8 rounded-full text-muted-foreground hover:bg-accent hover:text-foreground"
                    >
                        {" "}
                        <HelpCircle className="h-4 w-4" strokeWidth={1.75} />{" "}
                    </Button>
                </PopoverTrigger>

                <PopoverContent
                    align="end"
                    sideOffset={8}
                    className="w-[360px] border-border bg-popover p-0 text-popover-foreground"
                >
                    <div className="border-b border-border px-4 py-3.5">
                        <span className="text-[13px] font-medium text-foreground">
                            {" "}
                            Help &amp; resources{" "}
                        </span>
                    </div>

                    <div className="px-3 pt-3">
                        <div className="relative">
                            <Search
                                className="pointer-events-none absolute left-2.5 top-1/2 h-3 w-3.5 -translate-y-1/2 text-muted-foreground"
                                strokeWidth={1.75}
                            />

                            <Input
                                value={search}
                                onChange={(change) => setSearch(change.target.value)}
                                placeholder="Search help"
                                className="h-8 border-border bg-background pl-8 text-[13px]"
                            />
                        </div>
                    </div>

                    <div className="px-2 pb-1 pt-2">
                        {searchLinks.map((link) => {
                            const Icons = link.icon;
                            return (
                                <button
                                    key={link.id}
                                    type="button"
                                    onClick={() => handlingLinks(link)}
                                    className="flex w-full items-center gap-3 rounded-md px-2.5 py-2 text-left transition-colors hover:bg-accent"
                                >
                                    <span className="flex h-7 w-7 shrink-0 items-center justify-center rounded-md bg-muted text-primary">
                                        {" "}
                                        <Icons className="h-3.5 w-3.5" strokeWidth={1.75} />{" "}
                                    </span>

                                    <span className="min-w-0 flex-1">
                                        <span className="block text-[13px] font-medium text-foreground">
                                            {" "}
                                            {link.label}{" "}
                                        </span>
                                        <span className="block truncate text-[12px] text-muted-foreground">
                                            {" "}
                                            {link.description}{" "}
                                        </span>
                                    </span>

                                    {link.href && (
                                        <ArrowUpRight
                                            className="h-3.5 w-3.5 shrink-0 text-muted-foreground"
                                            strokeWidth={1.75}
                                        />
                                    )}
                                </button>
                            );
                        })}

                        {searchLinks.length === 0 && searchFaQuestion.length === 0 && (
                            <p className="px-2.5 py-6 text-center text-[12.5px] text-muted-foreground">
                                {" "}
                                No results for &quot;{search}&quot;{" "}
                            </p>
                        )}
                    </div>

                    {searchFaQuestion.length > 0 && (
                        <>
                            <div className="flex items-center gap-2 px-4 py-1.5">
                                <span className="h-px flex-1 bg-border" />

                                <span className="text-[10px] font-medium uppercase tracking-wider text-muted-foreground">
                                    {" "}
                                    Frequently asked{" "}
                                </span>

                                <span className="h-px flex-1 bg-border" />
                            </div>

                            <Accordion type="single" collapsible className="px-2 pb-2">
                                {searchFaQuestion.map((question) => (
                                    <AccordionItem
                                        key={question.id}
                                        value={question.id}
                                        className="border-none px-0.5"
                                    >
                                        <AccordionTrigger className="rounded-md px-2 py-2 text-[12.5px] font-medium text-foreground hover:bg-accent hover:no-underline">
                                            {" "}
                                            {question.question}{" "}
                                        </AccordionTrigger>

                                        <AccordionContent className="px-2 pb-2.5 text-[12px] leading-relaxed text-muted-foreground">
                                            {" "}
                                            {question.answer}{" "}
                                        </AccordionContent>
                                    </AccordionItem>
                                ))}
                            </Accordion>
                        </>
                    )}
                </PopoverContent>
            </Popover>

            <Dialog open={keyboardShortcutOpen} onOpenChange={setKeyboardShortcutOpen}>
                <DialogContent className="border-border bg-popover text-popover-foreground sm:max-w-[320px]">
                    <DialogHeader>
                        <DialogTitle className="text-[13px] font-medium">
                            {" "}
                            Keyboard shortcuts
                        </DialogTitle>
                    </DialogHeader>

                    <div className="py-1">
                        {SHORTCUT.map((keyshorts) => (
                            <div
                                key={keyshorts.function}
                                className="flex items-center justify-between py-1.5"
                            >
                                <span className="text-[12.5px] text-muted-foreground">
                                    {" "}
                                    {keyshorts.function}{" "}
                                </span>

                                <span className="flex gap-1">
                                    {keyshorts.key.map((keys) => (
                                        <kbd
                                            key={keys}
                                            className="inline-flex h-5 min-w-5 items-center justify-center rounded border border-border bg-muted px-1 text-[11px] font-medium text-muted-foreground"
                                        >
                                            {" "}
                                            {keys}{" "}
                                        </kbd>
                                    ))}
                                </span>
                            </div>
                        ))}
                    </div>
                </DialogContent>
            </Dialog>
        </>
    );
}
