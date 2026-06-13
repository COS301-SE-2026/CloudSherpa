'use client';
import React, { useState, useEffect } from 'react';
import { Button } from '@/components/atoms/button';
import {
  CloudCredentials,
  ResourceDetail,
  AwsPolicy, getCloudServices,
  generateAwsPermissionsPolicy,
  getCloudResources
} from '@/lib/fetch/cloud-resource-api';

interface PropsForStepTwo {
  credentials: CloudCredentials | null;

  onNext: (
    selectedServices: string[],
    resources: ResourceDetail[]
  ) => void;

  onBack: () => void;
}

export default function StepTwo({ credentials, onNext, onBack }: Readonly<PropsForStepTwo>) {
  const [availableServices, setAvailableServices] = useState<
    { id: string; name: string }[]
  >([]);
  const [servicesSelected, setSelectedServices] = useState<string[]>([]);
  const [permissions, setPermissions] = useState<AwsPolicy | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');


  const toggleService = (serviceId: string) => {
    setSelectedServices(prev =>
      prev.includes(serviceId)
        ? prev.filter(id => id !== serviceId)
        : [...prev, serviceId]
    );
  };

  useEffect(() => {
    const loadServices = async () => {
      const services = await getCloudServices('aws');

      setAvailableServices(
        services.map((s) => ({
          id: s,
          name: s.toUpperCase(),
        }))
      );
    };

    loadServices();
  }, []);

  useEffect(() => {
    const fetchPermissions = async () => {
      if (servicesSelected.length === 0) {
        setPermissions(null);
        return;
      }

      const result = await generateAwsPermissionsPolicy(servicesSelected);
      setPermissions(result);
    };

    fetchPermissions();
  }, [servicesSelected]);

  const handleSubmit = async (
    forHandlingSubmit: React.SubmitEvent<HTMLFormElement>
  ) => {
    forHandlingSubmit.preventDefault();

    try {
      setLoading(true);
      setError('');

      const resources = await getCloudResources(
        'aws',
        {
          accessKey: credentials?.accessKey,
          secretKey: credentials?.secretKey,
          awsRegion: credentials?.awsRegion,
        }
      );

      if (resources.length === 0) {
        setError('No resources were discovered.');
        return;
      }

      onNext(servicesSelected, resources);
    } catch (err) {
      console.error(err);

      setError(
        'Failed to discover resources. Check credentials and permissions.'
      );
    } finally {
      setLoading(false);
    }
  };

  const forHandlingAllSelected = () => {
    if (servicesSelected.length === availableServices.length) {
      setSelectedServices([]);
    } else {
      setSelectedServices(availableServices.map(s => s.id));
    }
  };

  return (
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

          <div className="pt-4">

            <h3 className="text-foreground text-sm font-semibold uppercase tracking-wider opacity-60 mb-3">
              Grant IAM permissions
            </h3>

            <div className="bg-background rounded-lg p-4 border border-border">
              <p className="text-foreground text-sm mb-3">
                Please add the following permissions to the newly created IAM user:
              </p>
              <pre className="bg-card p-4 rounded-lg overflow-x-auto text-xs font-mono text-foreground whitespace-pre-wrap">
                {permissions
                  ? JSON.stringify(permissions, null, 2)
                  : '{}'}
              </pre>

              <button
                type="button"
                onClick={() => {
                  navigator.clipboard.writeText(permissions
                    ? JSON.stringify(permissions, null, 2)
                    : '{}');
                }}
                className="mt-3 text-primary hover:text-accent text-sm transition-colors"
              >
                Copy to clipboard
              </button>

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
