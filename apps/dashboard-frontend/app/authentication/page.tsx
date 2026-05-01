import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import LoginForm from "@/components/auth/login_form";
import RegisterForm from "@/components/auth/register_form";


export default function Authentication() {
  return (
    <div className="flex flex-row w-full h-full justify-center">
      <LoginForm/>
      <RegisterForm/>
    </div>

  );
}
