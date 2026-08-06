import React from "react";
import { ArrowUp, ArrowDown } from "lucide-react";

type TrendProps = {
    current: number;
    previous?: number | null;
};

export function TrendLabel(props: TrendProps) {
    const current = props.current;
    const previous = props.previous;

    // check if we have no previous data first
    if (previous == null || Number.isNaN(previous)) {
        return <span className="text-sm text-gray-500">No trend</span>;
    }

    const difference = current - previous;

    let percentageChange = 0;
    if (previous !== 0) {
        percentageChange = (difference / Math.abs(previous)) * 100;
    }

    const isGoingUp = difference > 0;
    const isGoingDown = difference < 0;
    const isUnchanged = difference === 0;

    const formattedPercentage = `${Math.abs(percentageChange).toFixed(2)}%`;
    const sign = isGoingUp ? "+" : "";
    const formattedDifference = `${sign}${difference.toFixed(2)}`;

    let textColor = "text-gray-500";
    let icon = null;

    if (isGoingUp) {
        textColor = "text-green-600";
        icon = <ArrowUp size={14} />;
    } else if (isGoingDown) {
        textColor = "text-red-600";
        icon = <ArrowDown size={14} />;
    }

    return (
        <div className="flex items-center gap-2 text-sm">
            <span className={`flex items-center ${textColor}`}>
                {icon}
                {isUnchanged ? "No change" : formattedPercentage}
            </span>

            <span className="text-gray-500">({formattedDifference})</span>
        </div>
    );
}

export default TrendLabel;
