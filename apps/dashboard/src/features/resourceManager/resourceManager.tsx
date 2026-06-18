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
        <div className = "min-h-screen bg-white flex flex-col items-center justify-center p-8">

            <h2 className = "text-lg font-medium tracking-tight text-black mb-8"> Resource Manager </h2>

            <div className = "flex items-center gap-4">

                {/* this is for the inactive side */}
                <div className = "flex flex-col gap-1">
                    <span className = "text-xs text-black text-center"> Inactive </span>
                    <div className = "w-96 bg-white border border-black rounded-md overflow-hidden">
                        <div className = "flex items-center justify-between px-3 py-2 border-b border-black gap-2">
                            <Input
                                placeholder = "Search..."
                                value = {inactiveSearch}
                                onChange = {(clickSearch) => setInactiveSearch(clickSearch.target.value)}
                                className = "h-5 text-xs border-none shadow-none px-1 bg-transparent focus-visible:ring-0 text-black placeholder:text-black/50"
                            />

                            <button
                                onClick = {() => setInactiveSearch('')}
                                className = "text-black text-xs"> x 
                            </button>

                        </div>

                        <ul className = "p-2 min-h-96 space-y-1">
                            {inactiveFiltered.map((resource) =>
                                (
                                    <li 
                                        key = {resource.id}
                                        onClick = {() => setSelected(resource.id)}
                                        className = {`flex items-center justify-between px-3 py-2 rounded text-sm cursor-pointer transition-colors ${
                                            selected === resource.id ? 'bg-black text-white' : 'bg-white text-black hover:bg-black/10'}`}>
                                        {resource.name}

                                        <button
                                            onClick = {(clickButton) => {
                                                clickButton.stopPropagation(); 
                                                handleAdd(resource);
                                            }}

                                            className = "text-base leading-none ml-2"

                                        > + </button>
                                    </li>
                                )
                            )}
                        </ul>
                    </div>
                </div>
                
                {/* arrows */}
                <div className = "flex flex-col gap-2 mt-5">

                    <button className = "w-8 h-8 p-0 border-2 border-black bg-white text-black rounded-md flex items-center justify-center">
                        &rarr;
                    </button>

                    <button className = "w-8 h-8 p-0 border-2 border-black bg-white text-black rounded-md flex items-center justify-center">
                        &larr;
                    </button>

                </div>

                {/* this is for the active side */}
                <div className = "flex flex-col gap-1">
                    <span className = "text-xs text-black text-center"> Active </span>
                    <div className = "w-96 bg-white border border-black rounded-md overflow-hidden">
                        <div className = "flex items-center justify-between px-3 py-2 border-b border-black gap-2">
                            <Input
                                placeholder = "Search..."
                                value = {activeSearch}
                                onChange = {(clickSearch) => setActiveSearch(clickSearch.target.value)}
                                className = "h-5 text-xs border-none shadow-none px-1 bg-transparent focus-visible:ring-0 text-black placeholder:text-black/50"
                            />

                            <button
                                onClick = {() => setActiveSearch('')}
                                className = "text-black text-xs"> x 
                            </button>

                        </div>
                        <ul className = "p-2 min-h-96 space-y-1">
                            {activeFiltered.map((resource) => 
                                (
                                    <li 
                                        key = {resource.id} 
                                        onClick = {() => setSelected(resource.id)}
                                        className = {`flex items-center justify-between px-3 py-2 rounded text-sm cursor-pointer transition-colors ${
                                            selected === resource.id ? 'bg-black text-white' : 'bg-white text-black hover:bg-black/10'}`}>
                                        {resource.name}

                                        <button
                                            onClick = {(clickButton) => {
                                                clickButton.stopPropagation();
                                                handleRemove(resource);
                                            }}

                                            className = "text-base leading-none ml-2"
                                        > &minus </button>
                                    </li>
                                )
                            )}
                        </ul>
                    </div>
                </div>

            </div>

            {/* this is a popup for the users to confirm/cancel their actions */}
            {pending && (
                <div className = "fixed inset-0 bg-black/50 flex items-center justify-center z-50">
                    <div className = "bg-white border border-black rounded-lg shadow-xl p-8 w-80 text-center">
                        <h3 className = "text-lg font-bold text-black mb-3"> Are you sure? </h3>
                        <p className = "text-sm text-black/70 mb-6 leading-relaxed"> {confirmationText} </p>

                        <div className = "flex gap-3 justify-center">
                            <button 
                                onClick = {handleCancel}
                                className = "px-6 py-2 text-sm font-medium rounded border border-black text-black hover:bg-black/10 transition-colors"> cancel 
                            </button>

                            <button 
                                onClick = {handleConfirm}
                                className = "px-6 py-2 text-sm font-medium rounded bg-black text-white hover:bg-black/80 transition-colors"> confirm 
                            </button>

                        </div>
                    </div>
                </div>
            )}

        </div>
    );
}