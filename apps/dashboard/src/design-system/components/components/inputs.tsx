import SubSectionHeading from "@/design-system/shared/components/subsectionHeading";
import { useState } from "react";
import { Eye, EyeOff } from "lucide-react";
import { Input } from "@/components/atoms/input";

export default function Inputs() {
    const [isPasswordVisible, setIsPasswordVisible] = useState(false);

    const togglePasswordVisibility = () => {
        setIsPasswordVisible(!isPasswordVisible);
    };

    return (
        <div className="space-y-6">
            <SubSectionHeading
                title="Inputs"
                description="Inputs, like buttons, are a cornerstone of any project and in the center of most forms."
            />
            <div className="grid grid-cols-2 gap-6">
                <Input placeholder="default inputs"></Input>

                <div className="relative">
                    <Input
                        id="password"
                        type={isPasswordVisible ? "text" : "password"}
                        placeholder="password"
                        required
                        className="pr-10"
                    />
                    <button
                        type="button"
                        onClick={togglePasswordVisibility}
                        className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-ring"
                    >
                        {isPasswordVisible ? <Eye size={20} /> : <EyeOff size={20} />}
                    </button>
                </div>
            </div>
        </div>
    );
}
