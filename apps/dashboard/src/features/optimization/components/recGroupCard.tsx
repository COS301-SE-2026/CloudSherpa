import { Card, CardHeader, CardTitle } from "@/components/atoms/card";
import { Badge } from "@/components/atoms/badge";
import RecDrawer from "@/features/optimization/components/recDrawer";
import { RecommendationGroup } from "@/features/optimization/types/recommendations";
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuTrigger,
} from "@/components/atoms/dropdown-menu";
import {
    ContextMenu,
    ContextMenuContent,
    ContextMenuTrigger,
    ContextMenuItem,
} from "@/components/atoms/context-menu";
import { EllipsisVertical, Eye } from "lucide-react";
import { Button } from "@/components/atoms/button";
import { useState } from "react";
import { useRecStore } from "@/features/optimization/stores/useRecStore";

interface RecommendationGroupCardProps {
    group: RecommendationGroup;
}

export default function RecommendationGroupCard({ group }: Readonly<RecommendationGroupCardProps>) {
    const recommendationsCount = group.recommendations.length;

    const provider = recommendationsCount > 0 ? group.recommendations[0].provider : "Unknown";

    const focusedAccountId = useRecStore((state) => state.focusedAccountId);

    const [isDrawerOpen, setIsDrawerOpen] = useState(focusedAccountId === group.accountId);

    const handleOpenDrawer = () => setIsDrawerOpen(true);

    const [prevFocusedId, setPrevFocusedId] = useState(focusedAccountId);

    if (focusedAccountId !== prevFocusedId) {
        setPrevFocusedId(focusedAccountId);
        if (focusedAccountId === group.accountId) {
            setIsDrawerOpen(true);
        }
    }

    return (
        <ContextMenu>
            <ContextMenuTrigger>
                <Card
                    className="flex flex-col justify-between cursor-pointer hover:bg-muted/50"
                    onClick={handleOpenDrawer}
                >
                    <CardHeader className="flex flex-row justify-start items-start gap-2">
                        <div className="w-full flex flex-row justify-between items-center gap-2">
                            <div className="flex flex-row justify-start items-center gap-2">
                                <CardTitle>{group.displayName} </CardTitle>
                                <div className="flex flex-row pl-2 gap-2">
                                    <Badge>{provider}</Badge>
                                    <Badge variant="secondary">
                                        {recommendationsCount} Recommendation
                                        {recommendationsCount !== 1 ? "s" : ""}
                                    </Badge>
                                </div>
                            </div>
                            <DropdownMenu>
                                <DropdownMenuTrigger asChild>
                                    <Button
                                        variant="ghost"
                                        size="icon"
                                        className="h-8 w-8"
                                        onClick={(e) => e.stopPropagation()}
                                    >
                                        <EllipsisVertical className="h-4 w-4" />
                                    </Button>
                                </DropdownMenuTrigger>
                                <DropdownMenuContent align="end" className="w-fit">
                                    <DropdownMenuItem
                                        onClick={(e) => {
                                            e.stopPropagation();
                                            handleOpenDrawer();
                                        }}
                                    >
                                        <Eye className="mr-2 h-4 w-4" />
                                        View Recommendations
                                    </DropdownMenuItem>
                                </DropdownMenuContent>
                            </DropdownMenu>
                        </div>
                    </CardHeader>
                    <RecDrawer group={group} isOpen={isDrawerOpen} setIsOpen={setIsDrawerOpen} />
                </Card>
            </ContextMenuTrigger>
            <ContextMenuContent>
                <ContextMenuItem onClick={handleOpenDrawer}>
                    <Eye className="mr-2 h-4 w-4" />
                    View Recommendations
                </ContextMenuItem>
            </ContextMenuContent>
        </ContextMenu>
    );
}
