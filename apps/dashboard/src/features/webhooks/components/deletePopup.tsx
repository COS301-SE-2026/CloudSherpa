"use client";

import{AlertDialog, AlertDialogAction, AlertDialogCancel, AlertDialogContent, AlertDialogDescription, AlertDialogFooter, AlertDialogHeader, AlertDialogTitle} from "@/components/atoms/alert-dialog";

interface PropsForDelete{
    isOpen : boolean;
    webhookName : string;
    onCancel : () => void;
    onConfirm : () => void;
}

export const DeletePopup = ({
    isOpen, webhookName, onCancel, onConfirm,
} : PropsForDelete) => {
    return(
        <AlertDialog open = {isOpen} onOpenChange = {(open) => !open && onCancel()}>
            <AlertDialogContent>
                <AlertDialogHeader>
                    <AlertDialogTitle> Delete webhook? </AlertDialogTitle>

                    <AlertDialogDescription>
                        Delete "{webhookName}"? It will no longer receive events.
                    </AlertDialogDescription>
                </AlertDialogHeader>

                <AlertDialogFooter>
                    <AlertDialogCancel onClick = {onCancel}> Cancel </AlertDialogCancel>

                    <AlertDialogAction onClick = {onConfirm} className = "bg-destructive text-destructive-foreground hover:bg-destructive/90"> Delete webhook </AlertDialogAction>
                </AlertDialogFooter>
            </AlertDialogContent>
        </AlertDialog>
    );
};