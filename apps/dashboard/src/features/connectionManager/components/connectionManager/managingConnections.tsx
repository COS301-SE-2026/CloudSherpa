"use client";

/*
- users should be able to add connections
- when adding connections, they should be redirected to the wizard setup
- they should be able to view the resources assoc with that connection and delete connections
*/

import {useState} from "react";
import {Trash2} from "lucide-react";
import {Card, CardContent} from "@/components/atoms/card";
import {Button} from "@/components/atoms/button";

type Providers = "AWS" | "Azure" | "GCP";

interface Connections{
  id : number;
  name : string;
  provider : Providers;
  detail : string;
}

const providerTabs : Record<Providers, {active : string; inactive : string}> = {
  AWS : {
    active : "bg-[#FF9900] text-black", inactive : "bg-[#FF9900]/30 text-[#FF9900]/60",
  },

  Azure : {
    active : "bg-[var(--color-secondary-400)] text-black", inactive : "bg-[var(--color-secondary-400)]/30 text-[var(--color-secondary-400)]/60",
  },

  GCP : {
    active : "bg-[var(--color-success-600)] text-black", inactive : "bg-[var(--color-success-600)]/30 text-[var(--color-success-600)]/60",
  },
};

const hardCodedConn : Connections[] = [
  {
    id : 1 , name : "Connection 1",
    detail : "details about conn", provider : "AWS",
  },
];

export default function ManagingConnections(){
  const [connections, setConnections] = useState<Connections[]>(hardCodedConn);

  const [activeFilter, setActiveFilter] = useState<Providers | null>("AWS");

  const filtered = activeFilter ? connections.filter((filteredConnections) => filteredConnections.provider === activeFilter) : connections;

  const handleDeletion = (id : number) => {
    setConnections((previous) => previous.filter((filteredConnections) => filteredConnections.id != id));
  };

  const handleAdd = () => {
    //empty for now
  };

  return(
    <div
      data-theme = "dark"
      className = "min-h-screen bg-background text-foreground p-8">
      
      {/* this si for the heading */}
      <div className = "flex items-start justify-between mb-5">
        <h1 className = "text-3xl font-semibold text-foreground"> Connection Manager </h1>

        <Button
          onClick = {handleAdd}
          className = "text-sm px-3 py-1.5 h-auto"> + add connection
        </Button>

      </div>


      {/* this is for the provider tabs */}
      <div className = "flex gap-2 mb-4">
        {(["AWS", "Azure", "GCP"] as Providers[]).map((providers) => {
          const isActive = activeFilter === providers;

          const styling = providerTabs[providers];

          return(
            <Button 
              key = {providers}
              onClick = {() => setActiveFilter(isActive ? null : providers)}
              className = {`text-xs px-2.5 py-0.5 h-auto rounded-[var(--radius-sm)] font-medium transition-all ${
                isActive ? styling.active : styling.inactive
              }`}
              > {providers}
            </Button>
          );

        })}
      </div>


      {/* this is for the list of conn */}
      <div className = "flex flex-col gap-2">
        {filtered.map((connection) => (
          <Card 
            key = {connection.id}
            className = "border-border">

            <CardContent className = "px-4 py-3">
              <div className = "flex items-center justify-between">
                <div>
                  <p className = "text-sm font-medium text-foreground"> {connection.name} </p>

                  <p className = "text-xs text-[var(--color-neutral-400)] mt-0.5"> {connection.detail} </p>
                </div>

                <div className = "flex items-center gap-3">
                  <span className = "text-xs text-[var(--color-neutral-400)]"> resource </span>

                  <Button
                    onClick = {() => handleDeletion(connection.id)}
                    variant = "ghost"
                    size = "icon"
                    className = "text-[var(--color-neutral-400)] hover:text-[var(--color-error-500)] transition-colors"> <Trash2 size={15} />
                  </Button>

                </div>
              </div>
            </CardContent>

          </Card>
        ))}
      </div>

    </div>
  );
}
