import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Loader2 } from "lucide-react";
import { Eye, EyeOff } from "lucide-react";
import { useState } from "react"; 


interface RegisterFormProps {
  onSubmit?: (data: any) => void; //indicates form submission.
  isLoading?: boolean; //indicates loading state of form for asynchronous events.
}

export default function RegisterForm({ onSubmit, isLoading = false }: RegisterFormProps) {
  const [isPasswordVisible, setIsPasswordVisible] = useState(false);
  const [isConfirmPasswordVisible, setIsConfirmPasswordVisible] = useState(false);

  const togglePasswordVisibility = () => setIsPasswordVisible(!isPasswordVisible);
  const toggleConfirmPasswordVisibility = () => setIsConfirmPasswordVisible(!isConfirmPasswordVisible);
  return (
    <div className="w-full max-w-sm space-y-8 p-4">
      <div className="text-center">
        <h2 className="text-3xl font-bold tracking-tight text-foreground">Sign Up</h2>
      </div>

      <form className="space-y-6" action="#" method="POST">
        <div className="space-y-2">
          <Label htmlFor="email">Email</Label>
          <Input id="email" name="email" type="email" placeholder="name@company.com" required disabled={isLoading}/>
        </div>

        <div className="space-y-2">
          <div className="flex items-center justify-between">
            <Label htmlFor="password">Password</Label>
          </div>
          <div className="relative">
            <Input 
              id="password" 
              name="password" 
              type={isPasswordVisible ? "text" : "password"}
              required 
              disabled={isLoading}
              className="pr-10"
            />
            <button
              type="button"
              aria-pressed={isPasswordVisible}
              aria-label="Toggle password visibility"
              onClick={togglePasswordVisibility}
              className="absolute right-2 top-1/2 transform -translate-y-1/2 text-gray-500 hover:text-primary focus:outline-none focus:ring-2 focus:ring-blue-500"
              disabled={isLoading}
            >
              {isPasswordVisible ? <EyeOff size={20} /> : <Eye size={20} />}
            </button>
          </div>
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
              required 
              disabled={isLoading}
              className="pr-10"
            />
            <button
              type="button"
              aria-pressed={isConfirmPasswordVisible}
              aria-label="Toggle confirm password visibility"
              onClick={toggleConfirmPasswordVisibility}
              className="absolute right-2 top-1/2 transform -translate-y-1/2 text-gray-500 hover:text-primary focus:outline-none focus:ring-2 focus:ring-blue-500"
              disabled={isLoading}
            >
              {isConfirmPasswordVisible ? <EyeOff size={20} /> : <Eye size={20} />}
            </button>
          </div>
        </div>

        <Button type="submit" className="w-full" disabled={isLoading}>
          {isLoading && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
          {isLoading ? "Authenticating..." : "Sign Up"}
        </Button>
      </form>
    </div>
  );
}
