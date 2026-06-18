'use client'
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

export default function ResourceManager(){
    const [inactive, setInactive] = useState<Resource[]>([ {id: '1', name: 'Resource'},]);

    const [active, setActive] = useState<Resource[]>([]);

    return(
        <div className = "min-h-screen bg-white flex flex-col items-center justify-center p-8">

            <h2 className = "text-lg font-medium tracking-tight text-black mb-8"> Resource Manager </h2>

            <div className = "flex items-center gap-4">

                {/* this is for the inactive side */}
                <div className = "flex flex-col gap-1">
                    <span className = "text-xs text-black text-center"> Inactive </span>
                    <div className = "w-96 bg-white border border-black rounded-md overflow-hidden">
                        <ul className = "p-2 min-h-96 space-y-1">
                            {inactive.map((resource) =>
                                (
                                    <li key = {resource.id} className = "flex items-center justify-between px-3 py-2 rounded text-sm bg-white text-black">
                                        {resource.name}
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
                        <ul className = "p-2 min-h-96 space-y-1">
                            {active.map((resource) => 
                                (
                                    <li key = {resource.id} className = "flex items-center justify-between px-3 py-2 rounded text-sm bg-white text-black">
                                        {resource.name}
                                    </li>
                                )
                            )}
                        </ul>
                    </div>
                </div>

            </div>
        </div>
    );
}