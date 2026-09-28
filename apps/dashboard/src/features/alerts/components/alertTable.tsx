"use client";

import { useMemo, useState } from "react";
import {
    ColumnDef,
    flexRender,
    getCoreRowModel,
    getSortedRowModel,
    SortingState,
    useReactTable,
    getPaginationRowModel,
} from "@tanstack/react-table";
import { Button } from "@/components/atoms/button";
import { Switch } from "@/components/atoms/switch";
import { Table, TableBody, TableCell, TableRow } from "@/components/atoms/table";
import { Bell, BellOff, Info, MoreVertical, Trash2 } from "lucide-react";
import { SEVERITY_COLOURS, STATUS_LABELS, TYPE } from "@/features/alerts/types/alertTypes";
import type { Alert } from "@/features/alerts/types/alertTypes";
import { toast } from "sonner";
import { TableHeaderData } from "@/features/alerts/components/atoms/tableHeaderData";
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuTrigger,
} from "@/components/atoms/dropdown-menu";
import { LABELS_FOR_SCOPE, type ScopeForBudget } from "@/features/budgets/types/budgetTypes";
import { TablePagination } from "@/components/molecules/tablePagination";
import { Truncation } from "@/components/molecules/truncation";

interface AlertScope {
    scope: ScopeForBudget;
    name: string | null;
}

function getScope(alert: Alert): AlertScope {
    const forPayload = (alert.payload ?? {}) as Record<string, unknown>;

    if (alert.alertType === "BUDGET") {
        const forScope = forPayload.budget_scope;

        const scope: ScopeForBudget =
            forScope === "TENANT" || forScope === "ACCOUNT" || forScope === "RESOURCE"
                ? forScope
                : "TENANT";

        const name = (forPayload.scope_name as string | undefined) ?? null;

        return { scope, name };
    }

    const name = (forPayload.resource_name as string | undefined) ?? null;

    return { scope: "RESOURCE", name };
}

interface PropsForAlertsTable {
    alerts: Alert[];
    onToggle: (alert: Alert, enabled: boolean) => Promise<boolean>;
    onToggleNotificationSilence: (alert: Alert) => Promise<void>;
    info: (alert: Alert) => void;
    onDelete: (alert: Alert) => void;
    globalInAppNotificationsEnabled: boolean;
}

interface ForColumns {
    onToggle: (alert: Alert, enabled: boolean) => Promise<boolean>;
    onToggleNotificationSilence: (alert: Alert) => Promise<void>;
    info: (alert: Alert) => void;
    onDelete: (alert: Alert) => void;
    globalInAppNotificationsEnabled: boolean;
}

function cleanAlertTitle(title: string): string {
    const cleaned = title.trim();

    if (!cleaned.endsWith(")")) {
        return cleaned;
    }

    const tagStartIndex = cleaned.toLowerCase().lastIndexOf("(z=");

    if (tagStartIndex !== -1) {
        const insideTag = cleaned.slice(tagStartIndex + 3, -1);

        if (insideTag.length > 0 && !insideTag.includes(")")) {
            return cleaned.slice(0, tagStartIndex).trim();
        }
    }

    return cleaned;
}

function notificationLabel(
    alertDisabled: boolean,
    globalInAppNotificationsEnabled: boolean,
    individuallyMuted: boolean
): string {
    if (alertDisabled) {
        return "Alert is disabled";
    }

    if (!globalInAppNotificationsEnabled) {
        return "In-app notifications are globally muted";
    }

    if (individuallyMuted) {
        return "Enable in-app notifications";
    }

    return "Mute in-app notifications";
}

