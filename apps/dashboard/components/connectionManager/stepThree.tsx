'use client';
import { Button } from '@/components/atoms/button';

interface PropsForStepThree {
  selectedServices: string[];
  onComplete: (selectedInstances: string[]) => void;
  onBack: () => void;
}

export default function StepThree({ selectedServices, onComplete, onBack }: PropsForStepThree) {
  const handleSubmit = (forHandlingSubmit: React.FormEvent) => {
    forHandlingSubmit.preventDefault();
    onComplete([]);
  };

  return (
    <div className="min-h-screen bg-background flex items-center justify-center p-8">
      <div className="w-full max-w-4xl bg-card rounded-lg shadow-none p-8">
        <div className="pb-6">
          <div className="flex items-center gap-2 mb-4">
            <div className="w-2 h-2 rounded-full bg-primary" />
            <span className="text-sm font-medium text-muted-foreground/70">
              STEP 3 OF 3
            </span>
          </div>

          <h2 className="text-2xl font-semibold tracking-tight text-foreground">
            Select Instances
          </h2>

          <p className="mt-2 text-muted-foreground/70">
            Select the instances you want CloudSherpa to monitor.
          </p>
        </div>

        <form onSubmit={handleSubmit} className="space-y-8">
          <div className="min-h-[200px]">
            {/*the instances will be populated once they are fetched hence step 3 will be blank for nwo*/}
          </div>

          <div className="flex justify-between pt-6">
            <Button
              type="button"
              onClick={onBack}
              className="bg-primary hover:bg-accent hover:text-accent-foreground text-primary-foreground px-6 py-2 rounded-md transition-all duration-200 font-medium"
            >
              Back
            </Button>

            <Button
              type="submit"
              className="bg-primary hover:bg-accent hover:text-accent-foreground text-primary-foreground px-8 py-2 rounded-md transition-all duration-200 font-medium"
            >
              Finish
            </Button>
          </div>
        </form>
      </div>
    </div>
  );
}