"use client";

import { Button } from "@/components/atoms/button";
import React, { useState } from "react";
import { BillingConfig } from "./stepTwo";
import { Checkbox } from "@/components/atoms/checkbox";
import { Field } from "@/components/atoms/field";
import { Label } from "@/components/atoms/label";

interface BillingFormProps {
    readonly bucketName: string;
    readonly setBucketName: React.Dispatch<React.SetStateAction<string>>;
    readonly exportName: string;
    readonly setExportName: React.Dispatch<React.SetStateAction<string>>;
    readonly prefix: string;
    readonly setPrefix: React.Dispatch<React.SetStateAction<string>>;
    readonly savedBillingConfig?: BillingConfig;
    readonly handleSaveBillingConfig: () => void;
    readonly optedInToBilling: boolean;
    readonly handleOptedInToBillingChange: (checked: boolean) => void;
}

export function BillingForm({
    bucketName,
    setBucketName,
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
        <>
            <Field orientation={"horizontal"}>
                <Checkbox
                    checked={optedInToBilling}
                    onCheckedChange={(checked) => handleOptedInToBillingChange(checked === true)}
                />
                <Label>Opt-in to billing</Label>
            </Field>
            {optedInToBilling && (
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
                </section>
            )}
        </>
    );
}
