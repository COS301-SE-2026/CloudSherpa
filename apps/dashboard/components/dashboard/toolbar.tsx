"use client";

import { Button } from "@/components/atoms/button";
import { Dropdown } from "@/components/atoms/dropdown";
import { TimePeriodSelector } from "@/components/molecules/timePeriodSelector";
import { useState } from "react";
import { Pencil } from "lucide-react";
import { cn } from "@/lib/utils";

interface DashboardStub {
  id: string;
  label: string;
}

export default function Toolbar() {
  const dashboards: DashboardStub[] = [
    { id: "ds-1", label: "Global Cost Overview" },
    { id: "ds-2", label: "AWS Production Metrics" },
    { id: "ds-3", label: "Azure Spending Forecast" },
  ];

  const [selectedId, setSelectedId] = useState<string>(dashboards[0].id);

  return (
    <div className="w-full flex flex-col items-center justify-between gap-4 p-4 transition-card">
      <div className="w-full flex flex-row items-center justify-between gap-3">
        <div className="flex items-center gap-3">
          <Dropdown<DashboardStub>
            options={dashboards}
            value={selectedId}
            onChange={(value) => setSelectedId(value)}
            labelKey="label"
            valueKey="id"
            placeholder="Select Dashboard"
            className="w-72 bg-card border-border hover:bg-hover text-foreground transition-button"
          />

          <Button
            variant="outline"
            size="icon"
            className="bg-card border-border text-foreground-secondary hover:text-primary hover:border-primary transition-button">
            <Pencil className="h-4 w-4" />
          </Button>
        </div>
      </div>

      <div className="w-full flex flex-row items-center justify-end gap-3">
        <TimePeriodSelector />
      </div>
    </div>
  );
}
