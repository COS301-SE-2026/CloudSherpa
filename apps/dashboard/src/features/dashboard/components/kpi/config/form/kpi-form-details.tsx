import {
    FieldSet,
    FieldLegend,
    FieldDescription,
    FieldGroup,
    Field,
    FieldLabel,
} from "@/components/atoms/field";
import { Input } from "@/components/atoms/input";
import { FormCountCircle } from "@/components/atoms/form-count-circle";
import { cn } from "@/lib/utils";

type KpiFormDetailsProps = {
    readonly title: string;
    readonly onTitleChange: (title: string) => void;
    error?: string;
};

export function KpiFormDetails({ title, onTitleChange, error }: Readonly<KpiFormDetailsProps>) {
    return (
        <FieldSet>
            <div className="flex flex-row items-center gap-3">
                <FormCountCircle count={1} />
                <FieldLegend className="mb-0">KPI Details</FieldLegend>
            </div>
            <FieldDescription>
                Choose the title that will appear on the dashboard card.
            </FieldDescription>
            <FieldGroup>
                <Field>
                    <FieldLabel>Card Title</FieldLabel>
                    <Input
                        placeholder="Card Title"
                        value={title}
                        onChange={(e) => {
                            onTitleChange(e.target.value);
                        }}
                        maxLength={80}
                        aria-label={"kpi display name"}
                        className={cn({ "border border-destructive": error })}
                    ></Input>
                </Field>
            </FieldGroup>
        </FieldSet>
    );
}
