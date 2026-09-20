"use client";

import { Button } from "@/components/atoms/button";
import { BillingConfig } from "./stepTwo";
import { Input } from "@/components/atoms/input";
import { BillingFormContainer } from "../billingFormContainer";
import { Dropdown } from "@/components/atoms/dropdown";
import {
    Field,
    FieldDescription,
    FieldGroup,
    FieldLabel,
    FieldSet,
} from "@/components/atoms/field";

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

const regionOptions = regions.map((region) => ({
    label: region,
    value: region,
}));

export function AwsBillingForm({
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
            <FieldSet>
                <FieldGroup>
                    <Field>
                        <FieldLabel
                            htmlFor="bucketName"
                            className="text-sm font-medium text-foreground"
                        >
                            S3 Bucket Name
                        </FieldLabel>
                        <FieldDescription>
                            Enter the name of the S3 bucket where your AWS cost and usage reports
                            are delivered.
                        </FieldDescription>
                        <Input
                            id="bucketName"
                            type="text"
                            value={bucketName}
                            onChange={(e) => setBucketName(e.target.value)}
                            placeholder="e.g., my-billing-reports-bucket"
                        />
                    </Field>
                    <Field>
                        <FieldLabel
                            htmlFor="region"
                            className="text-foreground text-sm font-medium"
                        >
                            Bucket region
                        </FieldLabel>
                        <FieldDescription>
                            Select the AWS region where your S3 bucket is hosted.
                        </FieldDescription>
                        <Dropdown
                            options={regionOptions}
                            value={bucketRegion}
                            onChange={setBucketRegion}
                            placeholder="Select a region"
                            labelKey="label"
                            valueKey="value"
                            className="w-full"
                        />
                    </Field>
                    <Field>
                        <FieldLabel
                            htmlFor="exportName"
                            className="text-sm font-medium text-foreground"
                        >
                            Export Name
                        </FieldLabel>
                        <FieldDescription>
                            Enter the name of the data export configured in AWS Billing.
                        </FieldDescription>
                        <Input
                            id="exportName"
                            type="text"
                            value={exportName}
                            onChange={(e) => setExportName(e.target.value)}
                            placeholder="e.g., daily-cost-export"
                        />
                    </Field>
                    <Field>
                        <FieldLabel
                            htmlFor="prefix"
                            className="text-sm font-medium text-foreground"
                        >
                            Prefix / Path
                        </FieldLabel>
                        <FieldDescription>
                            Enter the report path prefix in your S3 bucket.
                        </FieldDescription>
                        <Input
                            id="prefix"
                            type="text"
                            value={prefix}
                            onChange={(e) => setPrefix(e.target.value)}
                            placeholder="e.g., cur-reports/2023/"
                        />
                    </Field>
                    <Field></Field>
                </FieldGroup>
            </FieldSet>

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
