import { Button } from "@/components/atoms/button";
import { Input } from "@/components/atoms/input";
import { Label } from "@/components/atoms/label";
import { Loader2, Eye, EyeOff, AlertCircle, CheckCircle2Icon } from "lucide-react";
import { useState } from "react";
import { cn } from "@/lib/utils";
import { useRegistration } from "@/features/authentication/hooks/useRegistration";
import { RegisterRequestDto } from "@/features/authentication/types/dtos/auth/RegisterRequestDto";
import { Alert, AlertTitle, AlertDescription } from "@/components/atoms/alert";
import { useAuthInputValidation } from "@/features/authentication/hooks/useAuthInputValidation";

interface RegisterFormProps {
  isLoading?: boolean; //indicates loading state of form for asynchronous events.
  onToggle?: () => void;
}

export default function RegisterForm({ isLoading = false, onToggle }: Readonly<RegisterFormProps>) {
  const [isPasswordVisible, setIsPasswordVisible] = useState(false);
  const [isConfirmPasswordVisible, setIsConfirmPasswordVisible] = useState(false);
  const [confirmPassword, setConfirmPassword] = useState("");
  const [confirmPasswordError, setConfirmPasswordError] = useState("");

  const {
    email,
    password,
    emailError,
    passwordError,
    validateEmail,
    validatePassword,
  } = useAuthInputValidation();

  const { register, registrationFailure, registrationSuccess, redirectCountdown } = useRegistration();

  const togglePasswordVisibility = () => setIsPasswordVisible(!isPasswordVisible); //note I want to implement a spring loaded button instead of a togglable one for shoulder surfing.
  const toggleConfirmPasswordVisibility = () => setIsConfirmPasswordVisible(!isConfirmPasswordVisible);

  const validateConfirmPassword = (value: string) => {
    setConfirmPassword(value);
    if (value == password) {
      setConfirmPasswordError("");
    } else {
      setConfirmPasswordError("Passwords do not match");
    }
  };

  const handleFormSubmit = (e: React.SubmitEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (!emailError && !passwordError && !confirmPasswordError && email.length > 0) {
      const registerPayload: RegisterRequestDto = {
        email: email,
        username: email,
        password: password,
      };

      register(registerPayload);
    }
  };
  return (
    <div className="w-full max-w-sm space-y-8 p-4">
      <div className="text-center">
        <h2 className="text-3xl font-bold tracking-tight">Sign Up</h2>
      </div>

      {registrationSuccess && (
        <Alert>
          <CheckCircle2Icon />
          <AlertTitle>Successful Registration</AlertTitle>
          <AlertDescription>You will be redirected to the dashboard in {redirectCountdown} seconds</AlertDescription>
        </Alert>
      )}

      {registrationFailure && (
        <div className="text-center">
          <Alert variant="destructive">
            <AlertCircle />
            <AlertTitle>Failed To Register</AlertTitle>
            <AlertDescription>Incorrect Username or Password.</AlertDescription>
          </Alert>
        </div>
      )}

      <form className="space-y-6" onSubmit={handleFormSubmit} noValidate>
        <div className="space-y-2">
          <Label htmlFor="email">Email</Label>
          <Input
            id="email"
            name="email"
            type="email"
            value={email}
            onChange={(e) => validateEmail(e.target.value)}
            placeholder="name@company.com"
            required
            disabled={isLoading}
            className={cn(
              "pr-10",
              emailError ? "border-destructive focus-visible:ring-destructive" : "focus-visible:ring-ring",
            )}
          />
          {emailError && (
            <div className="flex items-center gap-2 text-destructive text-xs mt-1 animate-in fade-in duration-300">
              <AlertCircle size={14} />
              <span>{emailError}</span>
            </div>
          )}
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
              className={cn(
                "pr-10",
                confirmPasswordError ? "border-destructive focus-visible:ring-destructive" : "focus-visible:ring-ring",
              )}
            />
            <button
              type="button"
              aria-pressed={isPasswordVisible}
              aria-label="Toggle password visibility"
              onClick={togglePasswordVisibility}
              className="absolute right-2 top-1/2 transform -translate-y-1/2 text-gray-500 hover:text-primary focus:outline-none"
              disabled={isLoading}>
              {isPasswordVisible ? <Eye size={20} /> : <EyeOff size={20} />}
            </button>
          </div>
          {passwordError && (
            <div className="flex items-center gap-2 text-destructive text-xs mt-1 animate-in fade-in duration-300">
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
                confirmPasswordError ? "border-destructive focus-visible:ring-destructive" : "focus-visible:ring-ring",
              )}
            />
            <button
              type="button"
              aria-pressed={isConfirmPasswordVisible}
              aria-label="Toggle confirm password visibility"
              onClick={toggleConfirmPasswordVisibility}
              className="absolute right-2 top-1/2 transform -translate-y-1/2 text-gray-500 hover:text-primary focus:outline-none"
              disabled={isLoading}>
              {isConfirmPasswordVisible ? <Eye size={20} /> : <EyeOff size={20} />}
            </button>
          </div>
          {confirmPasswordError && (
            <div className="flex items-center gap-2 text-destructive text-xs mt-1 animate-in fade-in duration-300">
              <AlertCircle size={14} />
              <span>{confirmPasswordError}</span>
            </div>
          )}
        </div>

        <Button
          type="submit"
          className="w-full"
          disabled={
            isLoading ||
            !!emailError ||
            !!passwordError ||
            !!confirmPasswordError ||
            email.length === 0 ||
            password.length < 8 ||
            !/[A-Z]/.test(password) ||
            !/\d/.test(password) ||
            !/[!@#$%^&*()_+={}[\]:;"'<>,.?/|\\~`-]/.test(password) || // some regex that contains all allowed symobols.
            confirmPassword.length < 8 ||
            password !== confirmPassword
          }>
          {" "}
          {isLoading && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
          {isLoading ? "Authenticating..." : "Sign Up"}
        </Button>
      </form>

      <div className="mt-6 text-center md:hidden">
        <div className="text-sm text-muted-foreground">
          Already have an account?{" "}
          <button type="button" onClick={onToggle} className="font-medium text-primary hover:underline">
            Log in
          </button>
        </div>
      </div>
    </div>
  );
}
