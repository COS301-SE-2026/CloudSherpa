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
    const [connectionName, setConnectionName] = useState('Connection 1');

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

                        <h1 className = "text-xl font-semibold text-foreground"> {connectionName} </h1>
                    </div>

                </div>
            </div>

            <div className = "px-8 py-8">

                {/* this is for the connection details */}
                <h2 className = "text-base font-medium text-foreground mb-3"> Connection details </h2>
                <Card className = "mb-8 bg-card border-border">
                    <CardContent className = "p-0">
                        <div className = "flex items-center justify-between px-5 py-3">

                            <span className = "text-sm text-muted-foreground" > Provider </span>

                            {/* is hard coded */}
                            <span className = "text-xs font-medium px-3 py-1 rounded bg-success text-success-foreground"> Azure </span>

                        </div>

                        <Separator className = "bg-border"/>

                        <div className = "flex items-center justify-between px-5 py-3">

                            <span className = "text-sm text-muted-foreground"> Account linked </span>

                            {/* is hard coded */}
                            <span className = "text-sm text-foreground" > fi@bitflip.com </span>

                        </div>

                        <Separator className = "bg-border" />

                        <div className = "flex items-center justify-between px-5 py-3">
                            <span className = "text-sm text-muted-foreground"> Connection name </span>

                        </div>

                    </CardContent>
                </Card>
            </div>
        </div>
    );
    
}