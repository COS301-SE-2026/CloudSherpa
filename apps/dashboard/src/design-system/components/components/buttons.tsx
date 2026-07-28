import { Button } from "@/components/atoms/button";
import SubSectionHeading from "@/design-system/shared/components/subsectionHeading";

export default function Buttons() {
    return (
        <div className="space-y-6">
            <SubSectionHeading
                title="Buttons"
                description="Buttons are important as its one of our primary call to actions. Our primary button is the default but can be styled to fit
            your needs by applying a variant attribute like variant='outline' or variant='destructive' and it will be styled accordingly. without having to add any additional classes"
            />
            <div className="grid grid-cols-2 md:grid-cols-4 lg:grid-cols-6 gap-6">
                <Button>Primary</Button>
                <Button variant="secondary">Secondary</Button>
                <Button variant="outline">Outline</Button>
                <Button variant="ghost">Ghost</Button>
                <Button variant="link">Link</Button>
                <Button variant="destructive">Destructive</Button>
            </div>
        </div>
    );
}
