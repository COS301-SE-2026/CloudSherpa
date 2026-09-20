import {
    InputGroup,
    InputGroupAddon,
    InputGroupButton,
    InputGroupTextarea,
} from "@/components/atoms/input-group";
import PresetPrompts from "@/features/dashboard/components/agenticdash/presetPrompts";

export default function GenerateDashInput() {
    return (
        <div className="h-full flex flex-col justify-between items-start gap-4">
            <div className="flex-1 overflow-y-auto w-full">
                <PresetPrompts />
            </div>
            <div className="shrink-0 bg-background w-full">
                <InputGroup>
                    <InputGroupTextarea
                        placeholder="Write your prompt here..."
                        className="z-50 min-h-0"
                    />
                    <InputGroupAddon align="block-end">
                        <InputGroupButton variant="default" size="sm" className="ml-auto">
                            Generate
                        </InputGroupButton>
                    </InputGroupAddon>
                </InputGroup>
            </div>
        </div>
    );
}
