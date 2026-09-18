import {
    InputGroup,
    InputGroupAddon,
    InputGroupButton,
    InputGroupInput,
    InputGroupText,
    InputGroupTextarea,
} from "@/components/atoms/input-group";

export default function StandardDashInput() {
    return (
        <div>
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
