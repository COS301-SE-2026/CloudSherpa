"use client";

import {useEffect, useState} from "react";
import {Label} from "@/components/atoms/label";
import {Input} from "@/components/atoms/input";
import {Button} from "@/components/atoms/button";
import {Dialog, DialogContent, DialogFooter, DialogHeader, DialogTitle} from "@/components/atoms/dialog";
import {Select, SelectContent, SelectItem, SelectTrigger, SelectValue} from "@/components/atoms/select";
import {Checkbox} from "@/components/atoms/checkbox";
import { CreateThresholdRequest, Threshold, OperatorsForThreshold, SeverityForThreshold, OPERATORS, SEVERITY} from "@/features/thresholds/types/thresholdTypes";

interface PropsForThresholds{
  open : boolean;
  initial?: Threshold | null;
  resourceId : string;
  userId : string;
  onClose : () => void;
  onSubmit : (thresholdPayload : CreateThresholdRequest) => Promise<void>;
}

export function ThresholdPopup({
  open, initial, resourceId, userId, onClose, onSubmit,
} : Readonly<PropsForThresholds>){
  const [metricName, setMetricName] = useState("");

  const [operator, setOperator] = useState<OperatorsForThreshold>("GT");

  const [value, setValue] = useState<number>(80);

  const [severity, setSeverity] = useState<SeverityForThreshold>("WARNING");

  const [enabled, setEnabled] = useState(true);

  const [submit, setSubmit] = useState(false);

  useEffect(() => {
    if(initial){
      setMetricName(initial.metricName);
      setOperator(initial.operator);
      setValue(initial.value);
      setSeverity(initial.severity);
      setEnabled(initial.enabled);
    }else{
      setMetricName("");
      setOperator("GT");
      setValue(80);
      setSeverity("WARNING");
      setEnabled(true);
    }
  }, [initial, open]);

  const handlingSubmit = async (submitting : React.FormEvent) => {
    submitting.preventDefault();

    setSubmit(true);

    try{
      await onSubmit({resourceId, userId, metric_name : metricName, operator, value : Number(value), severity, enabled,});

      onClose();
    }finally{
      setSubmit(false);
    }
  };

  return(
    <Dialog open = {open} onOpenChange = {(next) => !next && onClose()}>
      <DialogContent className = "sm:max-w-md">
        <DialogHeader>
          <DialogTitle> {initial ? "Edit threshold" : "New threshold"} </DialogTitle>
        </DialogHeader>

        <form onSubmit = {handlingSubmit} className = "space-y-4">
          <div className = "space-y-2">
            <Label htmlFor = "metricName"> Metric </Label>

            <Input id = "metricName" value = {metricName} onChange = {(change) => setMetricName(change.target.value)} required/>
          </div>

          <div className = "space-y-2">
            <Label htmlFor = "operator"> Operator </Label>

            <Select value = {operator} onValueChange = {(change) => setOperator(change as OperatorsForThreshold)}>
              <SelectTrigger id = "operator">
                <SelectValue placeholder = "Select operator"/>
              </SelectTrigger>

              <SelectContent>
                {OPERATORS.map((forOperators) => (
                  <SelectItem key = {forOperators} value = {forOperators}> {forOperators} </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>

          <div className = "space-y-2">
            <Label htmlFor = "value"> Value </Label>

            <Input id = "value" type = "number" step = "any" value = {value} onChange = {(change) => setValue(Number(change.target.value))} required/>
          </div>

          <div className = "space-y-2">
            <Label htmlFor = "severity"> Severity </Label>

            <Select value = {severity} onValueChange = {(change) => setSeverity(change as SeverityForThreshold)}>
              <SelectTrigger id = "severity">
                <SelectValue placeholder = "Select severity"/>
              </SelectTrigger>

              <SelectContent>
                {SEVERITY.map((forSeverity) => (
                  <SelectItem key = {forSeverity} value = {forSeverity}> {forSeverity} </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>

          <div className = "flex items-center gap-2">
            <Checkbox id = "enabled" checked = {enabled} onCheckedChange = {(change) => setEnabled(change === true)}/>

            <Label htmlFor = "enabled"> Enabled </Label>
          </div>

          <DialogFooter className = "pt-2">
            <Button type = "button" variant = "outline" onClick = {onClose}> Cancel </Button>

            <Button type = "submit" disabled = {submit}> {submit ? "Saving" : "Save"} </Button>
          </DialogFooter>

        </form>
      </DialogContent>
    </Dialog>
  );
}