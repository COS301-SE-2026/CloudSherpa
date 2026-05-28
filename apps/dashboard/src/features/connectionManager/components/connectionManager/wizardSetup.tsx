'use client';
import { useState } from 'react';
import StepOne from './stepOne';
import StepTwo from './stepTwo';
import StepThree from './stepThree';

interface WizardData{
  credentials: {
    accessKeyId: string;
    secretAccessKey: string;
    region: string;
  } | null;
  selectedServices: string[];
  selectedInstances: string[];
}

export default function WizardSetup(){
  const [step, setStep] = useState<1 | 2 | 3>(1);

  const [wizardData, setWizardData] = useState<WizardData>({
    credentials: null,
    selectedServices: [],
    selectedInstances: [],
  });

  const handleStepOneNext = (credentials: WizardData['credentials']) => {
    setWizardData({ ...wizardData, credentials });
    setStep(2);
  };

  const handleStepTwoNext = (selectedServices: string[]) => {
    setWizardData({ ...wizardData, selectedServices });
    setStep(3);
  };

  const handleStepThreeComplete = (selectedInstances: string[]) => {
    setWizardData({ ...wizardData, selectedInstances });
    console.log('Wizard completed:', { ...wizardData, selectedInstances });
  };

  const handleBack = () => {
    setStep(prev => (prev-1) as 1 | 2 | 3);
  };

  return(
    <>
      {step === 1 && <StepOne onNext={handleStepOneNext} />}

      {step === 2 && (
        <StepTwo
          onNext={handleStepTwoNext}
          onBack={handleBack}
        />
      )}

      {step === 3 && (
        <StepThree
          selectedServices={wizardData.selectedServices}
          onComplete={handleStepThreeComplete}
          onBack={handleBack}
        />
      )}
      
    </>
  );
}