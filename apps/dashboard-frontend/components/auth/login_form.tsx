"use client";

import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Loader2, Eye, EyeOff, AlertCircle } from "lucide-react";
import { useState } from "react";
import { cn } from "@/lib/utils";
import { SocialAuth } from "./social_auth";

export default function LoginForm({ onSubmit, isLoading = false }: any) {
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
    <div className="w-full max-w-sm space-y-8 p-4 bg-white">
      <div className="text-center">
        <h2 className="text-3xl font-bold tracking-tight text-[#080616]">Sign in</h2>
      </div>

      <form className="space-y-6" onSubmit={handleFormSubmit}>
        <div className="space-y-2">
          <Label htmlFor="email">Email</Label>
          <Input id="email" type="email" placeholder="name@company.com" required disabled={isLoading} />
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
                error ? "border-red-500 focus-visible:ring-red-500" : "focus-visible:ring-[#2F2FE4]",
              )}
            />
            <button
              type="button"
              onClick={togglePasswordVisibility}
              className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-[#2F2FE4]">
              {isPasswordVisible ? <EyeOff size={18} /> : <Eye size={18} />}
            </button>
          </div>

          {error && (
            <div className="flex items-center gap-2 text-red-500 text-xs mt-1 animate-in fade-in duration-300">
              <AlertCircle size={14} />
              <span>{error}</span>
            </div>
          )}
        </div>

        <Button
          type="submit"
          className="w-full bg-[#2F2FE4] hover:bg-[#162E93]"
          disabled={isLoading || !!error || password.length < 8}>
          {isLoading ? <Loader2 className="mr-2 h-4 w-4 animate-spin" /> : "Sign in"}
        </Button>

        <SocialAuth />
      </form>
    </div>
  );
}
