import { BillingFormContainer } from "../billingFormContainer";

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
            test
        </BillingFormContainer>
    );
}
