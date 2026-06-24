"use client";

/*
- users should be able to add connections
- when adding connections, they should be redirected to the wizard setup
- they should be able to view the resources assoc with that connection and delete connections
*/

import {useState} from "react";
import {Trash2} from "lucide-react";

type Providers = "AWS" | "Azure" | "GCP";

interface Connections{
  id : number;
  name : string;
  provider : Providers;
  detail : string;
}

const hardCodedConn : Connections[] = [
  {
    id : 1 , name : "Connection 1",
    detail : "details about conn", provider : "AWS",
  },
];

export default function ManagingConnections(){
  const [connections, setConnections] = useState<Connections[]>(hardCodedConn);

  return(
    <div
      data-theme = "dark"
      className = "min-h-screen bg-[var(--color-neutral-950)] text-[var(--color-neutral-50)] p-8">
      
      {/* this si for the heading */}
      <div className = "flex items-start justify-between mb-5">
        <h1 className = "text-3xl font-semibold text-[var(--color-neutral-50)]"> Connection Manager </h1>

      </div>

      {/* this is for the list of conn */}
      <div className = "flex flex-col gap-2">
        {filtered.map((connection) => (
          <div 
            key = {connection.id}
            className = "flex items-center justify-between px-4 py-3 rounded-[var(--radius-md)] border border-[var(--color-neutral-800)] bg-[var(--color-neutral-900)]">

            <div>
              <p className = "text-sm font-medium text-[var(--color-neutral-50)]"> {connection.name} </p>

              <p className = "text-xs text-[var(--color-neutral-400)] mt-0.5"> {connection.detail} </p>
            </div>

            <div className = "flex items-center gap-3">
              <span className = "text-xs text-[var(--color-neutral-400)]"> resource </span>

              <button
                onClick = {() => handleDeletion(connection.id)}
                className = "text-[var(--color-neutral-400)] hover:text-[var(--color-error-500)] transition-colors"> <Trash2 size={15} />
              </button>

            </div>

          </div>
        ))}
      </div>

    </div>
  );
}
