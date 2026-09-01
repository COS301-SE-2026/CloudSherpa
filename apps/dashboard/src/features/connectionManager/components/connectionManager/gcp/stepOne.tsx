"use client";

import React, { useState, useRef } from "react";
import { StepOne } from "@/features/connectionManager/components/connectionManager/wizardSetup/stepOne";
import { UploadCloud, FileJson, X } from "lucide-react";
import { Label } from "@/components/atoms/label";
import { Input } from "@/components/atoms/input";
import { GcpCredentialsDto, GcpCredentialsJson } from "@/lib/fetch/dto/cloud-credentials";

interface StepOnePropsForGcp {
    onNext: (data: { displayName: string; credentials: GcpCredentialsDto }) => void;
}

export default function StepOneGcp({ onNext }: Readonly<StepOnePropsForGcp>) {
    const [displayName, setDisplayName] = useState("");

    const [accountKey, setAccountKey] = useState<File | null>(null);

    const [draggingFile, setDraggingFile] = useState(false);

    const [errors, setErrors] = useState("");

    const [isSubmitting, setIsSubmitting] = useState(false);

    const inputForFile = useRef<HTMLInputElement>(null);

    const readingFile = async (forFile: File): Promise<string> => {
        return await forFile.text();
    };

    const acceptingFile = (forFile: File | undefined) => {
        if (!forFile) {
            return;
        }

        if (forFile.type !== "application/json" && !forFile.name.endsWith(".json")) {
            setErrors("Please upload a JSON service account key file.");

            return;
        }

        setErrors("");

        setAccountKey(forFile);
    };

    const handlingDroppingFile = (dropFile: React.DragEvent<HTMLDivElement>) => {
        dropFile.preventDefault();
        setDraggingFile(false);

        acceptingFile(dropFile.dataTransfer.files?.[0]);
    };

    const handlingSubmit = async (submittingFile: React.SubmitEvent<HTMLFormElement>) => {
        submittingFile.preventDefault();

        if (!accountKey) {
            setErrors("Please upload a service account key before continuing");

            return;
        }

        try {
            setIsSubmitting(true);
            const serviceAccountKeyJson = await readingFile(accountKey);

            try {
                JSON.parse(serviceAccountKeyJson) as GcpCredentialsDto;
            } catch {
                setErrors("The uploaded file contains invalid JSON");
                return;
            }
            const gcpCredentials = JSON.parse(serviceAccountKeyJson) as GcpCredentialsJson;
            const credentials: GcpCredentialsDto = {
                projectId: gcpCredentials.project_id,
                serviceAccountJson: serviceAccountKeyJson,
            };

            onNext({
                displayName,
                credentials,
            });
        } catch {
            setErrors("Failed to load service account");
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <StepOne
            heading="Link your GCP account"
            description="Please download the service account key as a JSON file and upload it below to begin connecting your account"
            onSubmit={handlingSubmit}
            isSubmitting={isSubmitting}
        >
            <div className="space-y-2">
                <Label htmlFor="name" className="text-foreground text-sm font-medium">
                    {" "}
                    Account name{" "}
                </Label>

                <Input
                    id="name"
                    type="text"
                    placeholder="Connection name"
                    value={displayName}
                    onChange={(changing) => setDisplayName(changing.target.value)}
                    className="bg-background border-border rounded-md px-4 py-3 text-foreground placeholder:text-muted-foreground/40 focus:outline-none focus:ring-2 focus:ring-ring focus:border-transparent transition-all w-full"
                    required
                />
            </div>

            <div className="space-y-2">
                <Label className="text-foreground text-sm font-medium"> Service account key </Label>

                <div
                    role="button"
                    tabIndex={0}

                    onDragOver={(dragging) => {
                        dragging.preventDefault();
                        setDraggingFile(true);
                    }}

                    onDragLeave={() => setDraggingFile(false)}
                    onDrop={handlingDroppingFile}
                    onClick={() => inputForFile.current?.click()}

                    onKeyDown={(pressKey) => {
                        if (pressKey.key === "Enter" || pressKey.key === " ") {
                            pressKey.preventDefault();
                            inputForFile.current?.click();
                        }
                    }}

                    className={`flex flex-col items-center justify-center gap-3 rounded-lg border-2 border-dashed p-10 text-center cursor-pointer transition-all w-full ${
                        draggingFile
                            ? "border-primary bg-primary/5"
                            : "border-border bg-background hover:border-primary/40"
                    } focus:outline-none`}
                >
                    <input
                        ref={inputForFile}
                        type="file"
                        accept="application/json,.json"
                        className="hidden"
                        onChange={(change) => acceptingFile(change.target.files?.[0])}
                    />

                    {accountKey ? (
                        <>
                            <FileJson className="w-8 h-8 text-primary" />

                            <div className="flex items-center gap-2">
                                <span className="text-foreground text-sm font-medium">
                                    {" "}
                                    {accountKey.name}{" "}
                                </span>

                                <button
                                    type="button"
                                    onClick={(click) => {
                                        click.stopPropagation();
                                        setAccountKey(null);
                                    }}
                                    className="text-muted-foreground hover:text-foreground transition-colors"
                                >
                                    <X className="w-4 h-4" />
                                </button>
                            </div>
                        </>
                    ) : (
                        <>
                            <UploadCloud className="w-8 h-8 text-primary" />

                            <p className="text-sm text-muted-foreground/70">
                                {" "}
                                Drag and drop your file here <br /> or <br />{" "}
                                <strong>Click to browse files</strong>
                            </p>
                        </>
                    )}
                </div>

                {errors && <p className="text-sm text-destructive"> {errors} </p>}
            </div>
        </StepOne>
    );
}
