"use client";

import {useState} from "react";

interface PropsForBaseWizard{
    eachStep : {
        forComponents : React.ComponentType<any>;
        props?: any;
    }[];

    onComplete : (forData : any) => void;
    initialData?: any;
    getDataForStep?: (forStep : number, forData : any) => any;
}

export function BaseWizard({
    eachStep, onComplete, initialData = {}, getDataForStep
} : Readonly<PropsForBaseWizard>){
    const [step, setStep] = useState(0);

    const [dataForWizard, setDataForWizard] = useState(initialData);

    const [forLoading, setForLoading] = useState(false);

    const handlingNext = async (forData : any) => {
        if(forLoading){
            return;
        }

        setForLoading(true);

        const processingData = getDataForStep ? await getDataForStep(step, forData) : forData;
        const newData = {...dataForWizard, ...processingData};

        setDataForWizard(newData);

        if(step === eachStep.length-1){
            onComplete(newData);
        } else {
            setStep(step+1);
        }

        setForLoading(false);
    };

    const handlingBack = () => {
        setStep(step-1);
    };

    const ForCurrentStep = eachStep[step].forComponents;

    return(
        <ForCurrentStep
            {...eachStep[step].props}
            onNext = {handlingNext}
            onBack = {step>0 ? handlingBack : undefined}
            data = {dataForWizard}
        />
    );
}