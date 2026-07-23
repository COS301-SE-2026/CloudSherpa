import SubSectionHeading from "@/design-system/shared/components/subsectionHeading";
import {
    ChevronDown,
    ChevronUp,
    Eye,
    EyeOff,
    Info,
    Check,
    Calendar,
    ChevronsUpDown,
    Sidebar,
} from "lucide-react";

const icons = [
    { name: "calender", icon: <Calendar /> },
    { name: "eye", icon: <Eye /> },
    { name: "eye-off", icon: <EyeOff /> },
    { name: "info", icon: <Info /> },
    { name: "check", icon: <Check /> },
    { name: "chevron-down", icon: <ChevronDown /> },
    { name: "chevron-up", icon: <ChevronUp /> },
    { name: "sidebar", icon: <Sidebar /> },
    { name: "chevrons-up-down", icon: <ChevronsUpDown /> },
];

export default function Icons() {
    return (
        <div className="space-y-6">
            <SubSectionHeading
                title="Icons"
                description="Icons convey meaning better than words by leveraging users previous experiences and the connotation to it, for example a calender icon has the connotation of looking up or marking a data in some calender. 
             can be used by importing it at the top of the component and using it like a component ie. <Eye/>"
            />

            <div className="grid grid-cols-3 md:grid-cols-5 lg:grid-cols-6 gap-6 place-items-center">
                {icons.map((icon) => (
                    <div
                        key={icon.name}
                        className="flex flex-col gap-3 justify-center items-center"
                    >
                        <h3 className="text-sm font-bold text-foreground">{icon.name}</h3>
                        {icon.icon}
                    </div>
                ))}
            </div>
        </div>
    );
}
