import SubSectionHeading from "@/design-system/shared/components/subsectionHeading";

export default function Voice() {
    return (
        <div className="space-y-6">
            <SubSectionHeading
                title="Voice"
                description="Voice is all about our steady personality in out textual content"
            />
            <span className="text-base">
                CloudSherpa’s voice is constant: calm (no panic, data speaks for itself), precise
                (no jargon, just numbers), honest (acknowledges limitations), and helpful (always
                includes next steps). No panic, no passive language, no fragmented sentences.
            </span>
        </div>
    );
}
