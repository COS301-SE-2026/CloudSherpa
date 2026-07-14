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

//only 3 tags ae displayed, rest are hidden
function ListOfTags({tags} : Readonly<{tags : string[]}>){
    const displayedTags = tags.slice(0,3);

    return(
        <div className = "flex items-center gap-1 flex-wrap">
            {displayedTags.map((tag) => (
                <Badge key = {tag} variant = "secondary" className = "text-[10px] font-normal"> {tag} </Badge>
            ))}
        </div>
    )
}

//this allows a sortable col to rendered, can be clicked on to sort the resources (asc to desc and vice versa)
function ResourceHeaders({column} : Readonly<HeaderContext<Resource, string>>){
    return(
        <Button variant = "ghost" size = "sm" 
            className = "h-auto p-0 font-medium text-foreground hover:bg-transparent hover:text-foreground/80"
            onClick = {() => column.toggleSorting(column.getIsSorted() === "asc")}> Resource <ArrowUpDown size = {12} className = "ml-1.5 text-muted-foreground"/>
        </Button>
    );
}

function ResourceCells({getValue} : Readonly<CellContext<Resource, string>>){
    return <span className = "font-medium"> {getValue()} </span>;
}

function SecondaryCells({getValue} : Readonly<CellContext<Resource, string>>){
    return <span className = "text-xs text-muted-foreground"> {getValue()} </span>;
}

function TagCells({getValue} : Readonly<CellContext<Resource, string[]>>){
    return <ListOfTags tags = {getValue()}/>;
}

function ToggleHeader(){
    return <span className = "block text-center"> Active/Inactive </span>;
}

function ToggleCells({row, table} : Readonly<CellContext<Resource, Resource["status"]>>){
    const {changeStatus} = table.options.meta as ResourceAction;

    return(
        <div className = "flex justify-center">
            <Switch checked = {row.original.status === "active"} onCheckedChange = {() => changeStatus(row.original.id)}/>
        </div>
    )
}

//this will define the col structure for the table
//by using createColumnHelper we get type-safe col def
const helperForColumns = createColumnHelper<Resource>();

const columns = [
    helperForColumns.accessor("name", {header : ResourceHeaders, cell : ResourceCells,}),

    helperForColumns.accessor("type", {header : "Type", cell : SecondaryCells,}),

    helperForColumns.accessor("region", { header : "Region", cell : SecondaryCells,}),

    helperForColumns.accessor("tag", {header : "Tags", cell : TagCells,}),

    helperForColumns.accessor("status", {header : ToggleHeader, filterFn : "equals", cell : ToggleCells,}),
];

export default function ResourceManager(){
    const [resource, setResource] = useState<Resource[]>(hardCodedResources);

    const [filter, setFilter] = useState("");

    const [sort, setSort] = useState<SortingState>([]);

    const [filterColumn, setFilterColumn] = useState<ColumnFiltersState>([]);

    const changeStatus = (id : string) => {
        setResource((previous) => 
            previous.map((resources) => 
                resources.id === id ? {...resources, status : resources.status === "active" ? "inactive" : "active"} : resources
            )
        );
    };

    //useMemo prevents the actions obj from being recreated on every render
    const actions = useMemo<ResourceAction>(() => ({changeStatus}), []);

    const table = useReactTable({
        data : resource,
        columns,
        meta : actions,
        state : {globalFilter : filter, sorting : sort, columnFilters : filterColumn,},
        
        getRowId : (row) => row.id,
        onGlobalFilterChange : setFilter,

        onSortingChange : setSort,
        onColumnFiltersChange : setFilterColumn,

        //returns all the rows without any filtering/sorting
        getCoreRowModel : getCoreRowModel(),

        //enables row filtering
        getFilteredRowModel : getFilteredRowModel(),

        //enables row sorting
        getSortedRowModel : getSortedRowModel(),
    });

    const filterStatus = (table.getColumn("status")?.getFilterValue() as string | undefined) ?? "all";

    const setFilterStatus = (value : string) => {
        table.getColumn("status")?.setFilterValue(value === "all" ? undefined : value);
    };

    return(
        <div className = "min-h-screen bg-background text-foreground p-8">
            <h1 className = "text-3xl font-semibold text-center mb-8"> Resource Manager </h1>

            <div className = "max-w-4xl mx-auto flex flex-col gap-4">
                <div className = "flex items-center justify-between gap-3">

                    <div className = "relative flex-1">
                        <Search size = {14} className = "absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground"/>

                        <Input value = {filter} onChange = {(change) => setFilter(change.target.value)} placeholder = "Search resources..." className = "pl-8 h-9"/>
                    </div>

                    {/*
                        why is ToggleGroup being used and not tabs?
                        - toggle group is more appropriate for when changing between states/filtering as the same content is being shown but just a filtered version
                    */}
                    <ToggleGroup type = "single" value = {filterStatus} onValueChange = {(value) => value && setFilterStatus(value)} className = "bg-muted rounded-lg p-1 h-9">
                        <ToggleGroupItem value = "all" className = "h-full text-xs px-3 capitalize data-[state=on]:bg-primary data-[state=on]:text-primary-foreground"> All</ToggleGroupItem>
                        <ToggleGroupItem value = "active" className = "h-full text-xs px-3 capitalize data-[state=on]:bg-primary data-[state=on]:text-primary-foreground"> Active </ToggleGroupItem>
                        <ToggleGroupItem value = "inactive" className = "h-full text-xs px-3 capitalize data-[state=on]:bg-primary data-[state=on]:text-primary-foreground"> Inactive </ToggleGroupItem>
                    </ToggleGroup>

                </div>

            </div>
        </div>
    );
}