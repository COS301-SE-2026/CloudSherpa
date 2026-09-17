"use client";

import React, { useEffect, useState, useMemo } from "react";
import { Separator } from "@/components/atoms/separator";
import { Input } from "@/components/atoms/input";
import { Button } from "@/components/atoms/button";
import { Card, CardContent } from "@/components/atoms/card";
import { ArrowLeft, ExternalLink, Pencil, Info } from "lucide-react";
import {
    Tooltip,
    TooltipContent,
    TooltipProvider,
    TooltipTrigger,
} from "@/components/atoms/tooltip";
import { useParams, useRouter } from "next/navigation";
import {
    getAwsAccount,
    getAwsAccountResources,
    updateAwsAccountName,
} from "@/lib/fetch/cloud-account-api";
import { CloudAccountDetails } from "@/lib/fetch/dto/cloud-account";
import { CloudResource, ResourceStatus } from "@/lib/fetch/dto/cloud-resource";
import {
    IngestionSlider,
    formattingSecond,
    calculatingIngestionPeriod,
} from "@/features/connectionManager/components/connectionManager/wizardSetup/stepThree";
import { Checkbox } from "@/components/atoms/checkbox";
import { Label } from "@/components/atoms/label";
import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from "@/components/atoms/select";

/*
- the user should be able to veiw details about a particular connectio here
- they should also be able to see the resources assoc with this connection (the ones that are active)
- they should be able to configure this connection - by changing the name of the connection
- they should also be able to go to the resource manager from this page
*/

type Budget = "free" | "custom";

