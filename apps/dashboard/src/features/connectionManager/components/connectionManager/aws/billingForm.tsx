"use client";

import { Button } from "@/components/atoms/button";
import { BillingConfig } from "./stepTwo";
import { Label } from "@/components/atoms/label";
import { BillingFormContainer } from "../billingFormContainer";

interface BillingFormProps {
    readonly bucketName: string;
    readonly setBucketName: React.Dispatch<React.SetStateAction<string>>;
    readonly bucketRegion: string;
    readonly setBucketRegion: React.Dispatch<React.SetStateAction<string>>;
    readonly exportName: string;
    readonly setExportName: React.Dispatch<React.SetStateAction<string>>;
    readonly prefix: string;
    readonly setPrefix: React.Dispatch<React.SetStateAction<string>>;
    readonly savedBillingConfig?: BillingConfig;
    readonly handleSaveBillingConfig: () => void;
    readonly optedInToBilling: boolean;
    readonly handleOptedInToBillingChange: (checked: boolean) => void;
}

const regions = [
    "us-east-1",
    "us-east-2",
    "us-west-1",
    "us-west-2",
    "af-south-1",
    "ap-south-1",
    "ap-southeast-1",
    "ap-southeast-2",
    "ap-northeast-1",
    "eu-west-1",
    "eu-north-1",
    "eu-central-1",
    "sa-east-1",
];

export function BillingForm({
    bucketName,
    setBucketName,
    bucketRegion,
    setBucketRegion,
    exportName,
    setExportName,
    prefix,
    setPrefix,
    savedBillingConfig,
    handleSaveBillingConfig,
    optedInToBilling,
    handleOptedInToBillingChange,
}: BillingFormProps) {
    return (
        <BillingFormContainer
            optedInToBilling={optedInToBilling}
            handleOptedInToBillingChange={handleOptedInToBillingChange}
        >
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="space-y-2">
                    <label htmlFor="bucketName" className="text-sm font-medium text-foreground">
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
                    <Label htmlFor="region" className="text-foreground text-sm font-medium">
                        Bucket region
                    </Label>

                    <select
                        id="region"
                        value={bucketRegion}
                        onChange={(e) => setBucketRegion(e.target.value)}
                        className="w-full bg-background border-border rounded-md px-4 py-3 text-foreground focus:outline-none focus:ring-2 focus:ring-ring focus:border-transparent transition-all cursor-pointer"
                    >
                        <option value="" className="bg-card">
                            Select a region
                        </option>
                        {regions.map((region) => (
                            <option key={region} value={region} className="bg-card">
                                {region}
                            </option>
                        ))}
                    </select>
                </div>

                <div className="space-y-2">
                    <label htmlFor="exportName" className="text-sm font-medium text-foreground">
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
                    <label htmlFor="prefix" className="text-sm font-medium text-foreground">
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
                        Billing config saved. IAM policy now includes billing export access.
                    </span>
                )}
            </div>
        </BillingFormContainer>
    );
}
