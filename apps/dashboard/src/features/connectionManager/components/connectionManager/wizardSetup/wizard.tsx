"use client";

import { useState } from "react";

interface PropsForBaseWizard {
    eachStep: {
        forComponents: React.ComponentType<{
            onNext: (data: Record<string, unknown>) => void;
            onBack?: () => void;
            data?: Record<string, unknown>;
        }>;
        props?: Record<string, unknown>;
    }[];

    onComplete: (forData: Record<string, unknown>) => void;
    initialData?: Record<string, unknown>;
    getDataForStep?: (forStep: number, forData: Record<string, unknown>) => Record<string, unknown>;
}

export function BaseWizard({
    eachStep,
    onComplete,
    initialData = {},
    getDataForStep,
}: Readonly<PropsForBaseWizard>) {
    const [step, setStep] = useState(0);

    const [dataForWizard, setDataForWizard] = useState(initialData);

    const [forLoading, setForLoading] = useState(false);

    const handlingNext = async (forData: Record<string, unknown>) => {
        if (forLoading) {
            return;
        }

        setForLoading(true);

        const processingData = getDataForStep ? getDataForStep(step, forData) : forData;
        const newData = { ...dataForWizard, ...processingData };

        setDataForWizard(newData);

        if (step === eachStep.length - 1) {
            onComplete(newData);
        } else {
            setStep(step + 1);
        }

        setForLoading(false);
    };

    const handlingBack = () => {
        setStep(step - 1);
    };

    const ForCurrentStep = eachStep[step].forComponents;

    return (
        <ForCurrentStep
            {...eachStep[step].props}
            onNext={handlingNext}
            onBack={step > 0 ? handlingBack : undefined}
            data={dataForWizard}
        />
    );
}
