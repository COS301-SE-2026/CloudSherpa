"use client";

import {Wrench} from "lucide-react";
import HelpCenter from "@/features/helpMenu/documents/documentsPage";
import {creatingIns} from "@/features/helpMenu/documents/createInstructions";
import {useSearchParams} from "next/navigation";
import {Suspense} from "react";

const Aws_Troubleshooting_INS = creatingIns([

]);

const Gcp_Troubleshooting_INS = creatingIns([

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