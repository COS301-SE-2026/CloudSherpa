import Perceivable from "@/design-system/accessibility/components/perceivable";
import Operable from "@/design-system/accessibility/components/operable";
import Understandable from "@/design-system/accessibility/components/understandable";
import Robust from "@/design-system/accessibility/components/robust";
import Scores from "@/design-system/accessibility/components/score";

export default function accessibility() {
    return (
        <div className="space-y-6">
            <Perceivable />
            <Operable />
            <Understandable />
            <Robust />
            <Scores />
        </div>
    );
}
