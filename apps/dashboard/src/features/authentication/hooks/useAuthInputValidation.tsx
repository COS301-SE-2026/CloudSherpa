import { useState, useCallback } from "react";

export function useAuthInputValidation() {
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [emailError, setEmailError] = useState("");
    const [passwordError, setPasswordError] = useState("");

    const validateEmail = useCallback((value: string) => {
        setEmail(value);

        const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
        const parts = value.split("@");

        if (value.length === 0) {
            setEmailError("");
        } else if (value.includes(" ")) {
            setEmailError("Email cannot contain spaces");
        } else if (value.includes("@")) {
            if (parts[0].length === 0) {
                setEmailError("Email must have a username before '@'");
            } else if (parts[1].length === 0) {
                setEmailError("Email must have a domain after '@'");
            } else if (parts[1].includes(".")) {
                if (emailRegex.test(value)) {
                    setEmailError("");
                } else {
                    setEmailError("Please enter a valid email address");
                }
            } else {
                setEmailError("Domain must contain a '.' (e.g., .com)");
            }
        } else {
            setEmailError("Email must contain an '@' symbol");
        }
    }, []);

    const validatePassword = useCallback((value: string) => {
        setPassword(value);

        if (value.length === 0) {
            setPasswordError("");
            return;
        }

        const minLength = value.length >= 8;
        const isAlphanumericPlus = /^[a-zA-Z0-9!@#$%^&*()_+={}[\]:;"'<>,.?/|\\~`-]*$/.test(value);
        const hasUpperCase = /[A-Z]/.test(value);
        const hasNumber = /\d/.test(value);
        const hasSymbol = /[!@#$%^&*()_+={}[\]:;"'<>,.?/|\\~`-](?:_)?/.test(value);

        if (!isAlphanumericPlus) {
            setPasswordError("Password contains invalid characters");
        } else if (!minLength) {
            setPasswordError("Must be at least 8 characters");
        } else if (!hasUpperCase) {
            setPasswordError("Must contain at least one uppercase letter (A-Z)");
        } else if (!hasNumber) {
            setPasswordError("Must contain at least one number (0-9)");
        } else if (hasSymbol) {
            setPasswordError("");
        } else {
            setPasswordError("Must contain at least one symbol (!, @, #, $, etc.)");
        }
    }, []);

    return {
        email,
        password,
        emailError,
        passwordError,
        validateEmail,
        validatePassword,
    };
}
