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
}

export function GcpBillingForm({
    optedInToBilling,
    handleOptedInToBillingChange,
}: GcpBillingFormProps) {
    return (
        <BillingFormContainer
            optedInToBilling={optedInToBilling}
            handleOptedInToBillingChange={handleOptedInToBillingChange}
        >
            <FieldSet>
                <FieldGroup>
                    <Field>
                        <FieldLabel htmlFor="billingId">Cloud Billing Account ID</FieldLabel>
                        <FieldDescription>
                            Enter the Google Cloud Billing account ID for this connection.
                        </FieldDescription>
                        <Input
                            id="billingId"
                            type="text"
                            placeholder="000000-000000-000000-000000"
                        ></Input>
                    </Field>
                    <Field>
                        <FieldLabel htmlFor="dataset">Billing Export Dataset ID</FieldLabel>
                        <FieldDescription>
                            Enter the BigQuery dataset that contains the detailed billing export
                            tables.
                        </FieldDescription>
                        <Input id="dataset" type="text" placeholder="example_dataset"></Input>
                    </Field>
                </FieldGroup>
            </FieldSet>
        </BillingFormContainer>
    );
}
