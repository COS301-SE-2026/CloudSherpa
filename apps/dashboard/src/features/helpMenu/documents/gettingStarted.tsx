"use client";

import { Rocket } from "lucide-react";
import HelpCenter from "@/features/helpMenu/documents/documentsPage";

/*
-this page should help people with navigating around ClourSherpa if they are not familiar with it
*/

interface Instructions {
    id: string;
    name: string;
    description: string;
    details: string[];
}

const INS: Instructions[] = [
    {
        id: "ins1",
        name: "Create your account",
        description: "Sign up and log in to CloudSherpa for the first time",
        details: [
            "Go to the signup page and create your CloudSherpa account",
            "Enter your credentials",
            "Log in and you will land on an empty dashboard and can thereafter utilize CloudSherpas features",
        ],
    },

    {
        id: "ins2",
        name: "Explore your dashboard",
        description: "Monitor data laid out across widgets",
        details: [
            "You can add widgets that you would like to monitor which are represented in charts",
            "Use the period selector to monitor is specific time range",
            "Click edit to rearrange widget or to add new widgets",
        ],
    },
];

export default function GettingStarted() {
    return (
        <HelpCenter name = "Getting started with CloudSherpa"
                    description = "Here are some easy steps to help you navigate CloudSherpa"
                    breadcrumb = "Getting Started"
                    icon = {Rocket}
                    instructions = {INS}
        />
    );
}
