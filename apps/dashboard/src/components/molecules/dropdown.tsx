import { Popover, PopoverTrigger, PopoverContent } from "@/components/atoms/popover";
import {
    Command,
    CommandInput,
    CommandList,
    CommandEmpty,
    CommandGroup,
    CommandItem,
} from "@/components/atoms/command";
import { Button } from "@/components/atoms/button";
import { useState } from "react";
import { ChevronDown, Check } from "lucide-react";
import { cn } from "@/lib/utils";

export interface DropdownOption {
    value: string;
    label: string;
}

interface BaseDropdownProps {
    options: DropdownOption[];
    onSelect: (value: string) => void;
    disabled?: boolean;
    disableSearch?: boolean;
    placeholder: string;
    widthVariant?: "small" | "medium" | "large" | "full";
    className?: string;
    emptyMessage?: string;
}

type SingleDropdownProps = BaseDropdownProps & {
    multiple?: false;
    value: string | null;
    onSelect: (value: string) => void;
};

type MultiDropdownProps = BaseDropdownProps & {
    multiple: true;
    value: string[];
    onSelect: (value: string[]) => void;
};

export type DropdownProps = SingleDropdownProps | MultiDropdownProps;

const WIDTH_VARIANTS = {
    small: "w-35",
    medium: "w-50",
    large: "w-80",
    full: "w-full",
};

export default function Dropdown(props: Readonly<DropdownProps>) {
    const {
        options,
        disabled = false,
        disableSearch = false,
        placeholder = "Select option...",
        widthVariant = "full",
        className,
        emptyMessage = "No options found",
    } = props;

    const [open, setOpen] = useState(false);

    const renderTriggerLabel = () => {
        if (props.multiple) {
            const selectedValues = props.value ?? [];
            if (selectedValues.length === 0) return placeholder;
            if (selectedValues.length === 1) {
                return (
                    options.find((opt) => opt.value === selectedValues[0])?.label ??
                    selectedValues[0]
                );
            }
            return `${selectedValues.length} selected`;
        }

        if (!props.value) return placeholder;
        return options.find((opt) => opt.value === props.value)?.label ?? props.value;
    };

    const handleSelectOption = (optionValue: string) => {
        if (props.multiple) {
            const currentValues = props.value ?? [];
            const isSelected = currentValues.includes(optionValue);
            const nextValues = isSelected
                ? currentValues.filter((v) => v !== optionValue)
                : [...currentValues, optionValue];
            props.onSelect(nextValues);
        } else {
            props.onSelect(optionValue);
            setOpen(false);
        }
    };

    const isOptionSelected = (optionValue: string) => {
        if (props.multiple) {
            return (props.value ?? []).includes(optionValue);
        }
        return props.value === optionValue;
    };

    return (
        <div className={cn(WIDTH_VARIANTS[widthVariant], className)}>
            <Popover open={open} onOpenChange={setOpen}>
                <PopoverTrigger asChild>
                    <Button
                        variant="outline"
                        role="combobox"
                        aria-expanded={open}
                        className="justify-between w-full bg-card"
                        disabled={disabled}
                    >
                        <span className="truncate">{renderTriggerLabel()}</span>
                        <ChevronDown
                            className={cn(
                                "h-4 w-4 opacity-50 transition-transform duration-200",
                                open && "rotate-180"
                            )}
                        />
                    </Button>
                </PopoverTrigger>
                <PopoverContent className="p-0 w-(--radix-popover-trigger-width)">
                    <Command>
                        {!disableSearch && <CommandInput placeholder="Search ..." />}
                        <CommandList>
                            <CommandEmpty>{emptyMessage}</CommandEmpty>
                            <CommandGroup>
                                {options.map((opt) => {
                                    const selected = isOptionSelected(opt.value);
                                    return (
                                        <CommandItem
                                            key={opt.value}
                                            value={opt.value}
                                            onSelect={() => handleSelectOption(opt.value)}
                                        >
                                            <Check
                                                className={cn(
                                                    "mr-2 h-4 w-4 shrink-0",
                                                    selected ? "opacity-100" : "opacity-0"
                                                )}
                                            />
                                            <span className="truncate">{opt.label}</span>
                                        </CommandItem>
                                    );
                                })}
                            </CommandGroup>
                        </CommandList>
                    </Command>
                </PopoverContent>
            </Popover>
        </div>
    );
}
