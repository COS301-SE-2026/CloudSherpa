"use client";

import { useState } from "react";
import { Copy, Check } from "lucide-react";
import { Button } from "@/components/atoms/button";
import { Input } from "@/components/atoms/input";
import { Label } from "@/components/atoms/label";
import { Checkbox } from "@/components/atoms/checkbox";
import {
    Dialog,
    DialogContent,
    DialogHeader,
    DialogTitle,
    DialogFooter,
    DialogDescription,
} from "@/components/atoms/dialog";

interface PropsForPopup {
    secret: string;
    onClose: () => void;
}

export const Popup = ({ secret, onClose }: PropsForPopup) => {
    const [saved, setSaved] = useState(false);

    const [copied, setCopied] = useState(false);

    const handlingCopy = () => {
        navigator.clipboard.writeText(secret);

        setCopied(true);

        setTimeout(() => setCopied(false), 2000);
    };

    return (
        <Dialog open={true} onOpenChange={onClose}>
            <DialogContent className="max-w-md">
                <DialogHeader>
                    <DialogTitle> Save your signing secret </DialogTitle>

                    <DialogDescription>
                        {" "}
                        This secret is shown once. Copy it now and store it securely in your
                        receiving application.{" "}
                    </DialogDescription>
                </DialogHeader>

                <div className="space-y-4 py-4">
                    <div className="space-y-2">
                        <Label> HMAC signing secret </Label>

                        <div className="flex gap-2">
                            <Input readOnly value={secret} className="flex-1" />

                            <Button variant="secondary" onClick={handlingCopy}>
                                {copied ? (
                                    <Check size={16} className="mr-2" />
                                ) : (
                                    <Copy size={16} className="mr-2" />
                                )}

                                {copied ? "Copied to clipboard." : "Copy"}
                            </Button>
                        </div>
                    </div>

                    <div className="flex items-center gap-2 mt-6">
                        <Checkbox
                            id="save-secret"
                            checked={saved}
                            onCheckedChange={(change) => setSaved(change as boolean)}
                        />

                        <Label htmlFor="save-secret" className="text-sm cursor-pointer">
                            {" "}
                            I have saved this secret. I understand I can not view it again.{" "}
                        </Label>
                    </div>
                </div>

                <DialogFooter>
                    <Button onClick={onClose} disabled={!saved}>
                        {" "}
                        Done{" "}
                    </Button>
                </DialogFooter>
            </DialogContent>
        </Dialog>
    );
};
