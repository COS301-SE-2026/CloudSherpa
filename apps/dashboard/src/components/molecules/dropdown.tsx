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
import { ChevronsUpDown, Check } from "lucide-react";
import { cn } from "@/lib/utils";

export interface DropdownOption {
    value: string;
    label: string;
}

interface DropdownProps {
    options: DropdownOption[];
    value: string | null;
    onSelect: (value: string) => void;
    disabled?: boolean;
    disableSearch?: boolean;
    placeholder: string;
    widthVariant?: "small" | "medium" | "large" | "full";
    className?: string;
    emptyMessage?: string;
}

const WIDTH_VARIANTS = {
    small: "w-35",
    medium: "w-50",
    large: "w-80",
    full: "w-full",
};

export default function Dropdown({
    options,
    value,
    onSelect,
    disabled = false,
    disableSearch = false,
    placeholder = "select option...",
    widthVariant = "full",
    className,
    emptyMessage = "No options found",
}: Readonly<DropdownProps>) {
    const [open, setOpen] = useState(false);

    return (
        <div className={cn(WIDTH_VARIANTS[widthVariant], className)}>
            <Popover open={open} onOpenChange={setOpen}>
                <PopoverTrigger asChild>
                    <Button
                        variant="outline"
                        role="combobox"
                        aria-expanded={open}
                        className="justify-between w-full"
                        disabled={disabled}
                    >
                        <span className="truncate">
                            {value
                                ? options.find((opt) => opt.value === value)?.label
                                : placeholder}
                        </span>
                        <ChevronsUpDown className="ml-2 h-4 w-4 shrink-0 opacity-50" />
                    </Button>
                </PopoverTrigger>
                <PopoverContent className="p-0 w-(--radix-popover-trigger-width)">
                    <Command>
                        {!disableSearch && <CommandInput placeholder="Search ..." />}
                        <CommandList>
                            <CommandEmpty>{emptyMessage}</CommandEmpty>
                            <CommandGroup>
                                {options.map((opt) => (
                                    <CommandItem
                                        key={opt.value}
                                        value={opt.value}
                                        onSelect={(currentValue) => {
                                            onSelect(currentValue);
                                            setOpen(false);
                                        }}
                                    >
                                        <Check
                                            className={cn(
                                                "mr-2 h-4 w-4",
                                                value === opt.value ? "opacity-100" : "opacity-0"
                                            )}
                                        />
                                        {opt.label}{" "}
                                    </CommandItem>
                                ))}
                            </CommandGroup>
                        </CommandList>
                    </Command>
                </PopoverContent>
            </Popover>
        </div>
    );
}
