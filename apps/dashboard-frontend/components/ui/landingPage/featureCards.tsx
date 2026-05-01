'use client';
import { forwardRef } from "react"; //this will allow the parent parent components to pass a reference to this component

interface forFeatureBlocks{
  showingFeatureCards: boolean;
}

const forFeatureItems = [
  {heading: 'AI - powered forecasting', 
    subHeading: 'Predict costs before they spike'},

  {heading: 'Deep Analytics', 
    subHeading: 'Know where money goes'},

  {heading: 'Budget Control', 
    subHeading: 'Never overspend again'},

  {heading: 'Multi - cloud', 
    subHeading: 'AWS, Azure & GCP'},

  {heading: 'Smart Alerts', 
    subHeading: 'Instant notification'},

  {heading: '5min Setup', 
    subHeading: 'Start saving today'},
];

//forwardRef creates a component that will be able to receive a reference from its parent
//<HTMLElement> the reference will point to an element
//forFeatureBlock is what the component will accept
const forFeatureCards = forwardRef<HTMLElement, forFeatureBlocks>(
  ({ showingFeatureCards }, containerRef) => {
    return (
      <section
        ref={containerRef}
        id="features-section"
        className="min-h-screen flex flex-col items-center justify-center py-15 px-10 bg-[#030712] transition-all duration-500 ease"
        style={{opacity: showingFeatureCards ? 1 : 0, 
                transform: showingFeatureCards ? 'translateY(0)' : 'translateY(20px)', 
                pointerEvents: showingFeatureCards ? 'auto' : 'none',}}
      >

        <h2 className="text-4xl font-bold tracking-tight text-center mb-12">
          <span className="bg-gradient-to-r from-[#2F2FE4] to-[#162E93] bg-clip-text text-transparent">Powerful.</span>{' '}
          <span className="text-white">Simple.</span>
        </h2>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
          {forFeatureItems.map((forFeature, forItem) => (
            <div
              key={forFeature.heading}
              className="w-[350px] h-[278px] rounded-xl p-7 pb-6 flex flex-col items-center justify-center text-center relative feature-card"
              style={{opacity: showingFeatureCards ? 1 : 0,
                      transform: showingFeatureCards ? 'translateY(0)' : 'translateY(20px)',
                      transition: `opacity 0.4s ease ${forItem * 0.07}s, transform 0.4s ease ${forItem * 0.07}s`,}}
            >

              {/*this is the placeholder for the icons*/}
              <div className="w-15 h-15 rounded-lg mb-6 flex-shrink-0 bg-[#374151]" />

              <div>
                <h3 className="text-[15px] font-semibold text-white mb-1.5">{forFeature.heading}</h3>
                <p className="text-[13px] text-[#CBD5E1] m-0 opacity-60">{forFeature.subHeading}</p>
              </div>

            </div>
          ))}
        </div>
      </section>
    );
  }
);

forFeatureCards.displayName = "FeatureCards";
export default forFeatureCards;