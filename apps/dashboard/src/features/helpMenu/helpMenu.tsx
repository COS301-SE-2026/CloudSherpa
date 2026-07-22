"use client";

import {useMemo, useState, type ComponentType} from "react";
import {HelpCircle, Search, BookOpen, PlayCircle, Command, MessageCircle, ArrowUpRight} from "lucide-react";
import {Button} from "@/components/atoms/button";
import {Input} from "@/components/atoms/input";
import {Popover, PopoverContent, PopoverTrigger} from "@/components/atoms/popover";
import {Accordion, AccordionContent, AccordionItem, AccordionTrigger} from "@/components/atoms/accordion";
import {Dialog, DialogHeader, DialogContent, DialogTitle} from "@/components/atoms/dialog";

/* 
- the page needs to provide info about how to nav around clousherpa
- should have a help center (should have docs and tuts)
- should have faqs
-
*/

interface LinksForHelp{
  id : string;
  label : string;
  description : string;
  icon : ComponentType<{className?: string; strokeWidth?: number}>;
  href? : string;
  action? : "shortcut" | "first time user";
}

const LINKS : LinksForHelp[] = [
  { id : "help center", label : "Help Center",
    description : "Browse documents and guides", icon : BookOpen,
    href : "documentsAndTutorials",
  },

  { id : "tutorials", label : "Tutorials",
    description : "Step-by-step CloudSherpa walkthroughs", icon : PlayCircle,
    href : "/documentsAndTutorials",
  },

  { id : "shortcut", label : "Keyboard Shortcuts",
    description : "Navigate CloudSherpa faster", icon : Command,
    action : "shortcut",
  },

  { id : "first time user", label : "First time user",
    description : "Learn by watching", icon : MessageCircle,
    action : "first time user",
  },
];

interface FaQuestion{
  id : string;
  question : string;
  answer : string;
}

const QUESTION : FaQuestion[] = [
  {
    id : "question1",
    question : "How do I connect my cloud provider?",
    answer : "Go to Connection Manager and click on Add. Follow the steps of the wizard by entering your credentials and selecting your services and resource to be monitored. You have then successfully connected your cloud provider!",
  },

  {
    id : "question2",
    question : "How do I connect my cloud provider?",
    answer : "Go to Connection Manager and click on Add. Follow the steps of the wizard by entering your credentials and selecting your services and resource to be monitored. You have then successfully connected your cloud provider!",
  },

  {
    id : "question3",
    question : "How do I connect my cloud provider?",
    answer : "Go to Connection Manager and click on Add. Follow the steps of the wizard by entering your credentials and selecting your services and resource to be monitored. You have then successfully connected your cloud provider!",
  },

  {
    id : "question4",
    question : "How do I connect my cloud provider?",
    answer : "Go to Connection Manager and click on Add. Follow the steps of the wizard by entering your credentials and selecting your services and resource to be monitored. You have then successfully connected your cloud provider!",
  },

  {
    id : "question5",
    question : "How do I connect my cloud provider?",
    answer : "Go to Connection Manager and click on Add. Follow the steps of the wizard by entering your credentials and selecting your services and resource to be monitored. You have then successfully connected your cloud provider!",
  },
];

interface KeyboardShortcuts{
  key : string[];
  function : string;
}

const SHORTCUT : KeyboardShortcuts[] = [
  {key : ["ENTER"], function : "Submit form"},
];

export function HelpMenu(){
  const [open, setOpen] = useState(false);

  return(
    <>
      <Popover open = {open} onOpenChange = {setOpen}>
        <PopoverTrigger asChild>
          <Button variant = "ghost" size = "icon" className = "h-8 w-8 rounded-full text-muted-foreground hover:bg-accent hover:text-foreground"> <HelpCircle className = "h-4 w-4" strokeWidth = {1.75}/> </Button>
        </PopoverTrigger>

        <PopoverContent align = "end" sideOffset = {8} className = "w-[360px] border-border bg-popover p-0 text-popover-foreground">
          <div className = "border-b border-border px-4 py-3.5">
            <span className = "text-[13px] font-medium text-foreground"> Help &amp; resources </span>
          </div>

        </PopoverContent>
      </Popover>
    </>
  );

}

