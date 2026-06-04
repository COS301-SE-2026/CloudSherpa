'use client';
import { useState } from 'react';
import { Button } from '@/components/atoms/button';
import { Checkbox } from '@/components/atoms/checkbox';
import { Badge } from '@/components/atoms/badge';
import { ResourceDetail } from '@/lib/fetch/cloud-resource-api';
import React from 'react';

interface PropsForStepThree {
  resources: ResourceDetail[];
  onComplete: (selectedInstances: string[]) => void;
  onBack: () => void;
}

export default function StepThree({ resources, onComplete, onBack }: Readonly<PropsForStepThree>) {
  const handleSubmit = (forHandlingSubmit: React.SubmitEvent<HTMLFormElement>) => {
    forHandlingSubmit.preventDefault();
    onComplete(selectedResources);
  };

  const [selectedResources, setSelectedResources] = useState<string[]>(
    resources.map(resource => resource.resourceId)
  );

  const groupedResources = resources.reduce(
    (groups, resource) => {
      const category = resource.serviceCategory;

      if (!groups[category]) {
        groups[category] = [];
      }

      groups[category].push(resource);

      return groups;
    },
    {} as Record<string, ResourceDetail[]>
  );

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
            <div className="space-y-8">
              {Object.entries(groupedResources).map(
                ([serviceCategory, categoryResources]) => (
                  <div key={serviceCategory}>
                    <h3 className="text-lg font-semibold text-foreground mb-4">
                      {serviceCategory}
                    </h3>

                    <div className="space-y-3">
                      {categoryResources.map(resource => (
                        <div
                          key={resource.resourceId}
                          className="
                flex
                items-start
                justify-between
                gap-4
                p-4
                bg-background
                rounded-lg
                border
                border-border
                hover:border-primary/40
                transition-all
                cursor-pointer
              "
                        >
                          <div className="flex items-start gap-3">
                            <Checkbox
                              checked={selectedResources.includes(resource.resourceId)}
                              onCheckedChange={(checked) => {
                                setSelectedResources(prev => {
                                  if (checked) {
                                    return prev.includes(resource.resourceId)
                                      ? prev
                                      : [...prev, resource.resourceId];
                                  }
                                  return prev.filter(id => id !== resource.resourceId);
                                });
                              }}
                            />
                            <div>
                              <div className="font-medium text-foreground">
                                {resource.name}

                                <span className="ml-2 text-muted-foreground">
                                  ({resource.resourceId})
                                </span>
                              </div>
                            </div>
                          </div>

                          <div className="flex flex-wrap justify-end gap-2 max-w-md">
                            {Object.entries(
                              resource.tags as Record<string, string>
                            ).map(([key, value]) => (
                              <Badge
                                key={`${key}-${value}`}
                                variant="secondary"
                              >
                                {key}: {value}
                              </Badge>
                            ))}
                          </div>
                        </div>
                      ))}
                    </div>
                  </div>
                )
              )}
            </div>
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
