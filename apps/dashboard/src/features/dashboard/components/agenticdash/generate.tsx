import {
    InputGroup,
    InputGroupAddon,
    InputGroupButton,
    InputGroupTextarea,
} from "@/components/atoms/input-group";
import { useState } from "react";
import { Button } from "@/components/atoms/button";
import { Check, X } from "lucide-react";
import PresetPrompts from "@/features/dashboard/components/agenticdash/presetPrompts";

export default function GenerateDashInput() {
    const [isSessionActive, setIsSessionActive] = useState(true);

    return (
        <div className="h-full flex flex-col justify-end items-start gap-4">
            {!isSessionActive && (
                <div className="h-full flex-1 overflow-y-auto w-full">
                    <PresetPrompts />
                </div>
            )}
            <div className="shrink-0 w-full flex flex-col">
                {isSessionActive && (
                    <div className="flex items-center justify-between px-3 py-2 bg-muted/30 border border-b-0 rounded-t-lg transition-all animate-in fade-in slide-in-from-bottom-2">
                        <span className="text-muted-foreground text-xs font-medium">
                            Review AI layout draft...
                        </span>
                        <div className="flex items-center gap-1">
                            <Button
                                variant="ghost"
                                size="sm"
                                className="h-6 px-2 text-destructive hover:text-destructive hover:bg-destructive/10"
                                onClick={() => setIsSessionActive(false)}
                            >
                                <X className="w-3 h-3 mr-1" /> Discard
                            </Button>
                            <Button
                                variant="outline"
                                size="sm"
                                className="h-6 px-2 border-primary/30 text-primary hover:bg-primary/10"
                                onClick={() => setIsSessionActive(false)}
                            >
                                <Check className="w-3 h-3 mr-1" /> Accept
                            </Button>
                        </div>
                    </div>
                )}
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
        </div>
    );
}
