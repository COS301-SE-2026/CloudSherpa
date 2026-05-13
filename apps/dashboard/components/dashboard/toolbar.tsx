import { Button } from "@/components/atoms/button";
import { Dropdown } from "@/components/atoms/dropdown";
import { TimePeriodSelector } from "@/components/dashboard/timePeriodSelector";
import { Playlist } from "@/components/dashboard/playlist";
import { useState } from "react";
import { TriangleAlert, Plus } from "lucide-react";

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
    <div className="w-full md:h-10 flex flex-row items-center justify-between gap-3">
      <Dropdown<DashboardStub>
        options={dashboards}
        value={selectedId}
        onChange={(value) => setSelectedId(value)}
        labelKey="label"
        valueKey="id"
        placeholder="select Dashboard"
        className="w-60 bg-card rounded-md" 
      />
      <div className="h-full flex flex-row justify-end items-center gap-2">
        <Button className="bg-card">
            <TriangleAlert />
            Alerts
        </Button>
        <Playlist />
        <TimePeriodSelector />
        <Button className="bg-card">
            <Plus />
            Add
        </Button>

      </div>
    </div>
  );
}
