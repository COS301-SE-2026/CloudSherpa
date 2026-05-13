'use client';
import { useState } from 'react';
import { Button } from '@/components/atoms/button';

interface PropsForStepTwo{
  onNext: (selectedServices: string[]) => void;
  onBack: () => void;
}

const availableServices = [
  { id: 'ec2', name: 'EC2'}, { id: 'rds', name: 'RDS'}, { id: 'lambda', name: 'Lambda'}, { id: 'ecs', name: 'ECS'}, { id: 'eks', name: 'EKS'},
];

export default function StepTwo({ onNext, onBack }: PropsForStepTwo){
    
  const [servicesSelected, setSelectedServices] = useState<string[]>([]);

  const toggleService = (serviceId: string) => {
    setSelectedServices(prev =>
      prev.includes(serviceId)
        ? prev.filter(id => id !== serviceId)
        : [...prev, serviceId]
    );
  };

  const handleSubmit = (forHandlingSubmit: React.FormEvent) => {
    forHandlingSubmit.preventDefault();
    onNext(servicesSelected);
  };

  const forHandlingAllSelected = () => {
    if(servicesSelected.length === availableServices.length){
      setSelectedServices([]);
    } else{
      setSelectedServices(availableServices.map(s => s.id));
    }
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
          <div>
            <div className="flex justify-between items-center mb-4">

              <h3 className="text-foreground text-sm font-semibold uppercase tracking-wider opacity-60">
                SERVICES WE OFFER
              </h3>

              <button
                type="button"
                onClick={forHandlingAllSelected}
                className="text-primary hover:text-accent text-sm transition-colors"
              >
                {servicesSelected.length === availableServices.length ? 'Deselect All' : 'Select All'}
              </button>

            </div>

            <div className="space-y-3">
              {availableServices.map((service) => (
                <div
                  key={service.id}
                  onClick={() => toggleService(service.id)}
                  className="flex items-start gap-3 p-4 bg-background rounded-lg border border-border hover:border-primary/40 transition-all cursor-pointer"
                >

                  <input
                    type="checkbox"
                    checked={servicesSelected.includes(service.id)}
                    onChange={() => toggleService(service.id)}
                    className="mt-0.5 w-4 h-4 rounded border-border bg-background text-primary focus:ring-ring focus:ring-2"
                  />

                  <div className="flex-1">
                    <div className="flex items-center gap-2">
                      <span className="text-foreground font-medium">{service.name}</span>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
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