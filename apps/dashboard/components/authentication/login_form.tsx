"use client";

import { Button } from "@/components/atoms/button";
import { Input } from "@/components/atoms/input";
import { Label } from "@/components/atoms/label";
import { Loader2, Eye, EyeOff, AlertCircle } from "lucide-react";
import { useState } from "react";
import { cn } from "@/lib/utils";
import { SocialAuth } from "./social_auth";

interface LoginFormProps {
  onSubmit?: (data: { password?: string }) => void;
  isLoading?: boolean;
}

export default function LoginForm({ onSubmit, isLoading = false }: LoginFormProps) {
  const [isPasswordVisible, setIsPasswordVisible] = useState(false);
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");

  const togglePasswordVisibility = () => setIsPasswordVisible(!isPasswordVisible);

  const validatePassword = (value: string) => {
    setPassword(value);

    // at least 8 characters
    //  alphanumeric
    const minLength = value.length >= 8;
    const isAlphanumericPlus = /^[a-zA-Z0-9!@#$%^&*()_+={}\[\]:;"'<>,.?/|\\~`-]*$/.test(value);

    if (!isAlphanumericPlus) {
      setError("Password contains invalid characters");
    } else if (!minLength && value.length > 0) {
      setError("Must be at least 8 characters");
    } else {
      setError("");
    }
  };

  const handleFormSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!error && onSubmit) onSubmit({ password });
  };

  return (
    <div className="w-full max-w-sm space-y-8 p-4">
      <div className="text-center">
        <h2 className="text-3xl font-bold tracking-tight">Sign in</h2>
      </div>

      <form className="space-y-6" onSubmit={handleFormSubmit} noValidate>
        <div className="space-y-2">
          <Label htmlFor="email">Email</Label>
          <Input
            id="email"
            type="email"
            placeholder="name@company.com"
            required
            disabled={isLoading}
            className={cn(
              "pr-10",
              error ? "border-destructive focus-visible:ring-destructive" : "focus-visible:ring-ring",
            )}
          />
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
                error ? "border-destructive focus-visible:ring-destructive" : "focus-visible:ring-ring",
              )}
            />
            <button
              type="button"
              onClick={togglePasswordVisibility}
              className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-ring">
              {isPasswordVisible ? <Eye size={20} /> : <EyeOff size={20} />}
            </button>
          </div>

          {error && (
            <div className="flex items-center gap-2 text-destructive text-xs mt-1 animate-in fade-in duration-300">
              <AlertCircle size={14} />
              <span>{error}</span>
            </div>
          )}
        </div>

        <Button
          type="submit"
          className="w-full"
          disabled={isLoading || password.length < 8}>
          {" "}
          {/*i know the disabled is a bit redundent, I'll fix it*/}
          {isLoading && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
          {isLoading ? "Authenticating..." : "Sign Up"}
        </Button>

      </form>
    </div>
  );
}
