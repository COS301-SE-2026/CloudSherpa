"use client"

import * as React from "react"
import { Play, Pause, RotateCcw } from "lucide-react"
import { Button } from "@/components/atoms/button"
import { Progress } from "@/components/atoms/progress"
import { Dropdown } from "@/components/atoms/dropdown"
import { cn } from "@/lib/utils"

interface AutoPlayProps {
  duration?: number // seconds
  totalDashboards?: number
  className?: string
}

export function Playlist({ 
  duration = 10, 
  totalDashboards = 5,
  className 
}: AutoPlayProps) {
  const [isPlaying, setIsPlaying] = React.useState(false)
  const [currentIndex, setCurrentIndex] = React.useState(1)
  const [progress, setProgress] = React.useState(0)
  const [secondsRemaining, setSecondsRemaining] = React.useState(duration)

  // stub sequence 
  const dashboardSequence = Array.from({ length: totalDashboards }, (_, i) => ({
    id: (i + 1).toString(),
    display: `${i + 1}`
  }))

  React.useEffect(() => {
    let timer: NodeJS.Timeout
    const tickRate = 100 // set how smooth bar moves

    if (isPlaying) {
      timer = setInterval(() => {
        setProgress((prev) => {
          if (prev >= 100) {
            // Reset and move to next
            handleNext()
            return 0
          }
          return prev + (100 / (duration * 10))
        })
        
        // update timer
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

  const togglePlay = () => setIsPlaying(!isPlaying)

  return (
    <div className={cn("flex items-center gap-3 bg-card px-2 rounded-md border border-border", className)}>
      <div className="flex items-center gap-2">
        <Button 
          variant="ghost" 
          size="icon" 
          className="h-8 w-8" 
          onClick={togglePlay}
        >
          {isPlaying ? <Pause className="size-4" /> : <Play className="size-4 fill-current" />}
        </Button>

        {/* Sequence Dropdown */}
        <Dropdown
          options={dashboardSequence}
          value={currentIndex.toString()}
          onChange={(val) => {
            setCurrentIndex(parseInt(val))
            setProgress(0)
            setSecondsRemaining(duration)
          }}
          labelKey="display"
          valueKey="id"
          className="w-12 h-8 text-foreground font-medium bg-background"
        />
      </div>

      <Progress value={progress} className="h-1 w-16" />
      <div className="text-foreground w-6 text-center tabular-nums text-xs font-medium">
        {Math.ceil(secondsRemaining)}
      </div>
    </div>
  )
}