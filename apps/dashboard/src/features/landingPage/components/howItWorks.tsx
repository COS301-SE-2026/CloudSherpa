"use client";

import {Card, CardContent} from "@/components/atoms/card";

/* 
- give a bried summary of how cloudsherpa works
*/

type ForStepNum = {
  number : string;
  name : string;
  description : string;
};

const FORSTEPNUM : ForStepNum[] = [
  { number : "1", name : "Connect your clouds",
    description : "Link your cloud accounts with your credentials in a guided wizard and CloudSherpa will handle the rest",
  },

  { number : "2", name : "CloudSherpa monitors usage",
    description : "Billing and resource data is unified and tracked. A unifrom view is provided across all providers",
  },

  { number : "3", name : "Act on clear insights",
    description : "Get recommendations that tell you exactly where to cut cost",
  },
];

export function HowItWorks(){
  return(
    <section id = "how-it-works" className = "relative overflow-hidden py-24">
      <div className = "max-w-6xl mx-auto px-6">
        <div className = "text-center mb-14">
          <p className = "text-xs font-semibold tracking-widest uppercase mb-4 text-muted-foreground"> How It Works </p>

          <h2 className = "text-3xl md:text-5xl font-bold text-foreground"> Up and running in minutes </h2>
        </div>

        <div className = "grid grid-cols-1 md:grid-cols-3 gap-6">
          {FORSTEPNUM.map((forSteps) => (
            <Card key = {forSteps.number} className = "relative h-full">
              <CardContent className = "p-6 flex flex-col items-center justify-center text-center">

                <div className = "w-12 h-12 rounded-xl flex items-center justify-center text-lg font-bold mb-5 bg-gradient-to-br from-primary to-accent text-primary-foreground"> {forSteps.number} </div>

                <h3 className = "text-base font-semibold text-foreground mb-2"> {forSteps.name} </h3>

                <p className = "text-sm text-muted-foreground leading-relaxed"> {forSteps.description} </p>

              </CardContent>
            </Card>
          ))}
        </div>

      </div>

    </section>
  );
}