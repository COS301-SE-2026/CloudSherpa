"use client";

import {billingIntelligenceStore} from "@/features/intelligence/stores/billingIntelligenceStore";
import BillingToolbar from "@/features/intelligence/components/billing/billingToolbar";
import CostBreakdownList from "@/features/intelligence/components/billing/costBreakdownList";
import BillingForecastChart from "@/features/intelligence/components/billing/billingForecastChart";
import BillingStatisticsCard from "@/features/intelligence/components/billing/billingStatisticsCard";
import BillingSummaryCard from "@/features/intelligence/components/billing/billingSummaryCard";
import {TrendingUp} from "lucide-react";

export default function BillingIntelligence(){
    const{
        provider, accountId, resourceId, breakdownSearch, setBreakdownSearch, pastTimeWindowDays, forecastTimeWindowDays,
    } = billingIntelligenceStore();

    //might add later on
    // const selected = provider && accountId && resourceId;

    // if(!selected){
    //     return(
    //         <div className = "h-full w-full p-6 flex flex-col gap-4">
    //             <BillingToolbar/>

    //             <div className = "flex-1 flex items-center justify-center">
    //                 <div className = "text-center max-w-md">
    //                     <div className = "mx-auto w-16 h-16 bg-muted rounded-full flex items-center justify-center mb-4"> <TrendingUp className = "h-8 w-8 text-muted-foreground"/> </div>

    //                     <h3 className = "text-lg font-semibold mb-2"> No selection made </h3>

    //                     <p className = "text-sm text-muted-foreground"> Select a provider, account and resource to view billing data </p>
    //                 </div>
    //             </div>
    //         </div>
    //     );
    // }

    return(
        <div className = "h-full w-full p-6 flex flex-col gap-4">
            <BillingToolbar/>

            <section className = "grid grid-cols-1 lg:grid-cols-2 gap-4">
                <BillingSummaryCard name = {`cumulative billing for last ${pastTimeWindowDays} days`} value = "" description = "No data available" valueClassName = "text-primary"/>

                <BillingSummaryCard name = {`projected horizon cost (${forecastTimeWindowDays} days)`} value = "" description = "No data available" valueClassName = "text-accent"/>
            </section>

            <section className = "grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
                <BillingStatisticsCard name = "forecast vs past variance" value = "" description = "No data available" icon = {TrendingUp} valueClassName = "text-primary"/>

                <BillingStatisticsCard name = "daily burn rate" value = "" description = "No data available"/>

                <BillingStatisticsCard name = "Primary cost driver" value = "" description = "No data available" valueClassName = "text-accent"/>

                <BillingStatisticsCard name = "Highest Cost Acceleration" value = "" description = "No data available" valueClassName = "text-accent"/>
            </section>

            <section className = "grid grid-cols-1 lg:grid-cols-2 gap-4">
                <BillingForecastChart name = {`cumulative billing forecast for ${forecastTimeWindowDays} days`} data = {[]}/>

                <CostBreakdownList name = "cost breakdown" description = {`projected charges for ${pastTimeWindowDays} day window`} eachEntry = {[]} search = {breakdownSearch} onSearchChange = {setBreakdownSearch}/>
            </section>
        </div>
    );
}

