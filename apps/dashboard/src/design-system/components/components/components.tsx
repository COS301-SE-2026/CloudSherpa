import Buttons from "@/design-system/components/components/buttons";
import Inputs from "@/design-system/components/components/inputs";
import Badges from "@/design-system/components/components/Badges";
import Cards from "@/design-system/components/components/cards";
import Tables from "@/design-system/components/components/Tables";

export default function Components() {
    return (
        <div className="flex flex-col gap-12">
            <Buttons />
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                <Inputs />
                <Badges />
            </div>
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                <Cards />
                <Tables />
            </div>
        </div>
    );
}
