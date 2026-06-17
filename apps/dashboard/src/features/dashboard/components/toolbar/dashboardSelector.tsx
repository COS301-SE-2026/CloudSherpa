"use client";

import * as React from "react";
import { useState } from "react";
import { LayoutDashboard, Plus, ChevronLeft, ChevronDown } from "lucide-react";
import { cn } from "@/lib/utils";
import { Button } from "@/components/atoms/button";
import { Popover, PopoverContent, PopoverTrigger } from "@/components/atoms/popover";

interface DashboardStub {
  id: string;
  label: string;
}

interface DashboardSelectorProps {
  dashboards: DashboardStub[];
  selectedId: string;
  onSelect: (id: string) => void;
  onCreate: (name: string) => void;
}

export function DashboardSelector({ dashboards, selectedId, onSelect, onCreate }: Readonly<DashboardSelectorProps>) {
  const [open, setOpen] = useState(false);
  const [view, setView] = useState<"list" | "create">("list");
  const [newDashboardName, setNewDashboardName] = useState("");

  const selectedDashboard = dashboards.find((d) => d.id === selectedId);

  const handleCreate = () => {
    if (newDashboardName.trim()) {
      onCreate(newDashboardName);
      setNewDashboardName("");
      setView("list");
      setOpen(false);
    }
  };

  return (
    <Popover
      open={open}
      onOpenChange={(val) => {
        setOpen(val);
        if (!val) setView("list");
      }}>
      <PopoverTrigger asChild>
        <Button
          variant="outline"
          className="group md:min-w-40 w-fit justify-start text-left font-normal bg-card text-foreground border-border hover:bg-hover/90 hover:text-foreground data-[state=open]:text-foreground data-[state=open]:hover:text-foreground transition-button">
          <div className="flex flex-row items-center overflow-hidden">
            <LayoutDashboard className="mr-2 h-4 w-4 shrink-0" />
            <span className="truncate">{selectedDashboard?.label || "Select Dashboard"}</span>
          </div>
          <ChevronDown className="ml-2 h-4 w-4 shrink-0 text-muted-foreground transition-transform duration-200 group-data-[state=open]:rotate-180" />
        </Button>
      </PopoverTrigger>

      <PopoverContent className="p-0 w-72 bg-popover border-border-strong shadow-xl" align="start">
        {view === "list" ? (
          <div className="flex flex-col p-1">
            <div className="px-2 py-1.5 text-xs font-semibold text-muted-foreground uppercase tracking-wider">
              My Dashboards
            </div>
            {dashboards.map((d) => (
              <Button
                key={d.id}
                variant="ghost"
                className={cn(
                  "justify-start font-normal transition-button mb-0.5 last:mb-0",
                  selectedId === d.id
                    ? "bg-active text-primary-foreground"
                    : "text-foreground-secondary hover:bg-hover hover:text-foreground",
                )}
                onClick={() => {
                  onSelect(d.id);
                  setOpen(false);
                }}>
                {d.label}
              </Button>
            ))}

            <div className="h-px bg-border-subtle my-1 w-full" />

            <Button
              variant="ghost"
              className="justify-start font-medium text-accent hover:bg-accent transition-button group/btn hover:text-secondary"
              onClick={() => setView("create")}>
              <Plus className="mr-2 h-4 w-4 transition-transform group-hover/btn:rotate-90" />
              Create New Dashboard
            </Button>
          </div>
        ) : (
          // create new dashboard view
          <div className="flex flex-col p-3 animate-in fade-in zoom-in-95 duration-200">
              <div className="grid grid-cols-[30px_1fr_30px] items-center mb-3">
                <Button
                  variant="ghost"
                  size="icon"
                  onClick={() => setView("list")}
                  className="h-7 w-7 hover:bg-hover text-foreground-secondary">
                  <ChevronLeft className="h-4 w-4" />
                </Button>

                <span className="text-sm font-medium text-foreground text-center">New Dashboard</span>

                <div className="w-7" />
              </div>

            <input
              autoFocus
              placeholder="e.g. Production AWS Costs"
              className="flex h-9 w-full rounded-md border border-input bg-transparent px-3 py-1 text-sm shadow-sm transition-colors placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring"
              value={newDashboardName}
              onChange={(e) => setNewDashboardName(e.target.value)}
              onKeyDown={(e) => e.key === "Enter" && handleCreate()}
            />

            <Button
              className="mt-3 w-full bg-primary text-primary-foreground hover:bg-primary/90"
              onClick={handleCreate}
              disabled={!newDashboardName.trim()}>
              Create Dashboard
            </Button>
          </div>
        )}
      </PopoverContent>
    </Popover>
  );
}
