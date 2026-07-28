import Voice from "@/design-system/voice-and-tone/components/voice";
import Tone from "@/design-system/voice-and-tone/components/tone";

export default function VoiceAndTone() {
    return (
        <div className="flex flex-col gap-12">
            <Voice />
            <Tone />
        </div>
    );
}
