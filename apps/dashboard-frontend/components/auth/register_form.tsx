import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Loader2 } from "lucide-react";
import { Eye, EyeOff, AlertCircle } from "lucide-react";
import { useState } from "react";
import { cn } from "@/lib/utils";
import { SocialAuth } from "./social_auth";

interface RegisterFormProps {
  onSubmit?: (data: any) => void; //indicates form submission.
  isLoading?: boolean; //indicates loading state of form for asynchronous events.
}

export default function RegisterForm({ onSubmit, isLoading = false }: RegisterFormProps) {
  const [isPasswordVisible, setIsPasswordVisible] = useState(false);
  const [isConfirmPasswordVisible, setIsConfirmPasswordVisible] = useState(false);
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [passwordError, setPasswordError] = useState("");
  const [confirmPasswordError, setConfirmPasswordError] = useState("");

  const togglePasswordVisibility = () => setIsPasswordVisible(!isPasswordVisible);
  const toggleConfirmPasswordVisibility = () => setIsConfirmPasswordVisible(!isConfirmPasswordVisible);

  const validatePassword = (value: string) => {
    setPassword(value);

    // at least 8 characters
    //  alphanumeric
    const minLength = value.length >= 8;
    const isAlphanumericPlus = /^[a-zA-Z0-9!@#$%^&*()_+={}\[\]:;"'<>,.?/|\\~`-]*$/.test(value);

    if (!isAlphanumericPlus) {
      setPasswordError("Password contains invalid characters");
    } else if (!minLength && value.length > 0) {
      setPasswordError("Must be at least 8 characters");
    } else {
      setPasswordError("");
    }
  };

  const validateConfirmPassword = (value: string) => {
    setConfirmPassword(value);
    if (value !== password) {
      setConfirmPasswordError("Passwords do not match");
    } else {
      setConfirmPasswordError("");
    }
  };

  const handleFormSubmit = (e: React.FormEvent) => {
    e.preventDefault();
  };
  return (
    <div className="w-full max-w-sm space-y-8 p-4">
      <div className="text-center">
        <h2 className="text-3xl font-bold tracking-tight text-foreground">Sign Up</h2>
      </div>

      <form className="space-y-6" action="#" method="POST">
        <div className="space-y-2">
          <Label htmlFor="email">Email</Label>
          <Input id="email" name="email" type="email" placeholder="name@company.com" required disabled={isLoading} />
        </div>

        <div className="space-y-2">
          <div className="flex items-center justify-between">
            <Label htmlFor="password">Password</Label>
          </div>
          <div className="relative">
            <Input
              id="password"
              name="password"
              value={password}
              onChange={(e) => validatePassword(e.target.value)}
              type={isPasswordVisible ? "text" : "password"}
              required
              disabled={isLoading}
            />
            <button
              type="button"
              aria-pressed={isPasswordVisible}
              aria-label="Toggle password visibility"
              onClick={togglePasswordVisibility}
              className="absolute right-2 top-1/2 transform -translate-y-1/2 text-gray-500 hover:text-primary focus:outline-none"
              disabled={isLoading}>
              {isPasswordVisible ? <EyeOff size={20} /> : <Eye size={20} />}
            </button>
          </div>
          {passwordError && (
            <div className="flex items-center gap-2 text-red-500 text-xs mt-1 animate-in fade-in duration-300">
              <AlertCircle size={14} />
              <span>{passwordError}</span>
            </div>
          )}
        </div>

        <div className="space-y-2">
          <div className="flex items-center justify-between">
            <Label htmlFor="password">Confirm Password</Label>
          </div>
          <div className="relative">
            <Input
              id="confirmPassword"
              name="confirmPassword"
              type={isConfirmPasswordVisible ? "text" : "password"}
              onChange={(e) => validateConfirmPassword(e.target.value)}
              value={confirmPassword}
              required
              disabled={isLoading}
              className={cn(
                "pr-10",
                confirmPasswordError ? "border-red-500 focus-visible:ring-red-500" : "focus-visible:ring-[#2F2FE4]",
              )}
            />
            <button
              type="button"
              aria-pressed={isConfirmPasswordVisible}
              aria-label="Toggle confirm password visibility"
              onClick={toggleConfirmPasswordVisibility}
              className="absolute right-2 top-1/2 transform -translate-y-1/2 text-gray-500 hover:text-primary focus:outline-none"
              disabled={isLoading}>
              {isConfirmPasswordVisible ? <EyeOff size={20} /> : <Eye size={20} />}
            </button>
          </div>
          {confirmPasswordError && (
            <div className="flex items-center gap-2 text-red-500 text-xs mt-1 animate-in fade-in duration-300">
              <AlertCircle size={14} />
              <span>{confirmPasswordError}</span>
            </div>
          )}
        </div>

        <Button type="submit" className="w-full" disabled={isLoading || !!passwordError || !!confirmPasswordError || password.length < 8}> {/*i know the disabled is a bit redundent, I'll fix it*/}
          {isLoading && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
          {isLoading ? "Authenticating..." : "Sign Up"}
        </Button>

        <SocialAuth />
      </form>
    </div>
  );
}
