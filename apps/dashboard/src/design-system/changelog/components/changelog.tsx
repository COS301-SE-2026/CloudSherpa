import ColourChanges from "@/design-system/changelog/components/colourChanges";
import LogoChanges from "./logoChanges";

export default function Changelog() {
    return (
        <div className="space-y-6">
            <ColourChanges />
            <LogoChanges />
        </div>
    );
}
