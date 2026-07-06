"use client";
import { useState } from "react";
import StepOne from "./stepOne";
import StepTwo from "./stepTwo";
import StepThree from "./stepThree";
import { ResourceDetail } from "@/lib/fetch/cloud-resource-api";

interface WizardData {
    credentials: {
        accessKey: string;
        secretKey: string;
        awsRegion: string;
    } | null;
    selectedServices: string[];
    selectedInstances: string[];
    resources: ResourceDetail[];
}

export default function WizardSetup() {
    const [step, setStep] = useState<1 | 2 | 3>(1);

    const [wizardData, setWizardData] = useState<WizardData>({
        credentials: null,
        selectedServices: [],
        selectedInstances: [],
        resources: [],
    });

    const handleStepOneNext = (credentials: WizardData["credentials"]) => {
        setWizardData({ ...wizardData, credentials });
        setStep(2);
    };

    const handleStepTwoNext = (selectedServices: string[], resources: ResourceDetail[]) => {
        setWizardData({
            ...wizardData,
            selectedServices,
            resources,
        });

        setStep(3);
    };

    const handleStepThreeComplete = (selectedInstances: string[]) => {
        setWizardData({ ...wizardData, selectedInstances });
        console.log("Wizard completed:", { ...wizardData, selectedInstances });
    };

    const handleBack = () => {
        setStep((prev) => (prev - 1) as 1 | 2 | 3);
    };

    return (
        <>
            {step === 1 && <StepOne onNext={handleStepOneNext} />}

            {step === 2 && (
                <StepTwo
                    credentials={wizardData.credentials}
                    onNext={handleStepTwoNext}
                    onBack={handleBack}
                />
            )}

            {step === 3 && (
                <StepThree
                    resources={wizardData.resources}
                    onComplete={handleStepThreeComplete}
                    onBack={handleBack}
                />
            )}
        </>
    );
}
