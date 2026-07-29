"use client";

import { Boxes } from "lucide-react";
import HelpCenter from "@/features/helpMenu/documents/documentsPage";
import {creatingIns} from "@/features/helpMenu/documents/createInstructions";

/*
- users should be able to easily follow the ins on how to get to, manage and delete theri resources
*/

export default function Resource(){
    const INS = creatingIns([
        {
            name: "Access the Resource Manager",
            description: "Navigate to the Resource Manager from your dashboard",
            details: [
                "From your dashboard, locate the sidebar on the left",
                "Select Resource Manager",
                "Once redirected you will be able to view all your active and inactive resources associated with a specific service",
            ],
        },

        {
            name: "Filter and search for resources",
            description: "Use filters and search to find a resource",
            details: [
                "Use the search bar to find a particular resource",
                "Filter the table to view the resources",
            ],
        },

        {
            name: "Active or Inactive resources",
            description: "Manage which resources are being monitored",
            details: [
                "Toggle on a resource to select which resource should be monitored (active) and which should not (inactive)",
            ],
        },
    ]);

    return (
        <HelpCenter name = "Managing your cloud resources"
                    description = "Learn how to manage and configure your cloud resources effectively with CloudSherpa"
                    breadcrumb = "Resources"
                    icon = {Boxes}
                    instructions = {INS}
        />
    );

}