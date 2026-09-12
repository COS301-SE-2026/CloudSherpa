import {
    Field,
    FieldDescription,
    FieldGroup,
    FieldLabel,
    FieldSet,
} from "@/components/atoms/field";
import { BillingFormContainer } from "../billingFormContainer";
import { Input } from "@/components/atoms/input";

interface AzureBillingFormProps {
    readonly optedInToBilling: boolean;
    readonly handleOptedInToBillingChange: (checked: boolean) => void;
}

export function AzureBillingForm({
    optedInToBilling,
    handleOptedInToBillingChange,
}: AzureBillingFormProps) {
    return (
        <BillingFormContainer
            optedInToBilling={optedInToBilling}
            handleOptedInToBillingChange={handleOptedInToBillingChange}
        >
            <FieldSet>
                <FieldGroup>
                    <Field>
                        <FieldLabel htmlFor="storageAccount">Storage account name</FieldLabel>
                        <FieldDescription>
                            The storage account where Azure saves your cost exports. Enter the
                            account name, not its URL.
                        </FieldDescription>
                        <Input
                            id="storageAccount"
                            type="text"
                            placeholder="e.g. companybilling"
                        ></Input>
                    </Field>
                    <Field>
                        <FieldLabel htmlFor="storageContainer">Blob container name</FieldLabel>
                        <FieldDescription>
                            The container in that storage account that holds your cost exports.
                        </FieldDescription>
                        <Input
                            id="storageContainer"
                            type="text"
                            placeholder="e.g. cost-exports"
                        ></Input>
                    </Field>
                    <Field>
                        <FieldLabel htmlFor="exportDirectory">Export directory</FieldLabel>
                        <FieldDescription>
                            The directory configured for your cost export, relative to the
                            container. Exclude the export name.
                        </FieldDescription>
                        <Input
                            id="exportDirectory"
                            type="text"
                            placeholder="e.g. exports/daily"
                        ></Input>
                    </Field>
                    <Field>
                        <FieldLabel htmlFor="exportName">Export name</FieldLabel>
                        <FieldDescription>
                            The exact name of the export you created in Azure Cost Management.
                        </FieldDescription>
                        <Input
                            id="storageContainer"
                            type="text"
                            placeholder="e.g. daily-cost-export"
                        ></Input>
                    </Field>
                </FieldGroup>
            </FieldSet>
        </BillingFormContainer>
    );
}
