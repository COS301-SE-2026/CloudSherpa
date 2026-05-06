import { cn } from "@/lib/utils";

interface AuthPanelProps {
  children: React.ReactNode;
  isActive: boolean;
  isSignUpState: boolean;
}

//wraps the login and register forms for the animation
//will need to adjust this to make it responsive
export function AuthPanel({ children, isActive, isSignUpState }: AuthPanelProps) {
  return (
    <div
      className={cn(
        "absolute top-0 h-full w-1/2 transition-all duration-700 ease-in-out left-0 flex items-center justify-center",
        // when isSignUpState is true the pnael translates-x-full(moves to he other side), if false it stays in place translate-x-0
        isSignUpState ? "translate-x-full" : "translate-x-0",
        // ensures the current panel is in focus
        isActive ? "opacity-100 z-20 animate-show" : "opacity-0 z-10"
      )}
    >
      {children}
    </div>
  );
}