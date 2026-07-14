// components/auth/social-auth.tsx
import { Button } from "@/components/atoms/button";
import { Cat, Globe, Cloud } from "lucide-react";

export function SocialAuth() {
    return (
        <div className="w-full space-y-6">
            {/* The Divider */}
            <div className="relative">
                <div className="absolute inset-0 flex items-center">
                    <span className="w-full border-t border-slate-200" />
                </div>
                <div className="relative flex justify-center text-xs uppercase">
                    <span className="bg-white px-4 text-slate-400 font-medium tracking-wider">
                        Or continue with
                    </span>
                </div>
            </div>

            {/* Social Buttons Grid */}
            <div className="grid grid-cols-2 gap-3">
                <Button
                    variant="outline"
                    className="h-11 border-slate-700 text-slate-700 hover:bg-slate-50 hover:text-[#2F2FE4] transition-all duration-300 flex items-center justify-center gap-2 font-semibold"
                >
                    <Cat size={18} />
                    <span>GitHub</span>
                </Button>

                <Button
                    variant="outline"
                    className="h-11 border-[#DB4437] text-[#DB4437] hover:bg-slate-50 hover:text-[#2F2FE4] transition-all duration-300 flex items-center justify-center gap-2 font-semibold"
                >
                    <Globe size={18} />
                    <span>Google</span>
                </Button>
            </div>

            {/* Optional: Microsoft/Azure for Enterprise FinOps */}
            <Button
                variant="outline"
                className="w-full h-11 border-[#FF9900] text-[#FF9900] hover:bg-slate-50 hover:text-[#2F2FE4] transition-all duration-300 flex items-center justify-center gap-2 font-semibold"
            >
                <Cloud size={18} />
                <span>AWS</span>
            </Button>
        </div>
    );
}
