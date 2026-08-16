"use client";

import StepThreeBase, {
    StepThreePropsForBase,
} from "@/features/connectionManager/components/connectionManager/wizardSetup/stepThree";

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

export default function StepThreeAzure(propsForAzure: Readonly<StepThreePropsForBase>) {
    return <StepThreeBase {...propsForAzure} hardCodedResources={hardCodedResources} />;
}
