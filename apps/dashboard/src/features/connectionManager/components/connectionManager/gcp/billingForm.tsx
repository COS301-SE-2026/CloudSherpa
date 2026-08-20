import {
    Field,
    FieldDescription,
    FieldGroup,
    FieldLabel,
    FieldSet,
} from "@/components/atoms/field";
import { BillingFormContainer } from "../billingFormContainer";
import { Input } from "@/components/atoms/input";

interface GcpBillingFormProps {
    readonly optedInToBilling: boolean;
    readonly handleOptedInToBillingChange: (checked: boolean) => void;
    readonly billingId: string;
    readonly setBillingId: React.Dispatch<React.SetStateAction<string>>;
    readonly billingDataset: string;
    readonly setBillingDataset: React.Dispatch<React.SetStateAction<string>>;
}

export function GcpBillingForm({
    optedInToBilling,
    handleOptedInToBillingChange,
    billingId,
    setBillingId,
    billingDataset,
    setBillingDataset,
}: GcpBillingFormProps) {
    return (
        <BillingFormContainer
            optedInToBilling={optedInToBilling}
            handleOptedInToBillingChange={handleOptedInToBillingChange}
        >
            <FieldSet>
                <FieldGroup>
                    <Field>
                        handleOptedInToBillingChange
                        <FieldLabel htmlFor="billingId">Cloud Billing Account ID</FieldLabel>
                        <FieldDescription>
                            Enter the Google Cloud Billing account ID for this connection.
                        </FieldDescription>
                        <Input
                            id="billingId"
                            type="text"
                            value={billingId}
                            onChange={(e) => setBillingId(e.target.value)}
                            placeholder="000000-000000-000000-000000"
                        ></Input>
                    </Field>
                    <Field>
                        <FieldLabel htmlFor="dataset">Billing Export Dataset ID</FieldLabel>
                        <FieldDescription>
                            Enter the BigQuery dataset that contains the detailed billing export
                            tables.
                        </FieldDescription>
                        <Input
                            id="dataset"
                            type="text"
                            placeholder="example_dataset"
                            value={billingDataset}
                            onChange={(e) => setBillingDataset(e.target.value)}
                        ></Input>
                    </Field>
                </FieldGroup>
            </FieldSet>
        </BillingFormContainer>
    );
}
