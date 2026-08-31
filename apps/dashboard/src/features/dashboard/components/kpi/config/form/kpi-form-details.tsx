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

type KpiFormDetailsProps = {
    readonly title: string;
    readonly onTitleChange: (title: string) => void;
};

export function KpiFormDetails({ title, onTitleChange }: KpiFormDetailsProps) {
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
                        aria-label={"kpi display name"}
                    ></Input>
                </Field>
            </FieldGroup>
        </FieldSet>
    );
}
