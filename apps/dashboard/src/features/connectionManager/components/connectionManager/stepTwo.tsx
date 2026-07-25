"use client";
import React, { useState, useEffect } from "react";
import { Button } from "@/components/atoms/button";
import {
  CloudCredentials,
  ResourceDetail,
  AwsPolicy,
  getCloudServices,
  generateAwsPermissionsPolicy,
  getCloudResources,
} from "@/lib/fetch/cloud-resource-api";

export interface BillingConfig {
  prefix: string;
  bucketName: string;
  exportName: string;
}

interface PropsForStepTwo {
  credentials: CloudCredentials | null;
  onNext: (
    selectedServices: string[],
    resources: ResourceDetail[],
    billingConfig: BillingConfig
  ) => void;
  onBack: () => void;
}

export default function StepTwo({ credentials, onNext, onBack }: Readonly<PropsForStepTwo>) {
  const [availableServices, setAvailableServices] = useState<{ id: string; name: string }[]>([]);
  const [servicesSelected, setServicesSelected] = useState<string[]>([]);
  const [permissions, setPermissions] = useState<AwsPolicy | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [prefix, setPrefix] = useState("");
  const [bucketName, setBucketName] = useState("");
  const [exportName, setExportName] = useState("");
  const [savedBillingConfig, setSavedBillingConfig] = useState<BillingConfig | null>(null);

  const toggleService = (serviceId: string) => {
    setServicesSelected((prev) =>
      prev.includes(serviceId) ? prev.filter((id) => id !== serviceId) : [...prev, serviceId]
    );
  };

  useEffect(() => {
    const loadServices = async () => {
      const services = await getCloudServices("aws");

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

  const getExtendedPermissions = (): AwsPolicy | null => {
    const baseStatements = permissions?.Statement ?? [];
    const version = permissions?.Version ?? "2012-10-17";

    if (!savedBillingConfig && baseStatements.length === 0) {
      return null;
    }

    const targetBucket = (savedBillingConfig?.bucketName ?? "").trim() || "bucket-name";
    const cleanPrefix = (savedBillingConfig?.prefix ?? "").trim().replace(/^\/+|\/+$/g, "");
    const exportPath = cleanPrefix ? `${cleanPrefix}/*` : "*";

    const billingStatements = savedBillingConfig
      ? [
        {
          Sid: "AllowBucketListing",
          Effect: "Allow",
          Action: ["s3:ListBucket"],
          Resource: [`arn:aws:s3:::${targetBucket}`],
        },
        {
          Sid: "AllowExportGetObject",
          Effect: "Allow",
          Action: ["s3:GetObject"],
          Resource: [`arn:aws:s3:::${targetBucket}/${exportPath}`],
        },
      ]
      : [];

    return {
      Version: version,
      Statement: [...baseStatements, ...billingStatements],
    };
  };

  const displayPermissions = getExtendedPermissions();

  const handleSaveBillingConfig = () => {
    if (!prefix || !bucketName || !exportName) {
      setError("Please fill out all billing configuration fields before saving.");
      return;
    }

    setSavedBillingConfig({
      prefix: prefix.trim(),
      bucketName: bucketName.trim(),
      exportName: exportName.trim(),
    });
    setError("");
  };

  const handleSubmit = async (forHandlingSubmit: React.FormEvent<HTMLFormElement>) => {
    forHandlingSubmit.preventDefault();

    if (!prefix || !bucketName || !exportName) {
      setError("Please fill out all billing configuration fields.");
      return;
    }

    try {
      setLoading(true);
      setError("");

      const resources = await getCloudResources("aws", {
        accessKey: credentials?.accessKey,
        secretKey: credentials?.secretKey,
        awsRegion: credentials?.awsRegion,
      }, servicesSelected);

      if (resources.length === 0) {
        setError("No resources were discovered.");
        return;
      }
      setLoading(false);

      onNext(servicesSelected, resources, { prefix, bucketName, exportName });
    } catch (err) {
      console.error(err);

      setError("Failed to discover resources. Check credentials and permissions.");
    } finally {
      setLoading(false);
    }
  };

  const forHandlingAllSelected = () => {
    if (servicesSelected.length === availableServices.length) {
      setServicesSelected([]);
    } else {
      setServicesSelected(availableServices.map((s) => s.id));
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
            Configure Billing & Services
          </h2>
          {error && (
            <div className="mt-3 rounded-sm bg-destructive/5 p-3 text-destructive text-sm">
              {error}
            </div>
          )}
        </div>

        <form onSubmit={handleSubmit} className="space-y-8">
          <section className="rounded-lg border border-primary/30 bg-primary/5 p-4 space-y-4">
            <div className="flex flex-wrap items-center justify-between gap-2">
              <h3 className="text-foreground text-sm font-semibold uppercase tracking-wider">
                Billing Export Configuration
              </h3>
              <span className="rounded-full bg-primary/15 px-2 py-1 text-xs font-medium text-primary">
                Account-wide cost scope
              </span>
            </div>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div className="space-y-2">
                <label
                  htmlFor="bucketName"
                  className="text-sm font-medium text-foreground"
                >
                  S3 Bucket Name
                </label>
                <input
                  id="bucketName"
                  type="text"
                  value={bucketName}
                  onChange={(e) => setBucketName(e.target.value)}
                  placeholder="e.g., my-billing-reports-bucket"
                  className="w-full p-2 rounded-md border border-border bg-background text-foreground focus:outline-none focus:ring-2 focus:ring-primary"
                />
              </div>

              <div className="space-y-2">
                <label
                  htmlFor="exportName"
                  className="text-sm font-medium text-foreground"
                >
                  Export Name
                </label>
                <input
                  id="exportName"
                  type="text"
                  value={exportName}
                  onChange={(e) => setExportName(e.target.value)}
                  placeholder="e.g., daily-cost-export"
                  className="w-full p-2 rounded-md border border-border bg-background text-foreground focus:outline-none focus:ring-2 focus:ring-primary"
                />
              </div>

              <div className="space-y-2 md:col-span-2">
                <label
                  htmlFor="prefix"
                  className="text-sm font-medium text-foreground"
                >
                  Prefix / Path
                </label>
                <input
                  id="prefix"
                  type="text"
                  value={prefix}
                  onChange={(e) => setPrefix(e.target.value)}
                  placeholder="e.g., cur-reports/2023/"
                  className="w-full p-2 rounded-md border border-border bg-background text-foreground focus:outline-none focus:ring-2 focus:ring-primary"
                />
              </div>
            </div>

            <div className="flex items-center gap-3 pt-2">
              <Button
                type="button"
                onClick={handleSaveBillingConfig}
                className="bg-primary hover:bg-accent hover:text-accent-foreground text-primary-foreground px-4 py-2 rounded-md transition-all duration-200 font-medium"
              >
                Save Billing Config
              </Button>
              {savedBillingConfig && (
                <span className="text-xs text-emerald-700 bg-emerald-50 border border-emerald-200 rounded px-2 py-1">
                  Billing config saved. IAM policy now includes billing export
                  access.
                </span>
              )}
            </div>
          </section>

          <section className="rounded-lg border border-border bg-background p-4">
            <div className="flex flex-wrap items-center justify-between gap-2 mb-4">
              <h3 className="text-foreground text-sm font-semibold uppercase tracking-wider opacity-80">
                Services for Usage Monitoring & Resource Discovery
              </h3>

              <div className="flex items-center gap-3">
                <button
                  type="button"
                  onClick={forHandlingAllSelected}
                  className="text-primary hover:text-accent text-sm transition-colors"
                >
                  {servicesSelected.length === availableServices.length
                    ? "Deselect All"
                    : "Select All"}
                </button>
              </div>
            </div>

            <div className="mb-4 rounded-md border border-amber-300/60 bg-amber-50 p-3 text-sm text-amber-900">
              Billing ingestion is account-wide and not limited by selected services.
              Select services to discover resources and monitor usage metrics
              alongside billing trends.
            </div>

            <div className="space-y-3">
              {availableServices.map((service) => (
                <button
                  type="button"
                  key={service.id}
                  onClick={() => toggleService(service.id)}
                  className="flex items-start gap-3 p-4 bg-background rounded-lg border border-border hover:border-primary/40 transition-all cursor-pointer w-full"
                >
                  <input
                    type="checkbox"
                    checked={servicesSelected.includes(service.id)}
                    onChange={() => toggleService(service.id)}
                    className="mt-0.5 w-4 h-4 rounded border-border bg-background text-primary focus:ring-ring focus:ring-2"
                  />

                  <div className="flex-1">
                    <div className="flex items-center gap-2">
                      <span className="text-foreground font-medium">
                        {service.name}
                      </span>
                    </div>
                  </div>
                </button>
              ))}
            </div>
          </section>

          <div className="pt-4">
            <h3 className="text-foreground text-sm font-semibold uppercase tracking-wider opacity-60 mb-3">
              IAM Permissions for Discovery + Billing Export Access
            </h3>

            <div className="bg-background rounded-lg p-4 border border-border">
              <p className="text-foreground text-sm mb-3">
                Add this IAM policy to your user. It includes selected-service
                discovery permissions and billing export S3 read access.
              </p>
              <pre className="bg-card p-4 rounded-lg overflow-x-auto text-xs font-mono text-foreground whitespace-pre-wrap">
                {displayPermissions
                  ? JSON.stringify(displayPermissions, null, 2)
                  : "{}"}
              </pre>

              <button
                type="button"
                onClick={() => {
                  navigator.clipboard.writeText(
                    displayPermissions
                      ? JSON.stringify(displayPermissions, null, 2)
                      : "{}"
                  );
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
              disabled={loading}
              onClick={onBack}
              className="bg-primary hover:bg-accent hover:text-accent-foreground text-primary-foreground px-6 py-2 rounded-md transition-all duration-200 font-medium"
            >
              Back
            </Button>

            <Button
              type="submit"
              disabled={loading}
              className="bg-primary hover:bg-accent hover:text-accent-foreground text-primary-foreground px-6 py-2 rounded-md transition-all duration-200 font-medium"
            >
              Next
            </Button>
          </div>
        </form>
      </div>
    </div>
  );
};