export default function ConfigureConnection() {
    const params = useParams();
    const router = useRouter();

    const accountId = params.connectionId as string;

    const [account, setAccount] = useState<CloudAccountDetails | null>(null);

    const [resources, setResources] = useState<CloudResource[]>([]);

    const [connectionName, setConnectionName] = useState("");

    const [newName, setNewName] = useState("");

    const [loading, setLoading] = useState(true);

    const [isChanging, setIsChanging] = useState(false);

    const [ingestionPeriod, setIngestionPeriod] = useState<number | null>(null);

    const [resourceDiscovery, setResourceDiscovery] = useState(false);

    const [monitorNewResources, setMonitorNewResources] = useState(false);

    const [adjustInterval, setAdjustInterval] = useState(false);

    const [budget, setBudget] = useState<Budget | "">("");

    const [custom, setCustom] = useState("");

    async function loadConnection() {
        try {
            const [accountResponse, resourcesResponse] = await Promise.all([
                getAwsAccount(accountId),
                getAwsAccountResources(accountId),
            ]);

            setAccount(accountResponse);

            setConnectionName(accountResponse.displayName);
            setNewName(accountResponse.displayName);

            setResources(
                resourcesResponse.filter((resource) => resource.status === ResourceStatus.ACTIVE)
            );
        } catch (err) {
            console.error("Failed to load connection", err);
        } finally {
            setLoading(false);
        }
    }

    useEffect(() => {
        // eslint-disable-next-line react-hooks/set-state-in-effect
        void loadConnection();
    }, [accountId]);

    const handlingEditing = () => {
        setNewName(connectionName);
        setIsChanging(true);
    };

    const handlingSave = async () => {
        if (newName.trim() === connectionName) {
            //no name change, so we don't send a request
            setIsChanging(false);
            return;
        }

        try {
            await updateAwsAccountName(accountId, newName.trim());

            setConnectionName(newName.trim());
            setIsChanging(false);
        } catch (err) {
            console.error("Failed to update connection name", err);
        }
    };

    const handlingCancel = () => {
        setNewName(connectionName);
        setIsChanging(false);
    };

    const activeCount = resources.length;

    const recIngestionPeriod = useMemo(
        () => calculatingIngestionPeriod(activeCount),
        [activeCount]
    );

    const accurateIngestionPeriod = ingestionPeriod ?? recIngestionPeriod;

    const handlingAdjust = (checked: boolean) => {
        setAdjustInterval(checked);

        if (!checked) {
            setIngestionPeriod(null);
        }
    };

    const handlingBudget = (choice: Budget) => {
        setBudget(choice);

        if (choice === "free") {
            setCustom("");

            setIngestionPeriod(null);
        }
    };

    if (loading) {
        return <div className="flex h-screen items-center justify-center">Loading...</div>;
    } else
        return (
            <div className="min-h-screen bg-background text-foreground">
                {/* this is fro the heading of the page */}
                <div className="px-8 py-4 border-b border-border">
                    <div className="flex items-center gap-3">
                        <Button
                            variant="ghost"
                            size="icon"
                            onClick={() => router.push("/manageConnections")}
                            className="text-muted-foreground hover:text-foreground h-8 w-8"
                        >
                            <ArrowLeft size={18} />
                        </Button>

                        <div>
                            <h1 className="text-xl font-semibold text-foreground">
                                {" "}
                                {connectionName}{" "}
                            </h1>
                        </div>
                    </div>
                </div>

                <div className="px-8 py-8">
                    {/* this is for the connection details */}
                    <div className="flex items-center gap-2 mb-3">
                        <h2 className="text-base font-medium text-foreground">
                            {" "}
                            Connection details{" "}
                        </h2>

                        <TooltipProvider>
                            <Tooltip>
                                <TooltipTrigger>
                                    <Info
                                        size={15}
                                        className="text-muted-foreground cursor-pointer"
                                    />
                                </TooltipTrigger>

                                <TooltipContent className="max-w-xs flex flex-col gap-2">
                                    <p className="font-medium text-sm">
                                        {" "}
                                        Configuration of connection{" "}
                                    </p>
                                    <p className="text-xs leading-relaxed">
                                        {" "}
                                        A connection links your cloud provider account to
                                        CloudSherpa for cost monitoring{" "}
                                    </p>
                                    <p className="text-xs leading-relaxed">
                                        {" "}
                                        You can rename this connection at any time using the pencil
                                        icon next to the connection name{" "}
                                    </p>
                                </TooltipContent>
                            </Tooltip>
                        </TooltipProvider>
                    </div>
                    <Card className="mb-8 bg-card border-border">
                        <CardContent className="p-0">
                            <div className="flex items-center justify-between px-5 py-3">
                                <span className="text-sm text-muted-foreground">
                                    {" "}
                                    Connection name{" "}
                                </span>

                                <div className="flex items-center gap-2">
                                    {isChanging ? (
                                        <>
                                            <Input
                                                autoFocus
                                                value={newName}
                                                onChange={(change) =>
                                                    setNewName(change.target.value)
                                                }
                                                className="h-7 text-sm w-36 bg-transparent border-border text-foreground focus-visible:ring-ring"
                                            />

                                            <Button
                                                size="sm"
                                                onClick={handlingSave}
                                                className="h-7 text-xs px-3 bg-primary text-primary-foreground hover:bg-primary/90"
                                            >
                                                {" "}
                                                Save{" "}
                                            </Button>

                                            <Button
                                                variant="ghost"
                                                size="sm"
                                                onClick={handlingCancel}
                                                className="h-7 text-xs px-3 text-muted-foreground hover:text-foreground"
                                            >
                                                {" "}
                                                Cancel{" "}
                                            </Button>
                                        </>
                                    ) : (
                                        <>
                                            <span className="text-sm text-foreground border border-border rounded px-2 py-0.5">
                                                {" "}
                                                {connectionName}{" "}
                                            </span>

                                            <Button
                                                variant="ghost"
                                                size="icon"
                                                onClick={handlingEditing}
                                                className="h-6 w-6 text-muted-foreground hover:text-foreground"
                                            >
                                                {" "}
                                                <Pencil size={14} />{" "}
                                            </Button>
                                        </>
                                    )}
                                </div>
                            </div>

                            <Separator className="bg-border" />

                            <div className="flex items-center justify-between px-5 py-3">
                                <span className="text-sm text-muted-foreground"> Provider </span>

                                <span className="text-xs font-medium px-3 py-1 rounded bg-success text-success-foreground">
                                    {" "}
                                    {account?.accountType.replace("_", " ")}{" "}
                                </span>
                            </div>

                            <Separator className="bg-border" />

                            <div className="flex items-center justify-between px-5 py-3">
                                <span className="text-sm text-muted-foreground">
                                    {" "}
                                    Account linked{" "}
                                </span>

                                <span className="text-sm text-foreground">
                                    {" "}
                                    {account?.accountEmail}{" "}
                                </span>
                            </div>
                        </CardContent>
                    </Card>

                    <div className="flex items-center gap-2 mb-3">
                        <h2 className="text-base font-medium text-foreground"> Settings </h2>
                    </div>

                    <Card className="mb-8 bg-card border-border">
                        <CardContent className="pt-6 space-y-6">
                            <div className="space-y-4">
                                <div className="flex items-start gap-3">
                                    <Checkbox
                                        id="resource-discovery"
                                        checked={resourceDiscovery}
                                        onCheckedChange={(checkedBox) =>
                                            setResourceDiscovery(checkedBox === true)
                                        }
                                        className="mt-0.5"
                                    />

                                    <div className="space-y-1">
                                        <Label
                                            htmlFor="resource-discovery"
                                            className="text-sm font-medium text-foreground cursor-pointer"
                                        >
                                            {" "}
                                            Enable automatic resource discovery{" "}
                                        </Label>

                                        <p className="text-sm text-muted-foreground leading-relaxed">
                                            {" "}
                                            This setting allows CloudSherpa to periodically scan
                                            your provider cloud account to find recently added
                                            resources.
                                        </p>
                                    </div>
                                </div>

                                <div className="flex items-start gap-3">
                                    <Checkbox
                                        id="monitor-new-resources"
                                        checked={monitorNewResources}
                                        onCheckedChange={(checkedBox) =>
                                            setMonitorNewResources(checkedBox === true)
                                        }
                                        className="mt-0.5"
                                    />

                                    <div className="space-y-1">
                                        <Label
                                            htmlFor="monitor-new-resources"
                                            className="text-sm font-medium text-foreground cursor-pointer"
                                        >
                                            {" "}
                                            Automatically monitor newly discovered resources{" "}
                                        </Label>

                                        <p className="text-sm text-muted-foreground leading-relaxed">
                                            {" "}
                                            When selected, new resources added to CloudSherpa during
                                            automatic and manual resource discovery will be set to
                                            &quot;active&quot; and have metrics ingested. It is
                                            highly recommended that this setting be enabled in
                                            conjunction with &quot;Automatic ingestion ingestion
                                            interval adjustment&quot; to avoid unexpected cloud
                                            costs associated with API free tier limit, as adding
                                            active resources without adjusting the ingestion
                                            interval leads to an increase in requests to cloud
                                            providers.
                                        </p>
                                    </div>
                                </div>

                                <div className="flex items-start gap-3">
                                    <Checkbox
                                        id="adjust-interval"
                                        checked={adjustInterval}
                                        onCheckedChange={(checked) =>
                                            handlingAdjust(checked === true)
                                        }
                                        className="mt-0.5"
                                    />

                                    <div className="space-y-1 flex-1">
                                        <Label
                                            htmlFor="adjust-interval"
                                            className="text-sm font-medium text-foreground cursor-pointer"
                                        >
                                            {" "}
                                            Automatic ingestion interval adjustment{" "}
                                        </Label>

                                        <p className="text-sm text-muted-foreground leading-relaxed">
                                            {" "}
                                            When enabled, CloudSherpa will automatically adjust your
                                            ingestion interval based on the number of resources as
                                            well as metrics to stay within a specified cost range.
                                        </p>

                                        {adjustInterval && (
                                            <div className="flex items-center gap-2 pt-2">
                                                <Select
                                                    value={budget}
                                                    onValueChange={(value) =>
                                                        handlingBudget(value as Budget)
                                                    }
                                                >
                                                    <SelectTrigger className="h-7 text-xs w-28 bg-transparent border-border text-foreground focus:ring-ring">
                                                        {" "}
                                                        <SelectValue placeholder="Select" />{" "}
                                                    </SelectTrigger>

                                                    <SelectContent>
                                                        <SelectItem value="free"> Free </SelectItem>

                                                        <SelectItem value="custom">
                                                            {" "}
                                                            Custom{" "}
                                                        </SelectItem>
                                                    </SelectContent>
                                                </Select>

                                                {budget === "custom" && (
                                                    <div className="flex items-center gap-1.5">
                                                        <Input
                                                            type="number"
                                                            min={0}
                                                            step="0.1"
                                                            placeholder="0.00"
                                                            value={custom}
                                                            onChange={(change) =>
                                                                setCustom(change.target.value)
                                                            }
                                                            className="h-9 text-sm w-24 bg-transparent border-border text-foreground focus-visible:ring-ring"
                                                        />

                                                        <span className="text-sm text-muted-foreground">
                                                            {" "}
                                                            / month{" "}
                                                        </span>
                                                    </div>
                                                )}
                                            </div>
                                        )}
                                    </div>
                                </div>
                            </div>

                            <IngestionSlider
                                ingestionPeriod={accurateIngestionPeriod}
                                setIngestionPeriod={setIngestionPeriod}
                                activeCount={activeCount}
                                recIngestionPeriod={recIngestionPeriod}
                                formatSeconds={formattingSecond}
                            />
                        </CardContent>
                    </Card>

                    {/* this is for the attached resources */}
                    <div className="flex items-center justify-between mb-3">
                        <h2 className="text-base font-medium text-foreground">
                            {" "}
                            Attached resources{" "}
                        </h2>

                        <span className="text-sm font-medium text-success">
                            {" "}
                            {resources.length} resources{" "}
                        </span>
                    </div>

                    <Card className="bg-card border-border">
                        <CardContent className="p-0">
                            {resources.map((resource, index) => (
                                <React.Fragment key={resource.id}>
                                    <div className="flex items-center justify-between px-5 py-3">
                                        <span className="text-sm text-foreground">
                                            {" "}
                                            {resource.resourceName}{" "}
                                        </span>
                                        <Button
                                            variant="ghost"
                                            size="icon"
                                            onClick={() =>
                                                router.push(
                                                    `/manageConnections/${accountId}/resources`
                                                )
                                            }
                                            className="h-6 w-6 text-muted-foreground hover:text-foreground"
                                        >
                                            <ExternalLink size={15} />
                                        </Button>{" "}
                                    </div>

                                    {index !== resources.length - 1 && (
                                        <Separator className="bg-border" />
                                    )}
                                </React.Fragment>
                            ))}
                        </CardContent>
                    </Card>
                </div>
            </div>
        );
}
