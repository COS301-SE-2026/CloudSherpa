import pandas as pd
import numpy as np

def sherpa_preprocess(
    path: str
):
    rename_columns = {
        "resourceId": "item_id",
        "value": "target"
    }

    keep_columns = ["resourceId", "value", "metricName", "timestamp"]

    # Not handling FileNotFoundError, want runtime error to be thrown if that happens
    context_df = pd.read_csv(path)
    
    context_df = context_df[keep_columns].copy()
    context_df = context_df.rename(columns=rename_columns)

    context_df["item_id"] = context_df["item_id"].astype(str)
    context_df["timestamp"] = pd.to_datetime(context_df['timestamp'], format='ISO8601', utc=True).dt.tz_convert("UTC").dt.tz_localize(None)
    context_df = context_df.dropna(subset=["timestamp"]).sort_values(by="timestamp")
    
    return context_df

def sherpa_get_feature(
    context_df: pd.DataFrame,
    resource_id: str,
    metricName: str,
):
    return context_df.loc[(context_df["item_id"] == resource_id) & (context_df["metricName"] == metricName)].copy()

def sherpa_combine_resources(
    context_df: pd.DataFrame,
    resource_id: str,
    target_metric: str
):

    resource_df = context_df.loc[context_df["item_id"] == resource_id]
    
    # Get all metric types for resource
    metric_types = resource_df["metricName"].unique()
    metric_types = np.delete(metric_types, np.where(metric_types == target_metric))
    
    target_df = resource_df.loc[resource_df["metricName"] == target_metric]
    
    for metric in metric_types:
        # construct merge df
        merge_df = resource_df.loc[resource_df["metricName"] == metric].drop(columns="metricName")
        merge_df = merge_df.rename(columns={"target": metric})
        # merge
        target_df = target_df.merge(merge_df, on=["item_id", "timestamp"])

    return target_df

def quantify_error_rmse(
    ground_truth_df: pd.DataFrame,
    prediction_df: pd.DataFrame
):
    rmse = np.sqrt(
        np.mean(
            (
                ground_truth_df["target"].to_numpy()
                - prediction_df["predictions"].to_numpy()
            ) ** 2
        )
    )
    print(f"Root Mean Squared Error: {rmse}")
    return rmse