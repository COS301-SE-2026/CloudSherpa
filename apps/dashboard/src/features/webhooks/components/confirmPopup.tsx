"use client";

import {useState} from "react";
import {Copy, Check} from "lucide-react";
import {Button} from "@/components/atoms/button";
import {Input} from "@/components/atoms/input";
import {Label} from "@/components/atoms/label";
import {Checkbox} from "@/components/atoms/checkbox";
import {Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter, DialogDescription} from "@/components/atoms/dialog";

interface PropsForPopup{
    secret : string;
    onClose : () => void;
}

export const Popup = ({secret, onClose} : PropsForPopup) => {
    const [saved, setSaved] = useState(false);

    const [copied, setCopied] = useState(false);

    const handlingCopy = () => {
        navigator.clipboard.writeText(secret);

        setCopied(true);

        setTimeout(() => setCopied(false), 2000);
    };

    return(
        <Dialog open = {true} onOpenChange = {onClose}>
            <DialogContent className = "max-w-md">
                <DialogHeader>
                    <DialogTitle> Save your signing secret </DialogTitle>

                    <DialogDescription> This secret is shown once. Copy it now and store it securely in your receiving application. </DialogDescription>
                </DialogHeader>

                <DialogFooter>

                </DialogFooter>
            </DialogContent>
        </Dialog>
    );
};