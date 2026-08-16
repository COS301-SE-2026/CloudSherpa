"use client";

import StepThreeBase, {StepThreePropsForBase} from "@/features/connectionManager/components/connectionManager/wizardSetup/stepThree";

const hardCodedResources = [
    {
        id: "resource1",
        name: "Resource one",
        type: "Service one",
        region: "region 1",
        tag: ["tag1", "tag2"],
    },
];

export default function StepThreeAzure(propsForAzure : StepThreePropsForBase){
    return(
        <StepThreeBase {...propsForAzure} cloudProvider = "azure" hardCodedResources = {hardCodedResources} errorMessage = "Unable to complete the Azure setup wizard"/>
    )
}