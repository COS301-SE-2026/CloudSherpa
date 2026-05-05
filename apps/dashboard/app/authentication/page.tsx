"use client"; //use client indicates that the page will be rendered by the client browser and not the server

import { useState } from "react";
import { AuthOverlay } from "@/components/auth/auth_overlay";
import { AuthPanel } from "@/components/auth/auth_panel";
import LoginForm from "@/components/auth/login_form";
import RegisterForm from "@/components/auth/register_form";

export default function Authentication() {
  const [isSignUp, setIsSignUp] = useState(false);

  return (
    <div className="fixed inset-0 overflow-hidden bg-white font-sans">
      <div className="relative w-full h-full">
        
        {/* login container */}
        {/* if isActive is false react unmounts the component which resets the state so it feels like you are navigating to a fresh page, 
        also removes previous errors in state of the components */}
        <AuthPanel isActive={!isSignUp} isSignUpState={isSignUp}>
          <LoginForm key={isSignUp ? "hidden" : "active"} />
        </AuthPanel>

        {/* register container */}
        <AuthPanel isActive={isSignUp} isSignUpState={isSignUp}>
          <RegisterForm key={isSignUp ? "active" : "hidden"} />
        </AuthPanel>

        {/* sliding overlay */}
        <AuthOverlay isSignUp={isSignUp} toggle={setIsSignUp} />
        
      </div>
    </div>
  );
}
