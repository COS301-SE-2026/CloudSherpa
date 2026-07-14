"use client";

/*
ideas for this page:
- should display a list of resources assoc with a particular conn
- should be able to sort the resource name (used a tan stack table)
- should be able to toggle the resource (active/inactive)
- should be able to search the resources
*/

import {useMemo, useState} from "react";
import {useReactTable, getCoreRowModel, getFilteredRowModel, getSortedRowModel, createColumnHelper, flexRender, type SortingState, type ColumnFiltersState, type HeaderContext, type CellContext} from "@tanstack/react-table";
import {ToggleGroup, ToggleGroupItem} from "@/components/atoms/toggle-group";
import {Switch} from "@/components/atoms/switch";
import {Badge} from "@/components/atoms/badge";
import {Button} from "@/components/atoms/button";
import {Input} from "@/components/atoms/input";
import {Table, TableBody, TableCell, TableHead, TableHeader, TableRow} from "@/components/atoms/table";
import {Search, ArrowUpDown} from "lucide-react";

interface Resource{
    id : string;
    name : string;
    type : string;
    region : string;
    tag : string[];
    status : "active" | "inactive";
}
interface ResourceAction{
    changeStatus : (id : string) => void;
}

const hardCodedResources : Resource[] = [
    {
        id : "1", name : "Resource 1", type : "Type 1",
        region : "region 1", tag : ["Tag1", "Tag2", "Tag3"], status : "active",
    },

    {
        id : "2", name : "Resource 2", type : "Type 2",
        region : "region 2", tag : ["Tag1", "Tag2", "Tag3"], status : "inactive",
    },
];

export default function ResourceManager(){
    return(
        <div className = "min-h-screen bg-background text-foreground p-8">
            <h1 className = "text-3xl font-semibold text-center mb-8"> Resource Manager </h1>

            <div className = "max-w-4xl mx-auto flex flex-col gap-4">
                
            </div>
        </div>
    );
}