"use client";

import React, {useState, useEffect, useMemo, useCallback} from "react";
import {StepThree} from "@/features/connectionManager/components/connectionManager/wizardSetup/stepThree";
import {Button} from "@/components/atoms/button";
import {Tooltip, TooltipContent, TooltipProvider, TooltipTrigger} from "@/components/atoms/tooltip";
import {Slider} from "@/components/atoms/slider";
import {Label} from "@/components/atoms/label";
import {Search, ArrowUpDown} from "lucide-react";
import {Input} from "@/components/atoms/input";
import {Badge} from "@/components/atoms/badge";
import {Switch} from "@/components/atoms/switch";
import {useReactTable, getCoreRowModel, getFilteredRowModel, getSortedRowModel, createColumnHelper, flexRender, type SortingState, type ColumnFiltersState, type HeaderContext, type CellContext} from "@tanstack/react-table";
import {Table, TableBody, TableCell, TableHead, TableHeader, TableRow} from "@/components/atoms/table";

/*
- should have tanstack table for resources, as elect & deselect all for it
- should also have pagination
*/
interface DetailsForResource{
    id : string;
    name : string;
    type?: string;
    region?: string;
    tag?: string[];
    status?: "active" | "inactive";
}

interface StepThreePropsForGcp{
    resources?: DetailsForResource[];
    onNext : (data : Record<string, unknown>) => void;
    onBack?: () => void;
    ingestionPeriod?: string;
}

interface Resources{
    id : string;
    name : string;
    type : string;
    region : string;
    tag : string[];
    status : "active" | "inactive";
    selected : boolean;
}

interface ActionForResource{
    changeStatus : (id : string) => Promise<void>;
    toggleResource : (id : string) => void;
    toggleAll : () => void;
}

const hardCodedResources : DetailsForResource[] = [
    {id : "resource1", name : "Resource one", type : "Service one", region : "us-central", tag : ["tag1", "tag2"]},
    {id : "resource2", name : "Resource two", type : "Service two", region : "us-east", tag : ["tag1", "tag2"]},
    {id : "resource3", name : "Resource three", type : "Service three", region : "europe-west", tag : ["tag1", "tag2"]},
];

function ListOfTags({tags} : Readonly<{tags : string[]}>){
    const displayedTags = tags.slice(0,3);

    return(
        <div className = "flex items-center gap-1 flex-wrap">
            {displayedTags.map((tag) => (
                <Badge key = {tag} variant = "secondary" className = "text-[10px] font-normal"> {tag} </Badge>
            ))}
        </div>
    );

}

function ResourceHeaders({column} : Readonly<HeaderContext<Resources, string>>){
    return(
        <Button variant = "ghost" size = "sm" className = "h-auto p-0 font-medium text-foreground hover:bg-transparent hover:text-foreground/80" onClick = {() => column.toggleSorting(column.getIsSorted() === "asc" )}> Resource <ArrowUpDown size = {12} className = "ml-1.5 text-muted-foreground"/> </Button>
    );
}

function ResourceCells({getValue} : Readonly<CellContext<Resources, string>>){
    return <span className = "font-medium"> {getValue()} </span>;
}

function SecondaryCells({getValue} : Readonly<CellContext<Resources, string>>){
    return <span className = "text-xs text-muted-foreground"> {getValue()} </span>;
}

function TagCells({getValue} : Readonly<CellContext<Resources, string[]>>){
    return <ListOfTags tags = {getValue()}/>;
}

function ToggleHeader(){
    return <span className = "block text-center"> Active/Inactive </span>
}

function ToggleCells({row, table} : Readonly<CellContext<Resources,Resources["status"]>>){
    const {changeStatus} = table.options.meta as ActionForResource;

    return(
        <div className = "flex justify-center">
            <Switch checked = {row.original.status === "active"} onCheckedChange = {() => changeStatus(row.original.id)}/>
        </div>
    )
}

function SelectionHeader({table} : Readonly<HeaderContext<Resources, boolean>>){
    const {toggleAll} = table.options.meta as ActionForResource;

    const rows = table.getRowModel().rows;

    const allSelected = rows.length>0 && rows.every((row) => row.original.selected);

    const someSelected = rows.some((row) => row.original.selected);

    return(
        <div className = "flex justify-center">

            <input type = "checkbox" checked = {allSelected} ref = {(input) => {
                if(input){
                    input.indeterminate = someSelected && !allSelected;
                }
            }}

            onChange = {toggleAll} className = "w-4 h-4 rounded border-border bg-background text-primary focus:ring-primary"/>
        </div>
    );
}

