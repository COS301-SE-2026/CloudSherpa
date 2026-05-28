"use client"

import { 
  Select, 
  SelectContent, 
  SelectItem, 
  SelectTrigger, 
  SelectValue 
} from "@/components/atoms/select"
import { cn } from "@/lib/utils"

interface DropdownProps<T> {
  options: T[]
  value?: string
  onChange: (value: string) => void
  placeholder?: string
  labelKey: keyof T
  valueKey: keyof T
  className?: string
}

export function Dropdown<T>({
  options,
  value,
  onChange,
  placeholder = "Select an option",
  labelKey,
  valueKey,
  className
}: DropdownProps<T>) {
  return (
    <Select value={value} onValueChange={onChange}>
<SelectTrigger 
  className={cn(
    "md:min-w-40 w-full justify-between text-left font-normal bg-card border-border hover:bg-hover transition-button text-foreground",
    className
  )}
>
  <SelectValue placeholder={placeholder} />
</SelectTrigger>

      <SelectContent 
        position="popper" 
        className="p-1 min-w-44 bg-popover border-border-strong shadow-xl animate-in fade-in zoom-in-95 duration-100"
      > 
        {options.map((option) => {
          const itemValue = String(option[valueKey])
          const itemLabel = String(option[labelKey])
          const isSelected = value === itemValue
          
          return (
            <SelectItem 
              key={itemValue} 
              value={itemValue}
              className={cn(
                "justify-start font-normal transition-button cursor-pointer rounded-sm mb-0.5 last:mb-0",
                isSelected 
                  ? "bg-active text-primary-foreground" 
                  : "text-foreground-secondary hover:bg-hover hover:text-foreground"
              )}
            > 
              {itemLabel}
            </SelectItem>
          )
        })}
      </SelectContent>
    </Select>
  )
}