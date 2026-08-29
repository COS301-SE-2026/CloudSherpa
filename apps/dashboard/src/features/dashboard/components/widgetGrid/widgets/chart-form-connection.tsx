"use client";
import { useState, useEffect } from "react";
import { ChartWidgetConfig } from "@/features/dashboard/types/widgets";
import { Label } from "@/components/atoms/label";
import { FieldSet, FieldLegend, FieldDescription, FieldGroup } from "@/components/atoms/field";
import { FormCountCircle } from "@/components/atoms/form-count-circle";
import { getAwsAccountConnections, CloudAccount } from "@/lib/fetch/aws-connection-api";
import Dropdown from "@/components/molecules/dropdown";

const PROVIDERS = ["AWS", "AZURE", "GCP"];

const PROVIDER_MAP: Record<string, string> = {
    AWS: "AWS_ACCOUNT",
    AZURE: "AZURE_SUBSCRIPTION",
    GCP: "GCP_PROJECT",
};

interface ChartFormConnectionProps {
    configuration: ChartWidgetConfig;
    setConfiguration: (config: ChartWidgetConfig) => void;
}

export default function ChartFormConnection({
    configuration,
    setConfiguration,
}: Readonly<ChartFormConnectionProps>) {
    //fetch connections
    const [connections, setConnections] = useState<CloudAccount[]>([]);

    useEffect(() => {
        if (configuration.provider) {
            getAwsAccountConnections()
                .then((retrievedConnections) => {
                    const targetType = PROVIDER_MAP[configuration.provider!];
                    const filtered = retrievedConnections.filter(
                        (conn) => (conn.accountType || "").toUpperCase() === targetType
                    );
                    setConnections(filtered);
                })
                .catch(console.error);
        }
    }, [configuration.provider]);

    return (
        <FieldSet>
            <div className="flex flex-row items-center gap-3">
                <FormCountCircle count={2} />
                <FieldLegend className="mb-0">Connection</FieldLegend>
            </div>
            <FieldDescription>
                Choose the connection for the resource to be displayed on the chart.
            </FieldDescription>
            <FieldGroup>
                <div className="grid gap-2">
                    <Label>Provider</Label>
                    <Dropdown
                        value={configuration.provider}
                        options={PROVIDERS.map((provider) => ({
                            value: provider,
                            label: provider,
                        }))}
                        onSelect={(currentValue) => {
                            const provider = currentValue.toUpperCase();

                            setConfiguration({
                                ...configuration,
                                provider,
                                accountId: null,
                                resourceId: null,
                                metricType: null,
                            });
                        }}
                        disableSearch={true}
                        widthVariant="full"
                        placeholder="select provider..."
                    />
                </div>
                <div className="grid gap-2">
                    <Label>Connection</Label>
                    <Dropdown
                        value={configuration.accountId}
                        options={connections.map((connection) => ({
                            value: connection.id,
                            label: connection.displayName,
                        }))}
                        onSelect={(currentValue) => {
                            setConfiguration({
                                ...configuration,
                                accountId: currentValue,
                                resourceId: null,
                                metricType: null,
                            });
                        }}
                        widthVariant="full"
                        placeholder="select connection..."
                        emptyMessage="No connections found"
                    />
                </div>
            </FieldGroup>
        </FieldSet>
    );
}
