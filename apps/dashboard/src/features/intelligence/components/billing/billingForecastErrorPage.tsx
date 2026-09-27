import React from "react";

interface BillingForecastErrorPageProps {
    readonly children: React.ReactNode;
}

export default function BillingForecastErrorPage({ children }: BillingForecastErrorPageProps) {
    return (
        <div className="flex-1 flex flex-col items-center justify-center text-center p-10">
            {children}
        </div>
    );
}
