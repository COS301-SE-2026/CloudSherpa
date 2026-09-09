"use client";

import {Wrench} from "lucide-react";
import HelpCenter from "@/features/helpMenu/documents/documentsPage";
import {creatingIns} from "@/features/helpMenu/documents/createInstructions";
import {useSearchParams} from "next/navigation";
import {Suspense} from "react";

const Aws_Troubleshooting_INS = creatingIns([
    {name : "Billing export issues",
     description : "Troubleshoot AWS billing export configuration",
     details : [
        "S3 bucket name - The user should get it from their own AWS environment. They need to identify existing S3 buckets that they own and have to write permissions for or create one for this purpose. The name must be globally unique",
        "Export name - The user can create this themselves. It is an identifier for the export they are setting up. The AWS documentation provides rule for valid names, such as using only alphanumeric characters, hyphens and underscores",
        "Prefix/path - This is a virtual folder inside the S3 bucket. The user defines this themselves to help organize their exported files.",
        "Bucket region - Users must know the AWS region where their chosen S3 bucket is located. This is a property of the S3 bucket and this can be found in the S3 console or via the AWS CLI.", 
     ],
    },
]);

const Gcp_Troubleshooting_INS = creatingIns([
    {name : "Billing export issues",
     description : "Troubleshoot AWS billing export configuration",
     details : [
        "Obtain your cloud billing account id. You can find this in the Google cloud console under your billing account details",
        "Create or have ready a BigQuery Dataset id. If you do not have one, create a new dataset in BigQuery in a supported region",
        "Data availability - If you use a multi-region dataset (like US/EU), the initial export will backfill data from the beginning of the previous month. If you use a single region dataset, the export will only contain data from the day you enabled it forward.",
     ],
    },

    {name : "Service account issues",
     description : "Troubleshoot GCP resource discovery",
     details : [
        "Verify the service accounts IAM roles - in the GCP console, go to the IAM and check which roles are assigned to the specific service account at the organization, folder and project levels",
        "Confirm minimum required permissions - ensure the service account has at least the browser role or a custom role that includes list permissions",
        "Test discovery with gcloud - use the gcloud projects list command with the service accounts key file to simulate the discovery and see which projects are returned",
     ],
    },
]);

const Azure_Troubleshooting_INS = creatingIns([

]);

function ContentForTroubleshooting(){
    const search = useSearchParams();

    const forProviders = search.get("forProviders");

    const getInstructions = () => {
        switch(forProviders){
            case "aws" :
                return{
                    instructions : Aws_Troubleshooting_INS, name : "AWS troubleshooting guide",
                    description : "Troubleshoot common AWS connection, service account and billing issues", icon : Wrench,
                };
            
            case "gcp" : 
                return{
                    instructions : Gcp_Troubleshooting_INS, name : "GCP troubleshooting guide",
                    description : "Troubleshoot common GCP connection, service account and billing issues", icon : Wrench,
                };
            
            case "azure" :
                return{
                    instructions : Azure_Troubleshooting_INS, name : "Azure troubleshooting guide",
                    description : "Troubleshoot common Azure connection, service account and billing issues", icon : Wrench,
                };
            
            default : 
                return{
                    instructions : Aws_Troubleshooting_INS, name : "Troubleshooting guide",
                    description : "Troubleshoot common AWS connection, service account and billing issues", icon : Wrench,
                };
        }
    };

    const {instructions,name, description, icon} = getInstructions();

    return(
        <HelpCenter name = {name} description = {description} breadcrumb = "Troubleshooting" icon = {icon} instructions = {instructions}/>
    );
}

export default function Troubleshooting(){
    return(
        <Suspense fallback = {<div className = "flex items-center justify-center min-h-screen"> Loading... </div>}> </Suspense>
    );
}