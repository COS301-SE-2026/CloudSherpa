"use client";
import { useState } from "react";
import StepOneAws from "./stepOne";
import StepTwoAws from "./stepTwo";
import StepThreeAws from "./stepThree";
import { ResourceDetail } from "@/lib/fetch/dto/cloud-resource";

interface BillingConfig {
    prefix: string;
    bucketName: string;
    bucketRegion: string;
    exportName: string;
}

interface WizardData {
    credentials: {
        accessKeyId: string;
        secretAccessKey: string;
        awsRegion: string;
    } | null;
    displayName: string;
    ingestionPeriod: string;
    selectedServices: string[];
    selectedInstances: string[];
    resources: ResourceDetail[];
    billingConfig: BillingConfig | null;
}

export default function WizardSetup() {
    const [step, setStep] = useState<1 | 2 | 3>(1);

    const [wizardData, setWizardData] = useState<WizardData>({
        credentials: null,
        displayName: "AWS Connection",
        ingestionPeriod: "60",
        selectedServices: [],
        selectedInstances: [],
        resources: [],
        billingConfig: null,
    });

    const handleStepOneNext = (data: {
        displayName: string;
        accessKey: string;
        secretKey: string;
        awsRegion: string;
    }) => {
        setWizardData({
            ...wizardData,
            displayName: data.displayName,
            credentials: {
                accessKeyId: data.accessKey,
                secretAccessKey: data.secretKey,
                awsRegion: data.awsRegion,
            },
        });

        setStep(2);
    };
    const handleStepTwoNext = (
        selectedServices: string[],
        resources: ResourceDetail[],
        billingConfig: BillingConfig
    ) => {
        setWizardData({
            ...wizardData,
            selectedServices,
            resources,
            billingConfig,
        });

        setStep(3);
    };

    const handleStepThreeComplete = (ingestionPeriod: string) => {
        setWizardData({
            ...wizardData,
            ingestionPeriod,
        });

        console.log("Wizard completed:", {
            ...wizardData,
            ingestionPeriod,
        });
    };

    const handleBack = () => {
        setStep((prev) => (prev - 1) as 1 | 2 | 3);
    };

    return (
        <>
            {step === 1 && <StepOneAws onNext={handleStepOneNext} />}

            {step === 2 && (
                <StepTwoAws
                    credentials={wizardData.credentials}
                    onNext={handleStepTwoNext}
                    onBack={handleBack}
                />
            )}

            {step === 3 && (
                <StepThreeAws
                    displayName={wizardData.displayName}
                    ingestionPeriod={""}
                    credentials={{
                        accessKeyId: wizardData.credentials!.accessKeyId,
                        secretAccessKey: wizardData.credentials!.secretAccessKey,
                    }}
                    resources={wizardData.resources}
                    billingConfig={wizardData.billingConfig!}
                    onComplete={handleStepThreeComplete}
                    onBack={handleBack}
                />
            )}
        </>
    );
}
