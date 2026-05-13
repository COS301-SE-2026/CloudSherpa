"use client"
import Toolbar from "@/components/dashboard/toolbar";
import Grid from "@/components/dashboard/grid";


export default function Dashboard () {
    return(
      <div className="w-full h-screen flex flex-col bg-[#080616] p-2 bg-[radial-gradient(circle_at_center,#2F2FE4_0%,#080616_100%)]">
        <Toolbar/>
        <Grid/>
      </div>
    );
}