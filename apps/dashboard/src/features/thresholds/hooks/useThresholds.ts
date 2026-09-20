import {useCallback, useEffect, useState} from "react";
import type {CreateThresholdRequest, Threshold, UpdateThresholdRequest} from "@/features/thresholds/types/thresholdTypes";
import {addThreshold, deleteThreshold, editThreshold, fetchThresholds} from "@/features/thresholds/thresholds";

interface RequestForThreshold{
  thresholds : Threshold[];
  loading : boolean;
  forError : string | null;
  refreshing : () => Promise<void>;
  createThreshold : (forPayload : CreateThresholdRequest) => Promise<Threshold>;
  updateThreshold : (id : string, forPayload : UpdateThresholdRequest) => Promise<void>;
  removeThreshold : (id : string) => Promise<void>;
}

export function useThresholds(resourceId?: string) : RequestForThreshold{
  const [thresholds, setThresholds] = useState<Threshold[]>([]);

  const [loading, setLoading] = useState<boolean>(false);

  const [forError, setForError] = useState<string | null>(null);

  const refreshing = useCallback(async () => {
    setLoading(true);

    setForError(null);

    try{
      const forData = await fetchThresholds(resourceId);

      setThresholds(forData);
    }catch(error){
      setForError(error instanceof Error ? error.message : "Failed to load Thresholds.");
    }finally{
      setLoading(false);
    }
  }, [resourceId]);

  useEffect(() => {
    void refreshing();
  }, [refreshing]);

  const createThreshold = useCallback(
    async (forPayload : CreateThresholdRequest) => {
      const created = await addThreshold(forPayload);

      setThresholds((previous) => [...previous, created]);

      return created;
    }, [],
  );

  const updateThreshold = useCallback(
    async (id : string, forPayload : UpdateThresholdRequest) => {
      await editThreshold(id, forPayload);

      setThresholds((previous) => 
        previous.map((forThreshold) => {
          if(forThreshold.thresholdId !== id){
            return forThreshold;
          }

          const {metric_name, ...rest} = forPayload;

          return{...forThreshold, ...rest, ...(metric_name !== undefined ? {metricName : metric_name} : {}),};
        }),
      );
    }, [],
  );

  const removeThreshold = useCallback(async (id : string) => {
    await deleteThreshold(id);

    setThresholds((previous) => previous.filter((forThreshold) => forThreshold.thresholdId !== id));
  }, []);

  return{thresholds, loading, forError, refreshing, createThreshold, updateThreshold, removeThreshold,};
}