"use client"

import React, {useState} from 'react';
import {Separator} from '@/components/atoms/separator';
import {Input} from '@/components/atoms/input';
import {Button} from '@/components/atoms/button';
import {Card, CardContent} from '@/components/atoms/card';
import {ArrowLeft, ExternalLink, Pencil, Info} from 'lucide-react';
import {Tooltip, TooltipContent, TooltipProvider, TooltipTrigger} from '@/components/atoms/tooltip';

/*
- the user should be able to veiw details about a particular connectio here
- they should also be able to see the resources assoc with this connection (the ones that are active)
- they should be able to configure this connection - by changing the name of the connection
- they should also be able to go to the resource manager from this page
*/

export default function ConfigureConnection(){
    const [connectionName, setConnectionName] = useState('Connection 1');

    const [newName, setNewName] = useState('Connection 1');

    const [isChanging, setIsChanging] = useState(false);

    const resources = ['Resource 1', 'Resource 2', 'Resource 3'];

    const handlingEditing = () => {
        setNewName(connectionName);
        setIsChanging(true);
    };

    const handlingSave = () => {
        setConnectionName(newName);
        setIsChanging(false);
    };

    const handlingCancel = () => {
        setNewName(connectionName);
        setIsChanging(false);
    };

    return(
        <div className = "min-h-screen bg-background text-foreground">

            {/* this is fro the heading of the page */}
            <div className = "px-8 py-4 border-b border-border">
                <div className = "flex items-center gap-3">

                    <Button variant = "ghost" size = "icon" className = "text-muted-foreground hover:text-foreground h-8 w-8">
                        <ArrowLeft size = {18} />
                    </Button>

                    <div>
                        <h1 className = "text-xl font-semibold text-foreground"> {connectionName} </h1>
                    </div>

                </div>
            </div>

            <div className = "px-8 py-8">

                {/* this is for the connection details */}
                <div className = "flex items-center gap-2 mb-3">
                    <h2 className = "text-base font-medium text-foreground"> Connection details </h2>

                    <TooltipProvider>
                        <Tooltip>

                            <TooltipTrigger>
                                <Info size = {15} className = "text-muted-foreground cursor-pointer"/>
                            </TooltipTrigger>

                            <TooltipContent className = "max-w-xs flex flex-col gap-2">
                                <p className = "font-medium text-sm"> Configuration of connection </p>
                                <p className = "text-xs text-muted-foreground leading-relaxed"> A connection links your cloud provider account to CloudSherpa for cost monitoring </p>
                                <p className = "text-xs text-muted-foreground leading-relaxed mt-1"> You can rename this connection at any time using the pencil icon next to the connection name </p>
                            </TooltipContent>

                        </Tooltip>
                    </TooltipProvider>

                </div>
                <Card className = "mb-8 bg-card border-border">
                    <CardContent className = "p-0">
                        <div className = "flex items-center justify-between px-5 py-3">
                            <span className = "text-sm text-muted-foreground"> Connection name </span>

                            <div className = "flex items-center gap-2">
                                {isChanging ? (
                                    <>
                                        <Input autoFocus value = {newName} onChange = {(change) => setNewName(change.target.value)} className = "h-7 text-sm w-36 bg-transparent border-border text-foreground focus-visible:ring-ring" />

                                        <Button size = "sm" onClick = {handlingSave} className = "h-7 text-xs px-3 bg-primary text-primary-foreground hover:bg-primary/90"> Save </Button>

                                        <Button variant = "ghost" size = "sm" onClick = {handlingCancel} className = "h-7 text-xs px-3 text-muted-foreground hover:text-foreground"> Cancel </Button>
                                    </>
                                ) : 

                                (
                                    <>
                                        <span className = "text-sm text-foreground border border-border rounded px-2 py-0.5"> {connectionName} </span>

                                        <Button variant = "ghost" size = "icon" onClick = {handlingEditing} className = "h-6 w-6 text-muted-foreground hover:text-foreground"> <Pencil size = {14}/> </Button>
                                    </>
                                )}
                            </div>

                        </div>

                        <Separator className = "bg-border" />

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

                    </CardContent>
                </Card>

                {/* this is for the attached resources */}
                <div className = "flex items-center justify-between mb-3">
                    <h2 className = "text-base font-medium text-foreground"> Attached resources </h2>

                    <span className = "text-sm font-medium text-success"> {resources.length} active </span>
                </div>

                <Card className = "bg-card border-border">
                    <CardContent className = "p-0">
                        {resources.map((resource, index) => (

                            <React.Fragment key = {resource}>
                                <div className = "flex items-center justify-between px-5 py-3">
                                    <span className = "text-sm text-foreground"> {resource} </span>

                                    <Button variant = "ghost" size = "icon" className = "h-6 w-6 text-muted-foreground hover:text-foreground"> <ExternalLink size = {15} /> </Button>
                                </div>

                                {index !== resources.length-1 && <Separator className = "bg-border"/> }
                            </React.Fragment>

                        ))}
                    </CardContent>
                </Card>

            </div>
        </div>
    );
    
}