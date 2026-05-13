"use client"

import * as React from "react"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/atoms/select"
import { Clock } from "lucide-react"

interface TimePickerProps {
  onDurationChange: (totalSeconds: number) => void
  initialSeconds?: number
}


export function TimePicker({ onDurationChange, initialSeconds = 10 }: TimePickerProps) {
  const [hours, setHours] = React.useState(Math.floor(initialSeconds / 3600))
  const [minutes, setMinutes] = React.useState(Math.floor((initialSeconds % 3600) / 60))

  // update when change occurs
  React.useEffect(() => {
    const total = (hours * 3600) + (minutes * 60)
    // potential division by 0 problem. will sort it . 
    onDurationChange(total > 0 ? total : 10) 
  }, [hours, minutes])

  return (
    <div className="flex items-center gap-1 bg-card h-full p-1 rounded-md border border-input">
      <Clock className="size-3 text-muted-foreground " />
      
      {/* hrs */}
      <Select value={hours.toString()} onValueChange={(val) => setHours(parseInt(val))}>
        <SelectTrigger className="h-7 w-16.25 border-none bg-transparent focus:ring-0 text-xs">
          <SelectValue />
        </SelectTrigger>
        <SelectContent className="bg-card">
          {[0, 1, 2, 3, 4, 5, 8, 12, 16, 24].map((h) => (
            <SelectItem key={h} value={h.toString()}>{h}h</SelectItem>
          ))}
        </SelectContent>
      </Select>

      <span className="text-muted-foreground text-[10px]">:</span>

      {/* mins */}
      <Select value={minutes.toString()} onValueChange={(val) => setMinutes(parseInt(val))}>
        <SelectTrigger className="h-7 w-16.25 border-none bg-transparent focus:ring-0 text-xs">
          <SelectValue />
        </SelectTrigger>
        <SelectContent>
          {[0, 1, 5, 10, 15, 30, 45].map((m) => (
            <SelectItem key={m} value={m.toString()}>{m}m</SelectItem>
          ))}
        </SelectContent>
      </Select>
    </div>
  )
}