import { Button } from "@/components/ui/button";
import { cn } from "@/lib/utils";

interface OverlayProps {
  isSignUp: boolean;
  toggle: (val: boolean) => void;
}

//this is the sliding container for login and register
export function AuthOverlay({ isSignUp, toggle }: OverlayProps) {
  return (
    <div className={cn(
      "absolute top-0 left-1/2 w-1/2 h-full overflow-hidden transition-transform duration-700 ease-in-out z-[100]",
      isSignUp ? "-translate-x-full" : ""
    )}>
      <div className={cn(
        "relative -left-full h-full w-[200%] transform transition-transform duration-700 ease-in-out bg-primary text-white", //adjust this to change the background
        isSignUp ? "translate-x-1/2" : "translate-x-0"
      )}>
        {/* gp to Login */}
        <div className={cn(
          "absolute top-0 flex flex-col items-center justify-center px-20 text-center h-full w-1/2 transition-transform duration-700 ease-in-out",
          isSignUp ? "translate-x-0" : "-translate-x-[20%]"
        )}>
          <h1 className="text-5xl font-extrabold mb-6">Welcome Back</h1>
          <p className="text-lg font-light mb-10 max-w-md">Return to your FinOps journey</p>
          <Button variant="outline" className="text-primary hover:text-slate-50 hover:bg-primary" onClick={() => toggle(false)}>
            Sign In
          </Button>
        </div>

        {/* go to register */}
        <div className={cn(
          "absolute top-0 right-0 flex flex-col items-center justify-center px-20 text-center h-full w-1/2 transition-transform duration-700 ease-in-out",
          isSignUp ? "translate-x-[20%]" : "translate-x-0"
        )}>
          <h1 className="text-5xl font-extrabold mb-6">CloudSherpa</h1>
          <p className="text-lg font-light mb-10 max-w-md">Start your optimization journey</p>
          <Button variant="outline" className=" text-primary hover:text-slate-50 hover:bg-primary" onClick={() => toggle(true)}>
            Get Started
          </Button>
        </div>
      </div>
    </div>
  );
}