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
    
  );
}