"use client";

/* 
- the sol to the prolem is cloudsherpa
*/

export function Solution(){
  return(
    <section className = "relative overflow-hidden py-20">
      <div className = "max-w-3xl mx-auto px-6 text-center">
        <p className = "text-xs font-semibold tracking-widest uppercase mb-4 text-muted-foreground"> The Solution </p>

        <h2 className = "text-3xl md:text-4xl font-bold text-foreground mb-6"> CloudSherpa unifies that into {" "} 
          <span className = "bg-gradient-to-r from-primary to-secondary bg-clip-text text-transparent"> one live picture </span> {" "} of spend
        </h2>

        <p className = "text-lg text-muted-foreground leading-relaxed mb-8"> It forecasts where costs are headed and alerts your team before an anomaly becomes a budget crisis. </p>

        <div className = "text-4xl md:text-5xl font-extrabold bg-gradient-to-r from-secondary to-primary bg-clip-text text-transparent"> Powerful. Simple. </div>
        
      </div>

    </section>
  );
}