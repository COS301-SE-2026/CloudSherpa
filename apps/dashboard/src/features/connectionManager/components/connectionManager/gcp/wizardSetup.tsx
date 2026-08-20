"use client";

import { useState } from "react";
import StepOneGcp from "./stepOne";
import StepTwoGcp from "./stepTwo";
import StepThreeGcp from "./stepThree";
import { ResourceDetail } from "@/lib/fetch/cloud-resource-api";
import { GcpCredentialsDto } from "@/lib/fetch/gcp-connection-api";
import type { GcpBillingConfigType } from "./validTypes";

interface WizardData {
    credentials: GcpCredentialsDto | null;
    displayName: string;
    ingestionPeriod: number;
    servicesSelected: string[];
    resources: ResourceDetail[];
    billingConfig: GcpBillingConfigType | null;
}

export default function WizardSetupGcp() {
    const [step, setStep] = useState<1 | 2 | 3>(1);

    const [wizardData, setWizardData] = useState<WizardData>({
        credentials: null,
        displayName: "GCP Connection",
        ingestionPeriod: 60,
        servicesSelected: [],
        resources: [],
        billingConfig: null,
    });

    const handleStepOneNext = (data: { displayName: string; credentials: GcpCredentialsDto }) => {
        setWizardData((previous) => ({
            ...previous,
            displayName: data.displayName,
            credentials: data.credentials,
        }));

        setStep(2);
    };

    const handleStepTwoNext = (data: {
        displayName: string;
        servicesSelected: string[];
        resources: ResourceDetail[];
        credentials: GcpCredentialsDto;
        billingConfig: GcpBillingConfigType | null;
    }) => {
        setWizardData((previous) => ({
            ...previous,
            displayName: data.displayName,
            servicesSelected: data.servicesSelected,
            resources: data.resources,
            credentials: data.credentials,
            billingConfig: data.billingConfig,
        }));

        setStep(3);
    };

    const handleStepThreeComplete = (ingestionPeriod: number) => {
        setWizardData((previous) => ({
            ...previous,
            ingestionPeriod,
        }));

        console.log("GCP wizard completed:", {
            ...wizardData,
            ingestionPeriod,
        });
    };

    const handleBack = () => {
        setStep((previous) => (previous - 1) as 1 | 2 | 3);
    };

    return (
        <>
            {step === 1 && <StepOneGcp onNext={handleStepOneNext} />}

            {step === 2 && wizardData.credentials && (
                <StepTwoGcp
                    displayName={wizardData.displayName}
                    credentials={wizardData.credentials}
                    onNext={handleStepTwoNext}
                    onBack={handleBack}
                />
            )}

            {step === 3 && wizardData.credentials && (
                <StepThreeGcp
                    displayName={wizardData.displayName}
                    ingestionPeriod={wizardData.ingestionPeriod}
                    credentials={wizardData.credentials}
                    resources={wizardData.resources}
                    onComplete={handleStepThreeComplete}
                    onBack={handleBack}
                />
            )}
        </>
    );
}
