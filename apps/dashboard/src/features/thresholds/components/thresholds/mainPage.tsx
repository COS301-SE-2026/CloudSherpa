"use client";

import {useMemo, useState} from "react";
import {Button} from "@/components/atoms/button";
import {Card, CardContent} from "@/components/atoms/card";
import {Input} from "@/components/atoms/input";
import {ThresholdPopup} from "@/features/thresholds/components/thresholds/thresholdPopup";
import {ThresholdTable} from "@/features/thresholds/components/thresholds/thresholdTable";
import {useThresholds} from "@/features/thresholds/hooks/useThresholds";
import type {CreateThresholdRequest, Threshold} from "@/features/thresholds/types/thresholdTypes";
import {Tabs, TabsList, TabsTrigger, TabsContent} from "@/components/atoms/tabs";
import {AlertDialog, AlertDialogAction, AlertDialogCancel, AlertDialogContent, AlertDialogDescription, AlertDialogFooter, AlertDialogHeader, AlertDialogTitle} from "@/components/atoms/alert-dialog";
import {Plus} from "lucide-react";

interface PropsForMainPage{
    resourceId : string;
    userId : string;
}

export function MainPage({resourceId, userId} : Readonly<PropsForMainPage>){
    const {thresholds, loading, forError, createThreshold, updateThreshold, removeThreshold,} = useThresholds(resourceId);

    const [search, setSearch] = useState("");

    const [selectedTab, setSelectedTab] = useState<"rules" | "thresholds">("thresholds");

    const [popupOpen, setPopupOpen] = useState(false);

    const[isEditing, setIsEditing] = useState<Threshold | null>(null);

    const [deleteThreshold, setDeleteThreshold] = useState<Threshold | null>(null);

    const forFilters = useMemo(() => thresholds.filter((forThreshold) => forThreshold.metricName.toLowerCase().includes(search.toLowerCase()),), [thresholds, search],);

    const countForEnabled = thresholds.filter((forThreshold) => forThreshold.enabled).length;

    const forTotalCount = thresholds.length;

    return(
        
    );
}