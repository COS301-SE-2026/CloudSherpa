"use client";

import StepThreeBase, {
    StepThreePropsForBase,
} from "@/features/connectionManager/components/connectionManager/wizardSetup/stepThree";

/*
- should have tanstack table for resources, as elect & deselect all for it
- should also have pagination
*/

const hardCodedResources = [
    {
        id: "resource1",
        name: "Resource one",
        type: "Service one",
        region: "region 1",
        tag: ["tag1", "tag2"],
    },
];

export default function StepThreeGcp(propsForGcp: Readonly<StepThreePropsForBase>) {
    return <StepThreeBase {...propsForGcp} hardCodedResources={hardCodedResources} />;
}
