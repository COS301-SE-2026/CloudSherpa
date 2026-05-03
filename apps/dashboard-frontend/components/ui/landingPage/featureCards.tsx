'use client';
import { forwardRef, useState } from "react";

interface forFeatureBlocks{
  showingFeatureCards: boolean;
}

//these cards explain the features in more detail and it gives he benefits for them
const forFeatureItems = [
  { heading: 'AI - powered forecasting',
    subHeading: 'Predict costs before they spike',
    desc: 'Stop reacting to cloud bills. Our ML models give your team a 30-day window to act — shifting you to proactive financial control.',
    benefits: [
      { label: 'Eliminate bill shock', 
        detail: 'Know your exact bill before it arrives — no more surprises that blow your budget.' },
      { label: 'Act before costs escalate', 
        detail: 'Forecasts surface 30 days early so engineers can right-size resources before spend compounds.' },
      { label: 'Protect profit margins', 
        detail: 'Finance teams get reliable cloud cost projections they can actually use in their reporting.' },
      { label: 'Eliminate reactive cost management', 
        detail: 'Shift your team from reactive cost investigations to proactive optimisation — saving hours every week.' },
    ],
  },

  { heading: 'Deep Analytics',
    subHeading: 'Know where money goes',
    desc: "Cloud spend is invisible until it isn't. Deep Analytics gives every team the exact view they need to make smarter decisions.",
    benefits: [
      { label: 'End blame game billing', 
        detail: "Tag based cost allocation means every team owns their spend. No more shared bills nobody understands." },
      { label: 'Find waste instantly', 
        detail: 'Idle resources are surfaced automatically — reclaim budget without a manual audit.' },
      { label: 'Empower every stakeholder', 
        detail: 'Custom dashboards for everyone to sees what\'s relevant to them.' },
      { label: 'Accelerate financial reconciliation', 
        detail: 'Clean, organized cost data cuts finance reconciliation time in half.' },
    ],
  },

  { heading: 'Budget Control',
    subHeading: 'Never overspend again',
    desc: 'CloudSherpa enforces your limits automatically — scaling back resources before a breach, not after.',
    benefits: [
      { label: 'Stop overruns at the source', 
        detail: 'Auto-pause or scale down resources the moment a threshold is hit — not after the damage is done.' },
      { label: 'Give teams financial autonomy', 
        detail: 'Delegate budgets to individual teams and projects without losing company-wide visibility' },
      { label: 'Protect your runway', 
        detail: 'Hard budget guardrails mean cloud costs can never quietly drain your reserves.' },
      { label: 'Ensure spend accountability', 
        detail: 'Every budget override is tracked and approved, no more untraceable spending decisions.' },
    ],
  },

  { heading: 'Multi - cloud',
    subHeading: 'AWS, Azure & GCP',
    desc: 'Managing three cloud bills, three dashboards, and three pricing models is a full-time job. CloudSherpa collapses it into one clear, unified picture.',
    benefits: [
      { label: 'One source of truth', 
        detail: 'Unified cost data across AWS, Azure, and GCP means no more spreadsheet compilation' },
      { label: 'Cloud price differentiation', 
        detail: 'Identify workloads that would be cheaper on a different provider — with cost estimates included.' },
      { label: 'Consistent tagging enforcement', 
        detail: 'Standardise resource tagging across all clouds so cost allocation works the same way everywhere.' },
      { label: 'Reduce operational complexity', 
        detail: 'Replace three separate native cost tools with one platform which reduces cognitive load and subscription costs.' },
    ],
  },

  { heading: 'Smart Alerts',
    subHeading: 'Instant notification',
    desc: 'Alert fatigue is real. CloudSherpa only alerts you when something genuinely needs your attention',
    benefits: [
      { label: 'Zero alert fatigue', 
        detail: 'AI filters out noise so only actionable anomalies reach your team — no more ignored notification floods.' },
      { label: 'Detect anomalies in real-time', 
        detail: "Unusual spending patterns are flagged within seconds." },
      { label: 'Catch runaway costs in seconds', 
        detail: 'Anomaly detection stops a misconfigured resource before it costs thousands.' },
      { label: 'Notify your way', 
        detail: "Email or SMS, choose what works for your team." },
    ],
  },

  { heading: '5min Setup',
    subHeading: 'Start saving today',
    desc: "Most cost tools take weeks to deploy and need a dedicated engineer. CloudSherpa is live in five minutes.",
    benefits: [
      { label: 'No engineering resources needed', 
        detail: 'Anyone can set this up alone and no infrastructure changes.' },
      { label: 'Immediate historical insight', 
        detail: 'CloudSherpa imports your cost data on day one and you get insights before the call ends.' },
      { label: 'Insights on first login', 
        detail: "Dashboards populate instantly with historical spending — no waiting for data to accumulate." },
      { label: 'Enterprise-grade security', 
        detail: 'Encrypted credentials. Your cloud is never exposed.' },
    ],
  },
];

