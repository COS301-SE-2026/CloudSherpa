'use client';
import { useState } from 'react';
import { Button } from '@/components/atoms/button';

interface PropsForStepTwo{
  onNext: (selectedServices: string[]) => void;
  onBack: () => void;
}

export default function StepTwo({ onNext, onBack }: PropsForStepTwo){
    
  const [servicesSelected, setSelectedServices] = useState<string[]>([]);

  const handleSubmit = (forHandlingSubmit: React.FormEvent) => {
    forHandlingSubmit.preventDefault();
    onNext(servicesSelected);
  };

  return(
    <div className="min-h-screen bg-background flex items-center justify-center p-8">
      <div className="w-full max-w-3xl bg-card rounded-lg shadow-none p-8">
        <div className="pb-6">
          <div className="flex items-center gap-2 mb-4">
            <div className="w-2 h-2 rounded-full bg-primary" />

            <span className="text-sm font-medium text-muted-foreground/70">
              STEP 2 OF 3
            </span>

          </div>

          <h2 className="text-2xl font-semibold tracking-tight text-foreground">
            Select Services
          </h2>

          <p className="mt-2 text-muted-foreground/70">
            Choose which AWS services you want to monitor.
          </p>

        </div>

        <form onSubmit={handleSubmit} className="space-y-6">
          <div className="flex justify-between pt-4">
            <Button
              type="button"
              onClick={onBack}
              className="bg-primary hover:bg-accent hover:text-accent-foreground text-primary-foreground px-6 py-2 rounded-md transition-all duration-200 font-medium"
            >
              Back
            </Button>

            <Button
              type="submit"
              className="bg-primary hover:bg-accent hover:text-accent-foreground text-primary-foreground px-6 py-2 rounded-md transition-all duration-200 font-medium"
            >
              Next
            </Button>

          </div>
        </form>
      </div>
    </div>
  );
}