"use client"

import React, {useState} from 'react';
import {Separator} from '@/components/atoms/separator';
import {Input} from '@/components/atoms/input';
import {Button} from '@/components/atoms/button';
import {Card, CardContent} from '@/components/atoms/card';
import {ArrowLeft, ExternalLink, Pencil} from 'lucide-react';

/*
- the user should be able to veiw details about a particular connectio here
- they should also be able to see the resources assoc with this connection (the ones that are active)
- they should be able to configure this connection - by changing the name of the connection
- they should also be able to go to the resource manager from this page
*/

export default function ConfigureConnection(){
    return(
        <div className = "min-h-screen bg-background text-foreground">

            {/* this is fro the heading of the page */}
            <div className = "px-8 py-4 border-b border-border">
                <div className = "flex items-center gap-3">

                    <Button variant = "ghost" size = "icon" className = "text-muted-foreground hover:text-foreground h-8 w-8">
                        <ArrowLeft size = {18} />
                    </Button>

                    <div>
                        <p className = "text-xs text-muted-foreground"> Connection manager/configuration </p>

                    </div>

                </div>
            </div>

        </div>
    );
    
}