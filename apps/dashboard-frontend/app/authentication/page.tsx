"use client"; //use client indicates that the page will be rendered by the client browser and not the server

import { useState } from "react";
import { Button } from "@/components/ui/button";
import { cn } from "@/lib/utils"; // this is a utility function used to conditionally join CSS class names together while resolving conflicts
import LoginForm from "@/components/auth/login_form";
import RegisterForm from "@/components/auth/register_form";

export default function Authentication() {
  const [isSignUp, setIsSignUp] = useState(false);
  return (
    <div className="fixed inset-0 overflow-hidden bg-white font-sans">
      <div className="relative w-full h-full">
        {/* Sign In Container (Login) */}
        <div
          className={cn(
            "absolute top-0 h-full w-1/2 transition-all duration-700 ease-in-out left-0",
            isSignUp ? "translate-x-full opacity-0 z-10" : "opacity-100 z-20",
          )}>
          <div className="flex items-center justify-center h-full">
            <LoginForm />
          </div>
        </div>

        {/* Sign Up Container (Register) - Moved second in DOM to stay on top */}
        <div
          className={cn(
            "absolute top-0 h-full w-1/2 transition-all duration-700 ease-in-out left-0",
            isSignUp ? "translate-x-full opacity-100 z-50 animate-show" : "opacity-0 z-10",
          )}>
          <div className="flex items-center justify-center h-full">
            <RegisterForm />
          </div>
        </div>

        {/* Overlay Container (The container that slides from one side to the other) */}
        <div
          className={cn(
            "absolute top-0 left-1/2 w-1/2 h-full overflow-hidden transition-transform duration-700 ease-in-out z-[100]",
            isSignUp ? "-translate-x-full" : "",
          )}>
          <div
            className={cn(
              "relative -left-full h-full w-[200%] transform transition-transform duration-700 ease-in-out bg-gradient-to-br from-slate-900 via-blue-900 to-slate-900 text-white",
              isSignUp ? "translate-x-1/2" : "translate-x-0",
            )}>
            {/* if on register/sign up this is the panel on left that takes you to login */}
            <div
              className={cn(
                "absolute top-0 flex flex-col items-center justify-center px-20 text-center h-full w-1/2 transition-transform duration-700 ease-in-out",
                isSignUp ? "translate-x-0" : "-translate-x-[20%]",
              )}>
              <h1 className="text-5xl font-extrabold mb-6">Welcome Back!</h1>
              <p className="text-lg font-light mb-10 max-w-md">
                Ready to find more savings? Log in to continue your cloud cost optimization.
              </p>
              <Button
                size="lg"
                className=" bg-transparent border-slate-50 text-slate-50 hover:bg-slate-50 hover:text-blue-900 uppercase text-sm font-bold transition-colors"
                onClick={() => setIsSignUp(false)}>
                Sign In
              </Button>
            </div>

            {/* if on login this is the panel on the right that takes you to register(get started) */}
            <div
              className={cn(
                "absolute top-0 right-0 flex flex-col items-center justify-center px-20 text-center h-full w-1/2 transition-transform duration-700 ease-in-out",
                isSignUp ? "translate-x-[20%]" : "translate-x-0",
              )}>
              <h1 className="text-5xl font-extrabold mb-6">CloudSherpa</h1>
              <p className="text-lg font-light mb-10 max-w-md">
                Stop overpaying for cloud services. 
                <br />
                Join us today.
              </p>
              <Button
                size="lg"
                className="bg-transparent border-slate-50 text-white hover:bg-slate-50 hover:text-blue-900 uppercase text-sm font-bold transition-colors"
                onClick={() => setIsSignUp(true)}>
                Get Started
              </Button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
