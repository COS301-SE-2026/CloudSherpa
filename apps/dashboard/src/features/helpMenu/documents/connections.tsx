"use client";

import { Plug } from "lucide-react";
import HelpCenter from "@/features/helpMenu/documents/documentsPage";

/*
- this page give users all the info on how to add, remove and manage their conn
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
        name: "Navigate to Connection Manager",
        description: "Access the Connection Manager from your dashboard",
        details: [
            "From your dashboard, locate the sidebar on the left",
            "Click on Connection Manager",
            "You will be redirected to the Connection Manager page where you can view and configure your cloud connections",
        ],
    },

    {
        id: "ins2",
        name: "Add an AWS cloud provider",
        description: "Follow the steps of the wizard to add your first cloud provider",
        details: [
            "On the Connection Manager page, click on add button located in the top right corner",
            "A wizard will appear",
            "Follow the steps of the wizard and you will be able to successfully add your first AWS connection",
        ],
    },

    {
        id: "ins3",
        name: "Enter your credentials",
        description: "Provide your credentials for the cloud provider",
        details: [
            "Enter your cloud providers credentials",
            "Ensure you have the required permissions",
        ],
    },

    {
        id: "ins4",
        name: "Select services and resources",
        description: "Choose which services and resources you would like CloudSherpa to monitor",
        details: [
            "Select the services you want to keep track of",
            "Choose the specific resources within each service you want to monitor",
        ],
    },

    {
        id: "ins5",
        name: "Manage your connections",
        description: "View and configure your connections",
        details: [
            "You can add or remove connections at any time from the Connection Manager",
            "You are able to rename your connections",
        ],
    },
];

export default function Connection() {
    return (
        <HelpCenter name = "Connecting your AWS cloud provider"
                    description = "Follow these five steps to connect your AWS account"
                    breadcrumb = "Connections"
                    icon = {Plug}
                    instructions = {INS}
        />
    );
}
