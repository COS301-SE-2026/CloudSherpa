import { Checkbox } from "@/components/atoms/checkbox";
import { Field } from "@/components/atoms/field";
import { Label } from "@/components/atoms/label";
import { ReactNode } from "react";

interface BillingFormContainerProps {
    readonly children: ReactNode;
    readonly optedInToBilling: boolean;
    readonly handleOptedInToBillingChange: (checked: boolean) => void;
}

export function BillingFormContainer({
    children,
    optedInToBilling,
    handleOptedInToBillingChange,
}: BillingFormContainerProps) {
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
                    {children}
                </section>
            )}
        </>
    );
}
