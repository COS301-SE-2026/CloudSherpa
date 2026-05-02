import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Loader2 } from "lucide-react";

interface RegisterFormProps {
  onSubmit?: (data: any) => void; //indicates form submission.
  isLoading?: boolean; //indicates loading state of form for asynchronous events.
}

export default function RegisterForm({ onSubmit, isLoading = false }: RegisterFormProps) {
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
          <Input id="password" name="password" type="password" required disabled={isLoading} />
        </div>

        <div className="space-y-2">
          <div className="flex items-center justify-between">
            <Label htmlFor="password">Confirm Password</Label>
          </div>
          <Input id="confirmPassword" name="confirmPassword" type="password" required disabled={isLoading} />
        </div>

        <Button type="submit" className="w-full" disabled={isLoading}>
          {isLoading && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
          {isLoading ? "Authenticating..." : "Sign Up"}
        </Button>
      </form>
    </div>
  );
}
