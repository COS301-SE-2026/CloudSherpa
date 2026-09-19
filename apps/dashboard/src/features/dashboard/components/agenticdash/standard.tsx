import {
    InputGroup,
    InputGroupAddon,
    InputGroupButton,
    InputGroupTextarea,
} from "@/components/atoms/input-group";
import PresetPrompts from "@/features/dashboard/components/agenticdash/presetPrompts";

export default function StandardDashInput() {
    return (
        <div className="h-full flex flex-col justify-between items-start gap-4">
            <PresetPrompts />
            <InputGroup>
                <InputGroupTextarea placeholder="Write your prompt here..." />
                <InputGroupAddon align="block-end">
                    <InputGroupButton variant="default" size="sm" className="ml-auto">
                        Generate
                    </InputGroupButton>
                </InputGroupAddon>
            </InputGroup>
        </div>
    );
}
