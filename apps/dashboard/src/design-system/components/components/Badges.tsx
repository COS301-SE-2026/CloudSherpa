import SubSectionHeading from "@/design-system/shared/components/subsectionHeading";
import { Badge } from "@/components/atoms/badge";

export default function Badges() {
    return (
        <div className="space-y-6">
            <SubSectionHeading
                title="Badges"
                description="Similar to icons badges help users get a grasp of the current state or status of process or components"
            />
            <Badge>Badge</Badge>
        </div>
    );
}
