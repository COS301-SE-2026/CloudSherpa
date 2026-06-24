'use client'
import { Input } from '@/components/atoms/input';
import React, {useState} from 'react';

/*
idea for this page:
- should have 2 sides (active and inactive)
- should be able to move resources from inactive and active, vice versa (a popup should appear to confirm actions)
- should be able to seach for resources
*/

interface Resource{
    id : string;
    name : string;
}

type ActionPending = {
    resource : Resource;
    direction : 'enable' | 'disable';
} | null;

export default function ResourceManager(){
    const [inactive, setInactive] = useState<Resource[]>([ {id: '1', name: 'Resource'},]);

    const [active, setActive] = useState<Resource[]>([]);

    const [selected, setSelected] = useState<string | null>(null);

    const [pending, setPending] = useState<ActionPending>(null);

    const [inactiveSearch, setInactiveSearch] = useState('');

    const [activeSearch, setActiveSearch] = useState('');

    const handleAdd = (resource : Resource) => {
        setPending({resource, direction : 'enable'});
    };

    const handleRemove = (resource : Resource) => {
        setPending({resource, direction : 'disable'});
    };

    const handleConfirm = () => {
        if(!pending){
            return;
        }

        if(pending.direction === 'enable'){
            setInactive((previous) => previous.filter((resource) => resource.id !== pending.resource.id));
            setActive((previous) => [...previous, pending.resource]);
        } else{
            setActive((previous) => previous.filter((resource) => resource.id !== pending.resource.id));
            setInactive((previous) => [...previous, pending.resource]);
        }

        if(selected === pending.resource.id){
            setSelected(null);
        }

        setPending(null);
    };

    const handleCancel =() => {
        setPending(null);
    };

    const inactiveFiltered = inactive.filter((resource) =>
        resource.name.toLowerCase().includes(inactiveSearch.toLowerCase())
    );

    const activeFiltered = active.filter((resource) =>
        resource.name.toLowerCase().includes(activeSearch.toLowerCase())
    );

    const confirmationText = pending?.direction === 'enable' ? "Are you sure you want resource to be active?" : "Are you sure you want resource to be inactive?";

    return(
        <div className = "min-h-screen flex flex-col items-center justify-center p-8 bg-background text-foreground">

            <h2 className = "text-lg font-medium tracking-tight mb-8 text-foreground"> Resource Manager </h2>

            <div className = "flex items-center gap-4">

                {/* this is for the inactive side */}
                <div className = "flex flex-col gap-1">
                    <span className = "text-xs text-center text-[var(--color-neutral-400)]"> Inactive </span>
                    <div className = "w-96 rounded-md overflow-hidden bg-card border border-border">
                        <div className = "flex items-center justify-between px-3 py-2 gap-2 border-b border-border">
                            <Input
                                placeholder = "Search..."
                                value = {inactiveSearch}
                                onChange = {(clickSearch) => setInactiveSearch(clickSearch.target.value)}
                                className = "h-5 text-xs border-none shadow-none px-1 bg-transparent focus-visible:ring-0 placeholder:text-neutral-500 text-foreground"
                            />

                            <button
                                onClick = {() => setInactiveSearch('')}
                                className = "text-xs text-[var(--color-neutral-400)]"> x 
                            </button>

                        </div>

                        <ul className = "p-2 min-h-96 space-y-1">
                            {inactiveFiltered.map((resource) =>
                                (
                                    <div
                                        key = {resource.id}
                                        className = {`flex items-center justify-between px-3 py-2 rounded text-sm cursor-pointer transition-colors ${ selected === resource.id ? 'bg-sidebar-primary-foreground text-primary-foreground' : 'text-sidebar-border hover:bg-border'}`}>

                                        <button
                                            onClick = {() => setSelected(resource.id)}
                                            className = "flex-1 text-left bg-transparent border-none text-inherit text-sm"> {resource.name} 
                                        </button>

                                        <button
                                            onClick = {(clickingAdd) => {clickingAdd.stopPropagation(); handleAdd(resource); }}
                                            className = "test-base leading-none ml-2 text-sidebar-accent hover:text-ring"> + 
                                        </button>

                                    </div>
                                )
                            )}
                        </ul>
                    </div>
                </div>
                
                {/* arrows */}
                <div className = "flex flex-col gap-2 mt-5">

                    <button className = "w-8 h-8 p-0 rounded-md flex items-center justify-center bg-border border border-sidebar-foreground text-foreground transition-colors">
                        &rarr;
                    </button>

                    <button className = "w-8 h-8 p-0 rounded-md flex items-center justify-center bg-border border border-sidebar-foreground text-foreground transition-colors">
                        &larr;
                    </button>

                </div>

                {/* this is for the active side */}
                <div className = "flex flex-col gap-1">
                    <span className = "text-xs text-center text-[var(--color-neutral-400)]"> Active </span>
                    <div className = "w-96 rounded-md overflow-hidden bg-card border border-border">
                        <div className = "flex items-center justify-between px-3 py-2 gap-2 border-b border-border">
                            <Input
                                placeholder = "Search..."
                                value = {activeSearch}
                                onChange = {(clickSearch) => setActiveSearch(clickSearch.target.value)}
                                className = "h-5 text-xs border-none shadow-none px-1 bg-transparent focus-visible:ring-0 placeholder:text-neutral-500 text-foreground"
                            />

                            <button
                                onClick = {() => setActiveSearch('')}
                                className = "text-xs text-[var(--color-neutral-400)]"> x 
                            </button>

                        </div>
                        <ul className = "p-2 min-h-96 space-y-1">
                            {activeFiltered.map((resource) => 
                                (
                                    <div
                                        key = {resource.id}
                                        className = {`flex items-center justify-between px-3 py-2 rounded text-sm cursor-pointer transition-colors $ {selected === resource.id ? 'bg-sidebar-primary-foreground text-primary-foreground' : 'text-sidebar-border hover:bg-border'}`}>

                                            <button
                                                onClick = {() => setSelected(resource.id)}
                                                className = "flex-1 text-left bg-transparent border-none text-inherit text-sm"> {resource.name} 
                                            </button>

                                            <button
                                                onClick = {(clicking) => { clicking.stopPropagation(); handleRemove(resource); }}
                                                className = "text-base leading-none ml-2 text-[var(--color-error-400)] hover:text-[var(--color-error-300)]"> - 
                                            </button>

                                    </div>
                                )
                            )}
                        </ul>
                    </div>
                </div>

            </div>

            {/* this is a popup for the users to confirm/cancel their actions */}
            {pending && (
                <div className = "fixed inset-0 flex items-center justify-center z-50 bg-black/60">
                    <div className = "rounded-lg shadow-xl p-8 w-80 text-center bg-card border border-border">
                        <h3 className = "text-lg font-bold mb-3 text-muted"> Are you sure? </h3>
                        <p className = "text-sm leading-relaxed text-[var(--color-neutral-400)]"> {confirmationText} </p>

                        <div className = "flex gap-3 justify-center">
                            <button 
                                onClick = {handleCancel}
                                className = "px-6 py-2 text-sm font-medium rounded transition-colors border border-sidebar-foreground text-muted hover:bg-border"> cancel 
                            </button>

                            <button 
                                onClick = {handleConfirm}
                                className = "px-6 py-2 text-sm font-medium rounded transition-colors border border-muted text-foreground hover:bg-muted-foreground"> confirm 
                            </button>

                        </div>
                    </div>
                </div>
            )}

        </div>
    );
}