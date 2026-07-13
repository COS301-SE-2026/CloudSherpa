import { cn } from "@/lib/utils";

interface AuthPanelProps {
  children: React.ReactNode;
  isActive: boolean;
  isSignUpState: boolean;
}

//wraps the login and register forms for the animation
//will need to adjust this to make it responsive
export function AuthPanel({ children, isActive, isSignUpState }: Readonly<AuthPanelProps>) {
  return (
    <div
      className={cn(
        "absolute top-0 h-full transition-all duration-0 md:duration-700 ease-in-out left-0 flex items-center justify-center",
        "w-full md:w-1/2 bg-background",
        // On mobile, stay in place. On desktop, translate across the screen.
        isSignUpState ? "translate-x-0 md:translate-x-full" : "translate-x-0",
        // ensures the current panel is in focus
        isActive ? "opacity-100 z-20 animate-show pointer-events-auto" : "opacity-0 z-10 pointer-events-none"
      )}
    >
      {children}
    </div>
  );
}