//show the benefits of each feature that CloudSherpa has to offer
function DetailedFeatures({ 
  card, 
  onBack 
}: { 
  card: typeof forFeatureItems[0]; 
  onBack: () => void;
}) {
  return(
    <div style={{ width: '100%', maxWidth: 1100, animation: 'slideInRight 0.3s ease' }}>

      {/*this is the styling for the back button*/}
      <button
        onClick={onBack}
        style={{
          display: 'inline-flex',
          alignItems: 'center',
          gap: 8,
          background: '#030712',
          border: '1px solid transparent',
          borderRadius: 8,
          color: '#CBD5E1',
          fontSize: 13,
          padding: '8px 14px',
          cursor: 'pointer',
          marginBottom: 28,
          transition: 'all 0.2s',
          fontFamily: 'inherit',
          backgroundImage: `linear-gradient(#030712, #030712), linear-gradient(135deg, #2F2FE4 0%, #162E93 100%)`,
          backgroundOrigin: 'border-box',
          backgroundClip: 'padding-box, border-box',
        }}

        onMouseEnter={(forEnter) => {
          forEnter.currentTarget.style.transform = 'translateY(-1px)';
        }}

        onMouseLeave={(forEnter) => {
          forEnter.currentTarget.style.transform = 'translateY(0)';
        }}
      >
         Back to features
      </button>

      {/*border for the details card*/}
      <div
        style={{
          background: '#030712',
          border: '1px solid transparent',
          borderRadius: 18,
          padding: '40px 44px',
          backgroundImage: `linear-gradient(#030712, #030712), linear-gradient(135deg, #2F2FE4 0%, #162E93 100%)`,
          backgroundOrigin: 'border-box',
          backgroundClip: 'padding-box, border-box',
        }}
      >

        {/*this is for the heading of the details page*/}
        <div style={{ display: 'flex', alignItems: 'center', gap: 18, marginBottom: 24 }}>
          <div style={{ width: 60, height: 60, background: '#374151', borderRadius: 14, flexShrink: 0 }} />
          <div>
            <h2 style={{ fontSize: 24, fontWeight: 700, color: '#ffffff', marginBottom: 5, letterSpacing: '-0.01em' }}>
              {card.heading}
            </h2>
            <p style={{ fontSize: 14, color: '#CBD5E1', opacity: 0.55 }}>{card.subHeading}</p>
          </div>
        </div>

        <div style={{ borderTop: '1px solid rgba(203,213,225,0.08)', marginBottom: 28 }} />

        <p style={{ fontSize: 15, color: '#CBD5E1', opacity: 0.7, lineHeight: 1.75, marginBottom: 36, maxWidth: 640 }}>
          {card.desc}
        </p>

        {/*this is for the benefits label*/}
        <div style={{ fontSize: 11, fontWeight: 600, color: '#CBD5E1', opacity: 0.35, textTransform: 'uppercase', letterSpacing: '0.08em', marginBottom: 24 }}>
          Key benefits
        </div>

        {/*this is for the bullet points for the benefits*/}
        <div
          style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(2, 1fr)',
            gap: 20,
            marginBottom: 0,
          }}
        >

          {card.benefits.map((forBenefits) => (
            <div
              key={forBenefits.label}
              style={{
                background: '#0a0f1e',
                border: '1px solid rgba(203,213,225,0.07)',
                borderRadius: 12,
                padding: '20px 22px',
              }}
            >

              <div style={{ display: 'flex', alignItems: 'center', gap: 10, marginBottom: 10 }}>

                {/*this is for the gradients for the border*/}
                <div
                  style={{
                    width: 8,
                    height: 8,
                    borderRadius: '50%',
                    background: 'linear-gradient(135deg, #2F2FE4 0%, #162E93 100%)',
                    flexShrink: 0,
                  }}
                />

                <div style={{ fontSize: 14, fontWeight: 600, color: '#ffffff' }}>{forBenefits.label}</div>
              </div>
              <p style={{ fontSize: 13, color: '#CBD5E1', opacity: 0.58, lineHeight: 1.65, margin: 0 }}>{forBenefits.detail}</p>
            </div>
          ))}

        </div>
      </div>
    </div>
  );
}