function helperForColumns({
    onToggle,
    onToggleNotificationSilence,
    globalInAppNotificationsEnabled,
    info,
    onDelete,
}: ForColumns): ColumnDef<Alert>[] {
    return [
        {
            id: "enabled",
            header: () => "STATUS",
            enableSorting: false,
            size: 120,
            cell: ({ row }) => {
                const active = row.original.status === "ACTIVE";

                const handlingToggle = async (checked: boolean) => {
                    try {
                        const changed = await onToggle(row.original, checked);

                        if (changed) {
                            toast.success(checked ? "Alert enabled" : "Alert disabled");
                        }
                    } catch {
                        toast.error(checked ? "Failed to enable alert" : "Failed to disable alert");
                    }
                };

                return (
                    <div className="flex items-center gap-2">
                        <Switch checked={active} onCheckedChange={handlingToggle} />

                        <span className="text-xs text-muted-foreground">
                            {STATUS_LABELS[row.original.status]}
                        </span>
                    </div>
                );
            },
        },
        {
            id: "notifications",
            header: () => <span className = "block text-center"> NOTIFICATIONS </span>,
            enableSorting: false,
            cell: ({ row }) => {
                const alertDisabled = row.original.status !== "ACTIVE";
                const individuallyMuted = row.original.inAppNotificationsSilenced === true;
                const muted =
                    alertDisabled || !globalInAppNotificationsEnabled || individuallyMuted;

                const label = notificationLabel(
                    alertDisabled,
                    globalInAppNotificationsEnabled,
                    individuallyMuted
                );

                return (
                    <div className = "flex items-center justify-center">
                        <Button
                            variant="ghost"
                            size="sm"
                            type="button"
                            disabled={alertDisabled || !globalInAppNotificationsEnabled}
                            className="h-8 w-8 p-0 text-muted-foreground hover:text-foreground"
                            title={label}
                            aria-label={label}
                            onClick={() => void onToggleNotificationSilence(row.original)}
                        >
                            {muted ? (
                                <BellOff className="h-4 w-4" />
                            ) : (
                                <Bell className="h-4 w-4 text-success" />
                            )}
                        </Button>
                    </div>
                );
            },
        },
        {
            accessorKey: "severity",
            header: () => "SEVERITY",
            size: 110,
            cell: ({ row }) => {
                const inactive = row.original.status !== "ACTIVE";

                const colours = inactive
                    ? "text-muted-foreground"
                    : SEVERITY_COLOURS[row.original.severity];

                return (
                    <span className={`text-xs font-semibold uppercase tracking-wider ${colours}`}>
                        {row.original.severity}
                    </span>
                );
            },
        },
        {
            accessorKey: "alertType",
            header: () => "TYPE",
            size: 110,
            cell: ({ row }) => (
                <span className="text-xs text-muted-foreground">
                    {TYPE[row.original.alertType]}
                </span>
            ),
        },
        {
            id: "scope",
            header: () => "SCOPE",
            enableSorting: false,
            size: 180,
            cell: ({ row }) => {
                const alert = row.original;

                const inactive = alert.status !== "ACTIVE";

                const { scope, name } = getScope(alert);

                const labelForScope = LABELS_FOR_SCOPE[scope];

                return (
                    <div
                        className={`flex flex-col text-sm ${inactive ? "text-muted-foreground" : "text-foreground"}`}
                    >
                        <Truncation text={labelForScope} />

                        {name && <Truncation text={name} className="text-muted-foreground" />}
                    </div>
                );
            },
        },

        {
            accessorKey: "title",
            header: () => "ALERT",
            size: 200,
            cell: ({ row }) => {
                const inactive = row.original.status !== "ACTIVE";
                const displayTitle = cleanAlertTitle(row.original.title);

                return (
                    <Truncation
                        text={displayTitle}
                        className={inactive ? "text-muted-foreground" : "text-foreground"}
                    />
                );
            },
        },
        {
            accessorKey: "lastSeen",
            header: () => "UPDATED AT",
            size: 160,
            cell: ({ row }) => {
                const updatedAt = row.original.lastSeen ?? row.original.createdAt;

                return (
                    <span className="text-xs text-muted-foreground">
                        {updatedAt ? new Date(updatedAt).toLocaleString() : "-"}
                    </span>
                );
            },
        },
        {
            id: "info",
            header: () => "",
            enableSorting: false,
            size: 60,
            cell: ({ row }) => {
                const alert = row.original;

                return (
                    <div className = "flex justify-center">
                        <DropdownMenu>
                            <DropdownMenuTrigger asChild>
                                <Button
                                    variant="ghost"
                                    size="sm"
                                    className="h-auto p-0 text-muted-foreground hover:text-foreground"
                                >
                                    <MoreVertical className="h-4 w-4" />
                                </Button>
                            </DropdownMenuTrigger>

                            <DropdownMenuContent align="end" className="w-40">
                                <DropdownMenuItem
                                    onClick={() => info(alert)}
                                    className="cursor-pointer"
                                >
                                    <Info className="mr-2 h-4 w-4" />
                                    More info
                                </DropdownMenuItem>

                                <DropdownMenuItem
                                    onClick={() => onDelete(alert)}
                                    className="cursor-pointer text-destructive focus:text-destructive"
                                >
                                    <Trash2 className="mr-2 h-4 w-4" />
                                    Delete
                                </DropdownMenuItem>
                            </DropdownMenuContent>
                        </DropdownMenu>
                    </div>
                );
            },
        },
    ];
}

export function AlertTable({
    alerts,
    onToggle,
    onToggleNotificationSilence,
    globalInAppNotificationsEnabled,
    info,
    onDelete,
}: Readonly<PropsForAlertsTable>) {
    const [sorting, setSorting] = useState<SortingState>([]);

    const columns = useMemo(
        () =>
            helperForColumns({
                onToggle,
                onToggleNotificationSilence,
                globalInAppNotificationsEnabled,
                info,
                onDelete,
            }),
        [onToggle, onToggleNotificationSilence, globalInAppNotificationsEnabled, info, onDelete]
    );

    const tableForAlerts = useReactTable({
        data: alerts,
        columns,
        state: { sorting },
        onSortingChange: setSorting,
        getCoreRowModel: getCoreRowModel(),
        getSortedRowModel: getSortedRowModel(),
        getPaginationRowModel: getPaginationRowModel(),
        initialState: { pagination: { pageSize: 10 } },
    });

    return (
        <div className="overflow-hidden rounded-lg border border-border bg-card text-card-foreground">
            <Table className="table-fixed">
                <TableHeaderData headerGroups={tableForAlerts.getHeaderGroups()} />

                <TableBody>
                    {tableForAlerts.getRowModel().rows.length === 0 ? (
                        <TableRow>
                            <TableCell
                                colSpan={columns.length}
                                className="py-8 text-center text-sm text-muted-foreground"
                            >
                                No alerts found
                            </TableCell>
                        </TableRow>
                    ) : (
                        tableForAlerts.getRowModel().rows.map((row) => (
                            <TableRow key={row.id}>
                                {row.getVisibleCells().map((cell) => (
                                    <TableCell key={cell.id} className="text-sm">
                                        {flexRender(cell.column.columnDef.cell, cell.getContext())}
                                    </TableCell>
                                ))}
                            </TableRow>
                        ))
                    )}
                </TableBody>
            </Table>

            <TablePagination forTable={tableForAlerts} />
        </div>
    );
}
