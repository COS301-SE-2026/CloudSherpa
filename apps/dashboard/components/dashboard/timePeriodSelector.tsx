// components/dashboard/TimePeriodSelector.tsx
"use client"

import * as React from "react"
import { Button } from "@/components/atoms/button"
import { DateRangePicker } from "@/components/molecules/dateRangePicker" 
import { DateRange } from "react-day-picker"
import { subDays, subHours, subMonths } from "date-fns"

export function TimePeriodSelector() {
  const [date, setDate] = React.useState<DateRange | undefined>({
    from: subDays(new Date(), 7),
    to: new Date(),
  })

  //quick select ranges
  const quickRanges = [
    { label: "1H", getValue: () => ({ from: subHours(new Date(), 1), to: new Date() }) },
    { label: "24H", getValue: () => ({ from: subDays(new Date(), 1), to: new Date() }) },
    { label: "7D", getValue: () => ({ from: subDays(new Date(), 7), to: new Date() }) },
    { label: "30D", getValue: () => ({ from: subDays(new Date(), 30), to: new Date() }) },
  ]

  return (
    <div className="flex items-center gap-2 border rounded-md bg-card text-foreground">
      <div className="flex items-center gap-1 border-r pr-2">
        {quickRanges.map((range) => (
          <Button
            key={range.label}
            variant="ghost"
            size="sm"
            className="h-8 px-2 text-xs bg-card text-foreground active:bg-primary"
            onClick={() => setDate(range.getValue())}
          >
            {range.label}
          </Button>
        ))}
      </div>

      {/* The Calendar Picker for Specific Ranges */}
      <DateRangePicker date={date} setDate={setDate} />
    </div>
  )
}