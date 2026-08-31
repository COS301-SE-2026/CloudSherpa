"use client";

import { Plug } from "lucide-react";
import HelpCenter from "@/features/helpMenu/documents/documentsPage";
import { creatingIns } from "@/features/helpMenu/documents/createInstructions";

/*
- this page give users all the info on how to add, remove and manage their conn
*/

//removed from inside func
const Aws_INS = creatingIns([
    {
        name: "Navigate to Connection Manager",
        description: "Access the Connection Manager from your dashboard",
        details: [
            "From your dashboard, locate the sidebar on the left",
            "Click on Connection Manager",
            "You will be redirected to the Connection Manager page where you can view and configure your cloud connections",
        ],
    },

    {
        name: "Add an AWS cloud provider",
        description: "Follow the steps of the wizard to add your first cloud provider",
        details: [
            "On the Connection Manager page, click on add button located in the top right corner",
            "A wizard will appear",
            "Follow the steps of the wizard and you will be able to successfully add your first AWS connection",
        ],
    },

    {
        name: "Enter your credentials",
        description: "Provide your credentials for the cloud provider",
        details: [
            "Enter your cloud providers credentials",
            "Ensure you have the required permissions",
        ],
    },

    {
        name: "Select services and resources",
        description:
            "Choose which services and resources you would like CloudSherpa to monitor",
        details: [
            "Select the services you want to keep track of",
            "Choose the specific resources within each service you want to monitor",
        ],
    },

    {
        name: "Manage your connections",
        description: "View and configure your connections",
        details: [
            "You can add or remove connections at any time from the Connection Manager",
            "You are able to rename your connections",
        ],
    },
]);

const Gcp_INS = creatingIns([
    {name : "Navigate to Connection manager",
     description : "Access the Connection Manager from your dashboard",
     details : [
        "From your dashboard, locate the sidebar on the left",
        "Click on Connection Manager",
        "You will be redirected to the Connection Manager page where you can view and configure your cloud connections",
     ],
    },

    {name : "Add a GCP cloud provider",
     description : "Follow the steps of the wizard to add GCP as a cloud provider",
     details : [
        "On the Connection Manager page, click on the add button which is located on the top right-hand side corner of the page",
        "Follow the steps of the wizard to add you GCP connection",
     ],
    },

    {name : "Enter your credentials",
        description : "Provide your credentials for GCP",
        details : [
            "Add an account name which you can refer to when looking for the connection",
            "Upload your GCP service account JSON file",
        ],
    },

    {name : "Select your services and resources",
     description : "Choose which services and resources you would like CloudSherpa to monitor",
     details : [
        "Select the services that would be monitored by CloudSherpa and you would then get a list of permissions that you would need to grant for your newly created GCP IAM user and then proceed to step 3",
        "Once all of the resources associated with your selected services have been discovered, you can choose which ones you would like to monitor",
        "The table can be filtered by the names of the resources or you can search for a resource using the toolbar",
     ],
    },
]);

const Azure_INS = creatingIns([
    {name : "Navigate to Connection Manager",
     description : "Access the Connection Manager from your dashboard",
     details : [
        "From your dashboard, locate the sidebar on the left",
        "Click on the Connection Manager",
        "You will be redirected to the Connection Manager page where you can view and configure your cloud connections",
     ],
    },
]);

export default function Connection() {
    

    return (
        <HelpCenter
            name="Connecting your AWS cloud provider"
            description="Follow these five steps to connect your AWS account"
            breadcrumb="Connections"
            icon={Plug}
            instructions={INS}
        />
    );
}
