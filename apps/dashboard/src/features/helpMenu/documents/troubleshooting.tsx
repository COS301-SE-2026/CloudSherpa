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

export default function Troubleshooting(){
    return(
        <Suspense fallback = {<div className = "flex items-center justify-center min-h-screen"> Loading... </div>}> </Suspense>
    );
}