//this is for the feature cards
const forFeatureCards = forwardRef<HTMLElement, forFeatureBlocks>(
  ({showingFeatureCards}, containerRef) => {
    const [activeCardIndex,setActiveCardIndex] = useState<number | null>(null);

    const handleCardClick = (forIndex: number) => {
      setActiveCardIndex(forIndex);
      setTimeout(() => {
        containerRef?.current?.scrollIntoView({ behavior: 'smooth', block: 'start' });
      }, 10);
    };

    const handleBack = () => {
      setActiveCardIndex(null);
      setTimeout(() => {
        containerRef?.current?.scrollIntoView({ behavior: 'smooth', block: 'start' });
      }, 10);
    };

    return(
      <section
        ref={containerRef}
        id="features-section"
        className="min-h-screen flex flex-col items-center justify-center py-15 px-10 bg-[#030712] transition-all duration-500 ease"
        style={{
          opacity: showingFeatureCards ? 1 : 0, 
          transform: showingFeatureCards ? 'translateY(0)' : 'translateY(20px)', 
          pointerEvents: showingFeatureCards ? 'auto' : 'none',
        }}
      >

        {activeCardIndex === null ? (
          /*this shows a grid display of all the cards*/
          <div style={{ width: '100%', maxWidth: 1100, animation: 'slideInLeft 0.3s ease' }}>
            <h2 className="text-4xl font-bold tracking-tight text-center mb-12">
              <span className="bg-gradient-to-r from-[#2F2FE4] to-[#162E93] bg-clip-text text-transparent">Powerful.</span>{' '}
              <span className="text-white">Simple.</span>
            </h2>

            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
              {forFeatureItems.map((forFeature, forItem) => (
                <div
                  key={forFeature.heading}
                  onClick={() => handleCardClick(forItem)}
                  className="w-[350px] h-[278px] rounded-xl p-7 pb-6 flex flex-col items-center justify-center text-center relative cursor-pointer transition-all duration-200 hover:-translate-y-1"
                  style={{
                    background: '#030712', border: '1px solid transparent',
                    backgroundImage: `linear-gradient(#030712, #030712), linear-gradient(135deg, #2F2FE4 0%, #162E93 100%)`,
                    backgroundOrigin: 'border-box', backgroundClip: 'padding-box, border-box',
                    animationName: showingFeatureCards ? 'fadeSlideUp' : 'none', animationDuration: '0.4s',
                    animationTimingFunction: 'ease', animationFillMode: 'both', animationDelay: `${forItem * 70}ms`,
                  }}

                  onMouseEnter={(forEnter) => {
                    forEnter.currentTarget.style.transform = 'translateY(-3px)';
                  }}

                  onMouseLeave={(forEnter) => {
                    forEnter.currentTarget.style.transform = 'translateY(0)';
                  }}
                >

                  {/*this is the placeholder for the icon*/}
                  <div className="w-15 h-15 rounded-lg mb-6 flex-shrink-0 bg-[#374151]" />

                  <div>
                    <h3 className="text-[15px] font-semibold text-white mb-1.5">{forFeature.heading}</h3>
                    <p className="text-[13px] text-[#CBD5E1] m-0 opacity-60">{forFeature.subHeading}</p>
                  </div>

                </div>
              ))}
            </div>
          </div>
        ) : (

          /*this will show the expanded view of each feature card*/
          <DetailedFeatures 
            card={forFeatureItems[activeCardIndex]} 
            onBack={handleBack} 
          />

        )}
      </section>
    );
  }
);

forFeatureCards.displayName = "FeatureCards";
export default forFeatureCards;