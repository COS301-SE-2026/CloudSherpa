"use client";

import StepOneAzure from "./stepOne";
import StepTwoAzure from "./stepTwo";
import {useRouter} from "next/navigation";
import {BaseWizard} from "@/features/connectionManager/components/connectionManager/wizardSetup/wizard";

export default function WizardSetupAzure(){
    const router = useRouter();

    return(
        <BaseWizard
            eachStep = {[{forComponents : StepOneAzure}, {forComponents : StepTwoAzure},]}

            onComplete = {() => {
                router.push("/manageConnections");
            }}

            initialData = {{
                name : "Azure connection", servicesSelected : [], resources : [],
            }}

            getDataForStep = {(step, forData) => {
                if(step === 0){
                    return{name : forData.name || "Azure connection"};
                }

                if(step === 1){
                    return{servicesSelected : forData.servicesSelected || [], resources : forData.resources || [],};
                }

                if(step === 2){
                    return {};
                }

                return forData;
            }}
        />
    );
}