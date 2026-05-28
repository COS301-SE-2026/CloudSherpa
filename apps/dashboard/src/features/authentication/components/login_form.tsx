"use client";

import { Button } from "@/components/atoms/button";
import { Input } from "@/components/atoms/input";
import { Label } from "@/components/atoms/label";
import { Loader2, Eye, EyeOff, AlertCircle, AlertCircleIcon } from "lucide-react";
import { useState } from "react";
import { cn } from "@/lib/utils";
import { useLogin } from "@/features/authentication/hooks/useLogin";
import { LoginRequestDto } from "@/features/authentication/types/dtos/auth/LoginRequestDto";
import { 
  Alert,
  AlertTitle,
  AlertDescription
} from "@/components/atoms/alert";

interface LoginFormProps {
  isLoading?: boolean;
  onToggle?: () => void;
}

export default function LoginForm({ isLoading = false, onToggle }: LoginFormProps) {
  const [isPasswordVisible, setIsPasswordVisible] = useState(false);
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [emailError, setEmailError] = useState(""); //email and password specific error messages
  const [passwordError, setPasswordError] = useState("");
  const { login, loginFailure } = useLogin();

  const togglePasswordVisibility = () => setIsPasswordVisible(!isPasswordVisible);

  const validateEmail = (value: string) => {
    setEmail(value);
    
    if (value.length === 0) {
      setEmailError("");
      return;
    }

    if (value.includes(" ")) {
      setEmailError("Email cannot contain spaces");
    } else if (!value.includes("@")) {
      setEmailError("Email must contain an '@' symbol");
    } else {
      const parts = value.split("@");
      if (parts[0].length === 0) {
        setEmailError("Email must have a username before '@'");
      } else if (parts[1].length === 0) {
        setEmailError("Email must have a domain after '@'");
      } else if (!parts[1].includes(".")) {
        setEmailError("Domain must contain a '.' (e.g., .com)");
      } else {
        const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
        if (!emailRegex.test(value)) {
          setEmailError("Please enter a valid email address");
        } else {
          setEmailError("");
        }
      }
    }
  };

  const validatePassword = (value: string) => {
    setPassword(value);

    if (value.length === 0) {
      setPasswordError("");
      return;
    }

    // at least 8 characters
    //  alphanumeric
    const minLength = value.length >= 8;
    const isAlphanumericPlus = /^[a-zA-Z0-9!@#$%^&*()_+={}\[\]:;"'<>,.?/|\\~`-]*$/.test(value);
    const hasUpperCase = /[A-Z]/.test(value); //checks if password has uppercases
    const hasNumber = /[0-9]/.test(value); //checks if password has numbers
    const hasSymbol = /[!@#$%^&*()_+={}\[\]:;"'<>,.?/|\\~`-]/.test(value); //checks if password has symbols

    if (!isAlphanumericPlus) {
      setPasswordError("Password contains invalid characters");
    } else if (!minLength) {
      setPasswordError("Must be at least 8 characters");
    } else if (!hasUpperCase) {
      setPasswordError("Must contain at least one uppercase letter (A-Z)");
    } else if (!hasNumber) {
      setPasswordError("Must contain at least one number (0-9)");
    } else if (!hasSymbol) {
      setPasswordError("Must contain at least one symbol (!, @, #, $, etc.)");
    } else {
      setPasswordError("");
    }
  };

  const handleFormSubmit = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (!emailError && !passwordError && email.length > 0) {
      const loginPayload: LoginRequestDto = {
        email: email,
        password: password
      }

      login(loginPayload);
    }
  };

  return (
    <div className="w-full max-w-sm space-y-8 p-4">
      <div className="text-center">
        <h2 className="text-3xl font-bold tracking-tight">Sign in</h2>
      </div>

      {loginFailure && (
        <div className="text-center">
          <Alert variant="destructive">
            <AlertCircleIcon/>
            <AlertTitle>Failed To Log In</AlertTitle>
            <AlertDescription>
              Incorrect Username or Password.
            </AlertDescription>
          </Alert>
        </div>)
      }


      <form className="space-y-6" onSubmit={handleFormSubmit} noValidate>
        <div className="space-y-2">
          <Label htmlFor="email">Email</Label>
          <Input
            id="email"
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
          <Label htmlFor="password">Password</Label>
          <div className="relative">
            <Input
              id="password"
              type={isPasswordVisible ? "text" : "password"}
              value={password}
              onChange={(e) => validatePassword(e.target.value)}
              required
              disabled={isLoading}
              className={cn(
                "pr-10",
                passwordError ? "border-destructive focus-visible:ring-destructive" : "focus-visible:ring-ring",
              )}
            />
            <button
              type="button"
              onClick={togglePasswordVisibility}
              className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-ring">
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

        <Button
          type="submit"
          className="w-full"
          disabled={
            isLoading ||
            !!emailError ||
            !!passwordError ||
            email.length === 0 ||
            password.length < 8 ||
            !/[A-Z]/.test(password) ||
            !/[0-9]/.test(password) ||
            !/[!@#$%^&*()_+={}\[\]:;"'<>,.?/|\\~`-]/.test(password)
          }>
          {" "}
          {isLoading && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
          {isLoading ? "Authenticating..." : "Log In"}
        </Button>

      </form>

      <div className="mt-6 text-center md:hidden">
        <div className="text-sm text-muted-foreground">
          Don&apos;t have an account?{" "}
          <button
            type="button"
            onClick={onToggle}
            className="font-medium text-primary hover:underline"
          >
            Sign up
          </button>
        </div>
      </div>
    </div>
  );
}
