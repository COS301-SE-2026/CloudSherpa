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
        resourceId: "resource1",
        name: "Resource one",
        resourceType: "Service one",
        serviceCategory: "Category one",
        region: "region 1",
        tags: { tag1: "tag1", tag2: "tag2" },
    },
];

export default function StepThreeGcp(propsForGcp: Readonly<StepThreePropsForBase>) {
    return <StepThreeBase {...propsForGcp} hardCodedResources={hardCodedResources} />;
}