function SelectionCells({row, table} : Readonly<CellContext<Resources, boolean>>){
    const {toggleResource} = table.options.meta as ActionForResource;

    return(
        <div className = "flex justify-center">

            <input type = "checkbox" checked = {row.original.selected}
            onChange = {() => {
                toggleResource(row.original.id);
            }}
            className = "w-4 h-4 rounded border-border bg-background text-primary focus:ring-primary"/>
        </div>
    );
}

const helperForColumns = createColumnHelper<Resources>();

const columns = [
    helperForColumns.accessor("selected", {
        header : SelectionHeader, cell : SelectionCells,
    }),

    helperForColumns.accessor("name", {header : ResourceHeaders, cell : ResourceCells}),
    helperForColumns.accessor("type", {header : "Type", cell : SecondaryCells}),

    helperForColumns.accessor("region", {header : "Region", cell : SecondaryCells}),
    helperForColumns.accessor("tag", {header : "Tags", cell : TagCells}),

    helperForColumns.accessor("status", {
        header : ToggleHeader, filterFn : "equals", cell : ToggleCells,
    }),
];

export default function StepThreeGcp({
    resources = [], onNext, onBack, ingestionPeriod = "60",
} : Readonly<StepThreePropsForGcp>){
    const [forSaving, setForSaving] = useState(false);

    const [errors, setErrors] = useState<string | null>(null);

    const [forIngestionPeriod, setForIngestionPeriod] = useState<string>(ingestionPeriod);

    const [tableResources, setTableResources] = useState<Resources[]>([]);

    const [filter, setFilter] = useState("");

    const [sort, setSort] = useState<SortingState>([]);

    const [filterColumn, setFilterColumn] = useState<ColumnFiltersState>([]);

    const realResources = useMemo(() => (resources && resources.length>0 ? resources : hardCodedResources), [resources]);

    const count = tableResources.filter(forResources => forResources.selected).length;

    const recIngestionPeriod = count*5*20;

    const formattingSecond = (totalSeconds : string | number) => {
        const seconds = Number(totalSeconds);

        if(Number.isNaN(seconds) || seconds<=0){
            return "0 seconds";
        }

        const minutes = Math.floor(seconds/60);

        const secondsLeft = seconds%60;

        let minText = "";
        if(minutes>0){
            const labelEnding = minutes === 1 ? "" : "s";
            minText = `${minutes} minute${labelEnding}`;
        }

        let secText = "";
        if(secondsLeft>0){
            const labelEnding = secondsLeft === 1 ? "" : "s";
            secText = `${secondsLeft} second${labelEnding}`;
        }

        if(minText && secText){
            return `${minText} ${secText}`;
        }

        return minText || secText;
    };

    const changeStatus = useCallback(async (id : string) => {
        setTableResources((previous) => previous.map((resources) => resources.id === id ? {...resources, status : resources.status === "active" ? "inactive" : "active"} : resources));
    }, []);

    const toggleResource = useCallback((resourceId : string) => {
        setTableResources((previous) => previous.map((forResources) => forResources.id === resourceId ? {...forResources, selected : !forResources.selected} : forResources));
    }, []);

    const handlingSelectedAll = useCallback(() => {
        setTableResources((previous) => {
            const allSelected = previous.every((forResources) => forResources.selected);

            return previous.map((resource) => ({
                ...resource, selected : !allSelected,
            }));
        });
    }, []);

    const actions = useMemo<ActionForResource>(() => ({
        changeStatus, toggleResource, toggleAll : handlingSelectedAll,
    }), [changeStatus, toggleResource, handlingSelectedAll]);

    const table = useReactTable({
        data : tableResources, columns, meta : actions, state : {globalFilter : filter, sorting : sort, columnFilters : filterColumn},
        getRowId : (row) => row.id, onGlobalFilterChange : setFilter, onSortingChange : setSort, onColumnFiltersChange : setFilterColumn, getCoreRowModel : getCoreRowModel(),
        getFilteredRowModel : getFilteredRowModel(), getSortedRowModel : getSortedRowModel(),
    })

    useEffect(() => {
        const mappedResources : Resources[] = realResources.map((resources) => ({
            id : resources.id, name : resources.name, type : resources.type || "Unknown",
            region : resources.region || "Unknown", tag : resources.tag || ["No tags"],
            status : "active", selected : false,
        }));
        setTableResources(mappedResources);
    }, [realResources]);

    const handlingSubmit = async (forEvent : React.SubmitEvent<HTMLFormElement>) => {
        forEvent.preventDefault();

        setForSaving(true);
        setErrors(null);

        try{
            const resourcesSelected = tableResources.filter(forResources => forResources.selected).map(forResources => forResources.id);

            onNext({
                selectedResources : resourcesSelected, ingestionPeriod : forIngestionPeriod, tableResources : tableResources,
            });

        } catch{
            setErrors("Unable to complete GCP connection setup");
        } finally{
            setForSaving(false);
        }
    };

    return(
        <StepThree heading = "Select instances"
                   description = "Select the instance you want CloudSherpa to monitor"
                   onSubmit = {handlingSubmit} onBack = {onBack || (() => {})} forSaving = {forSaving} forErrors = {errors}
        >

        <div className = "space-y-4">
            <div className = "flex flex-wrap items-center justify-between gap-2">
                <h3 className = "text-foreground text-sm font-semibold uppercase tracking-wider opacity-80"> Available resources </h3>

                <Button type = "button" variant = "ghost" size = "sm" onClick = {handlingSelectedAll} className = "text-primary hover:text-accent text-sm transition-colors px-0">
                    {tableResources.length>0 && tableResources.every(r => r.selected) ? "Deselect All" : "Select All"}
                </Button>
            </div>

            <div className = "relative flex-1">
                <Search size = {14} className = "absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground"/>

                <Input value = {filter} onChange = {(change) => setFilter(change.target.value)} placeholder = "Search resources..." className = "pl-8 h-9"/>
            </div>

            <div className = "rounded-lg border">
                <Table className = "table-fixed w-full">

                    <TableHeader>
                        {table.getHeaderGroups().map((headerGroup) => (
                            <TableRow key = {headerGroup.id} className = "hover:bg-transparent">
                                {headerGroup.headers.map((header) => (

                                    <TableHead key = {header.id} className = {header.column.id === "selected" ? "w-10" : undefined}>
                                        {flexRender(
                                            header.column.columnDef.header, header.getContext()
                                        )}
                                    </TableHead>
                                    
                                ))}
                            </TableRow>
                        ))}
                    </TableHeader>

                    <TableBody>
                        {table.getRowModel().rows.length === 0 ? (
                            <TableRow>
                                <TableCell colSpan = {columns.length} className = "text-center text-xs text-muted-foreground py-6"> No resources </TableCell>
                            </TableRow>
                        ) : (
                            table.getRowModel().rows.map((row) => (
                                <TableRow key = {row.id}>
                                    {row.getVisibleCells().map((cell) => (
                                        <TableCell key = {cell.id} className = {cell.column.id === "selected" ? "w-10" : undefined}>
                                            {flexRender(
                                                cell.column.columnDef.cell, cell.getContext()
                                            )}
                                        </TableCell>
                                    ))}
                                </TableRow>
                            ))
                        )}
                    </TableBody>

                </Table>
            </div>
            </div>

            <div className = "space-y-2 pt-4 border-t border-border">
                <div className = "flex items-center gap-2">
                    <Label htmlFor = "ingestionPeriod" className = "text-foreground text-sm font-medium"> Ingestion interval (seconds) </Label>

                    <TooltipProvider>
                        <Tooltip>
                            <TooltipTrigger asChild>
                                <button type = "button" className = "flex items-center justify-center w-5 h-5 rounded-full text-xs text-muted-foreground hover:text-foreground border border-border"> ? </button>
                            </TooltipTrigger>

                            <TooltipContent>
                                <p> Recommended ingestion interval: {recIngestionPeriod}{" "} seconds based on {count} selected resources.
                                    Setting the interval to a lower value could incur costs due to API free tier limits. The ingestion interval determines the 
                                    frequency of dashboard timeseries updates. 
                                </p>
                            </TooltipContent>

                        </Tooltip>
                    </TooltipProvider>

                </div>

                <div className = "flex flex-col gap-2 justify-center items-end">
                    <span className = "text-sm font-medium"> {formattingSecond(forIngestionPeriod)} </span>

                    <Slider value = {[Number(forIngestionPeriod)]} onValueChange = {(changeVal) => setForIngestionPeriod(String(changeVal[0]))} min = {60} max = {400} step = {1}/>

                    <p className = "text-sm text-muted-foreground/70"> Recommended: {formattingSecond(recIngestionPeriod)} </p>
                </div>

            </div>
        </StepThree>
    );
}