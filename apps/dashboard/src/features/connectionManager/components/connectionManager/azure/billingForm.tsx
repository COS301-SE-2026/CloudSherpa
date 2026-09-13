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
    readonly storageAccountName: string;
    readonly setStorageAccountName: React.Dispatch<React.SetStateAction<string>>;
    readonly blobContainerName: string;
    readonly setBlobContainerName: React.Dispatch<React.SetStateAction<string>>;
    readonly exportDirectory: string;
    readonly setExportDirectory: React.Dispatch<React.SetStateAction<string>>;
    readonly exportName: string;
    readonly setExportName: React.Dispatch<React.SetStateAction<string>>;
}

export function AzureBillingForm({
    optedInToBilling,
    handleOptedInToBillingChange,
    storageAccountName,
    setStorageAccountName,
    blobContainerName,
    setBlobContainerName,
    exportDirectory,
    setExportDirectory,
    exportName,
    setExportName,
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
                            value={storageAccountName}
                            onChange={(e) => setStorageAccountName(e.target.value)}
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
                            value={blobContainerName}
                            onChange={(e) => setBlobContainerName(e.target.value)}
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
                            value={exportDirectory}
                            onChange={(e) => setExportDirectory(e.target.value)}
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
                            value={exportName}
                            onChange={(e) => setExportName(e.target.value)}
                        ></Input>
                    </Field>
                </FieldGroup>
            </FieldSet>
        </BillingFormContainer>
    );
}
