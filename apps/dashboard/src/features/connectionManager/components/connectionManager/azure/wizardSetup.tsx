//wizard stepup

"use client";

import StepOneAzure from "./stepOne";
import StepTwoAzure from "./stepTwo";
import StepThreeAzure from "./stepThree";
import { useState } from "react";
import { ResourceDetail } from "@/lib/fetch/cloud-resource-api";

interface DataForWizard {
    displayName: string;
    ingestionPeriod: number;
    servicesSelected: string[];
    resources: ResourceDetail[];
}

//copied from previous pr
export default function WizardSetupAzure() {
    const [step, setStep] = useState<1 | 2 | 3>(1);

    const [wizardData, setWizardData] = useState<DataForWizard>({
        displayName: "Azure Connection",
        ingestionPeriod: 60,
        servicesSelected: [],
        resources: [],
    });

    const handleStepOneNext = (data: { displayName: string }) => {
        setWizardData((previous) => ({
            ...previous,
            displayName: data.displayName,
        }));

        setStep(2);
    };

    const handleStepTwoNext = (data: {
        servicesSelected: string[];
        resources: ResourceDetail[];
    }) => {
        setWizardData((previous) => ({
            ...previous,
            servicesSelected: data.servicesSelected,
            resources: data.resources,
        }));

        setStep(3);
    };

    const handleStepThreeComplete = (ingestionPeriod: number) => {
        setWizardData((previous) => ({
            ...previous,
            ingestionPeriod,
        }));
    };

    const handleBack = () => {
        setStep((previous) => (previous - 1) as 1 | 2 | 3);
    };

    return (
        <>
            {step === 1 && <StepOneAzure onNext={handleStepOneNext} />}

            {step === 2 && <StepTwoAzure onNext={handleStepTwoNext} onBack={handleBack} />}

            {step === 3 && (
                <StepThreeAzure
                    displayName={wizardData.displayName}
                    ingestionPeriod={wizardData.ingestionPeriod}
                    resources={wizardData.resources}
                    onComplete={handleStepThreeComplete}
                    onBack={handleBack}
                />
            )}
        </>
    );
}
