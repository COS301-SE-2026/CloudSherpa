"use client"

import * as React from "react"
import { Play, Pause, RotateCcw } from "lucide-react"
import { Button } from "@/components/atoms/button"
import { Progress } from "@/components/atoms/progress"
import { Dropdown } from "@/components/atoms/dropdown"
import { TimePicker } from "@/components/molecules/timePicker"
import { cn } from "@/lib/utils"

interface AutoPlayProps {
  duration?: number // seconds
  totalDashboards?: number
  className?: string
}

export function Playlist({ totalDashboards = 5, className }: AutoPlayProps) {
  const [isPlaying, setIsPlaying] = React.useState(false)
  const [currentIndex, setCurrentIndex] = React.useState(1)
  const [progress, setProgress] = React.useState(0)
  
  const [duration, setDuration] = React.useState(60) //default 1 min-division by 0 in timepicker
  const [secondsRemaining, setSecondsRemaining] = React.useState(60)

  const handleDurationChange = (newTotalSeconds: number) => {
    setDuration(newTotalSeconds)
    setSecondsRemaining(newTotalSeconds)
    setProgress(0)
  }

  React.useEffect(() => {
    let timer: NodeJS.Timeout
    const tickRate = 100 // Updates every 100ms for smooth progress bar

    if (isPlaying) {
      timer = setInterval(() => {
        setProgress((prev) => {
          if (prev >= 100) {
            handleNext()
            return 0
          }
          // Increment based on total duration
          return prev + (100 / (duration * (1000 / tickRate)))
        })
        
        setSecondsRemaining((prev) => (prev > 0.1 ? prev - 0.1 : duration))
      }, tickRate)
    }

    return () => clearInterval(timer)
  }, [isPlaying, duration, currentIndex])

  const handleNext = () => {
    setCurrentIndex((prev) => (prev % totalDashboards) + 1)
    setProgress(0)
    setSecondsRemaining(duration)
  }

  const dashboardSequence = Array.from({ length: totalDashboards }, (_, i) => ({
    id: (i + 1).toString(),
    display: `${i + 1}`
  }))

  return (
    <div className={cn("flex h-9 items-center gap-3 bg-card px-2 rounded-md border border-border", className)}>
      <div className="flex items-center gap-2 border-r ">
        <Button variant="ghost" size="icon" className="h-8 w-8" onClick={() => setIsPlaying(!isPlaying)}>
          {isPlaying ? <Pause className="size-4" /> : <Play className="size-4 fill-current" />}
        </Button>

        {/* sequence*/}
        <Dropdown
          options={dashboardSequence}
          value={currentIndex.toString()}
          labelKey="display"
          valueKey="id"
          onChange={(val) => {
            setCurrentIndex(parseInt(val))
            setProgress(0)
            setSecondsRemaining(duration)
          }}
          className="w-12 h-7"
        />
      </div>
      <TimePicker onDurationChange={handleDurationChange} initialSeconds={duration} />

      <div className="flex items-center gap-3 ">
        <Progress value={progress} className="h-1 w-16" />
        {/* <div className="text-foreground w-10 text-right tabular-nums text-[11px] font-mono font-bold">
          {Math.ceil(secondsRemaining)}s
        </div> */}
      </div>
    </div>
  )
}