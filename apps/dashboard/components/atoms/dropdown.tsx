"use client"

import { 
  Select, 
  SelectContent, 
  SelectItem, 
  SelectTrigger, 
  SelectValue 
} from "@/components/atoms/select"

// T represents the data object type
interface DropdownProps<T> {
  options: T[]
  value?: string
  onChange: (value: string) => void
  placeholder?: string
  labelKey: keyof T //label as in name
  valueKey: keyof T //value as in id
  className?: string
}

export function Dropdown<T>({
  options,
  value,
  onChange,
  placeholder = "Select an option",
  labelKey,
  valueKey,
  className = "w-full" //used w-full since it's more generic
}: DropdownProps<T>) {
  return (
    <Select value={value} onValueChange={onChange}>
      <SelectTrigger className={className}>
        <SelectValue placeholder={placeholder} />
      </SelectTrigger>
      <SelectContent position="popper"> {/*represents the lsit of items passed in, also not position=popper prevents content from overlaying on select trigger via alignment*/}
        {options.map((option) => {
          const itemValue = String(option[valueKey])
          const itemLabel = String(option[labelKey])
          
          return (
            <SelectItem key={itemValue} value={itemValue}> {/*renders <selectItem for each item as options is mapped over*/}
              {itemLabel}
            </SelectItem>
          )
        })}
      </SelectContent>
    </Select>
  )
}