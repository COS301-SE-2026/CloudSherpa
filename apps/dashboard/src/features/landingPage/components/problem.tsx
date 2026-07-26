"use client";

import {Card, CardContent} from "@/components/atoms/card";

/*
- states the problem that cloudsherpa is trying to overcome
*/

export function Problem(){
  return(
    <section id = "problem" className = "relative overflow-hidden py-20">
      <div className = "max-w-6xl mx-auto px-6">
        <div className = "grid grid-cols-1 md:grid-cols-2 gap-16 items-center">
          <div>
            <p className = "text-xs font-semibold tracking-widest uppercase mb-4 text-muted-foreground"> The Problem </p>

            <h2 className = "text-3xl md:text-4xl font-bold text-foreground mb-6 leading-snug"> Multiple clouds. Multiple bills.{' '}
              <span className = "bg-gradient-to-r from-secondary to-primary bg-clip-text text-transparent"> Zero visibility. </span>
            </h2>

            <p className = "text-base text-muted-foreground leading-relaxed mb-6"> Teams who operate multi-cloud environments struggle with fragmented billing, multiple dashboards and pricing models, Overspending is difficult to detect and combining this information manually is time consuming. </p>

            <p className = "text-base text-muted-foreground leading-relaxed"> Your overspending is caught weeks later, if at all. By the time a cost spike is identified the budget has be exceeded </p>
          </div>

          <div className = "flex flex-col gap-3">
            {["Delayed cost visibility & bill shock", "Overprovisioning due to risk aversion", "Inconsistent multi-cloud cost normalization", "Limited FinOps capabilites"].map((forProblems) => (
              <Card key = {forProblems} className = "border-border">
                <CardContent className = "flex items-center gap-3 px-4 py-3">

                  <div className = "w-5 h-5 rounded-full flex-shrink-0 flex items-center justify-center bg-destructive/15">
                    <div className = "w-1.5 h-1.5 rounded-full bg-destructive"/>
                  </div>

                  <span className = "text-sm text-foreground/80"> {forProblems} </span>
                </CardContent>
              </Card>

            ))}
          </div>
        </div>
      </div>
    </section>
  );
}