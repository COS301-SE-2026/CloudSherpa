import { Card, CardFooter, CardHeader, CardTitle } from "@/components/atoms/card";
import { Badge } from "@/components/atoms/badge";
import RecDrawer from "@/features/optimization/components/recDrawer";
import { RecommendationGroup } from "@/features/optimization/types/recommendations";
import { Separator } from "@/components/atoms/separator";
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

interface RecommendationGroupCardProps {
    group: RecommendationGroup;
}

export default function RecommendationGroupCard({ group }: Readonly<RecommendationGroupCardProps>) {
    const [isDrawerOpen, setIsDrawerOpen] = useState(false);
    const handleOpenDrawer = () => setIsDrawerOpen(true);

    const recommendationsCount = group.recommendations.length;

    const provider = recommendationsCount > 0 ? group.recommendations[0].provider : "Unknown";

    return (
        <ContextMenu>
            <ContextMenuTrigger>
                <Card
                    className="h-50 flex flex-col justify-between cursor-pointer hover:bg-muted/50"
                    onClick={handleOpenDrawer}
                >
                    <CardHeader className="flex flex-col justify-start items-start gap-2">
                        <div className="w-full flex flex-row justify-between items-center">
                            <CardTitle>{group.displayName} </CardTitle>
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
                        <Badge>{provider}</Badge>
                    </CardHeader>
                    <CardFooter className="w-full flex flex-col justify-start gap-2">
                        <Separator />
                        <div className="flex flex-row justify-end items-center w-full">
                            <span className="text-muted-foreground text-s">
                                {recommendationsCount} recommendations
                            </span>
                        </div>
                    </CardFooter>
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
