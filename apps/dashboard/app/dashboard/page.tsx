"use client"
import Toolbar from "@/components/dashboard/toolbar";
import Grid from "@/components/dashboard/grid";


export default function Dashboard () {
    return(
      <div className="w-full min-h-screen flex flex-col ">
        <Toolbar/>
        <Grid/>
      </div>
    );
}