"use client";

import StepOneGcp from "./stepOne";
import StepTwoGcp from "./stepTwo";
import StepThreeGcp from "./stepThree";
import { BaseWizard } from "@/features/connectionManager/components/connectionManager/wizardSetup/wizard";
import { useRouter } from "next/navigation";

export default function WizardSetupGcp() {
    const router = useRouter();

    return (
        <BaseWizard
            eachStep={[
                { forComponents: StepOneGcp },
                { forComponents: StepTwoGcp },
                { forComponents: StepThreeGcp },
            ]}

            onComplete={() => {
                router.push("/manageConnections");
            }}

            initialData={{
                name: "GCP connection",
                servicesSelected: [],
                resources: [],
            }}

            getDataForStep={(step, forData) => {
                if (step === 0) {
                    return {
                        name: forData.name,
                        credentials: { accountKey: forData.accountKey },
                    };
                }

                if (step === 1) {
                    return {
                        servicesSelected: forData.servicesSelected || [],
                        resources: forData.resources || [],
                    };
                }

                if (step === 2) {
                    return {};
                }

                return forData;
            }}
        />
    );
}
