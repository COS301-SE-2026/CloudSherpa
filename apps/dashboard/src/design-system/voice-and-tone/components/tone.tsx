import SubSectionHeading from "@/design-system/shared/components/subsectionHeading";

export default function Tone() {
    return (
        <div className="space-y-6">
            <SubSectionHeading
                title="Tone"
                description="Refers to how Cloudsherpa's mood comes across to the user in our textual content."
            />
            <span className="text-base">
                CloudSherpa’s tone shifts by situation: urgent but precise for anomalies, helpful
                and confident for recommendations, honest and instructive for errors, encouraging
                for empty states, warm and brief for success, patient and transparent for loading.
                Technical users see resource IDs and exact metrics; non-technical users see visual
                summaries and team-level allocation.
            </span>
        </div>
    );
}
