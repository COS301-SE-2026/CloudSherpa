// @ts-check

/**
 * Assumptions:
 * Test user reset after each test
 */
import { test, expect } from "@playwright/test";

test.describe("authentication", () => {
    const email = `e2e-user-${Date.now()}-${crypto.randomUUID()}@example.com`;
    test("unauthenticated dashboard access redirects to login", async ({ page }) => {
        await page.goto("http://localhost:3000/dashboard");

        await expect(page.getByRole("heading", { name: "Sign in" })).toBeVisible();
    });

    test("incorrect login details", async ({ page }) => {
        await page.goto("http://localhost:3000/login");

        const loginForm = page.locator("form").filter({
            has: page.getByRole("button", { name: "Log In" }),
        });
        await loginForm.getByLabel("Email", { exact: true }).fill("nonsensicleemail@gmail.com");
        await loginForm.getByLabel("Password", { exact: true }).fill("Randopassword123!");

        await loginForm.getByRole("button", { name: "Log In" }).click();

        await expect(page.getByRole("alert").filter({ hasText: "Failed To Log In" })).toBeVisible();
    });

    test("new user signup, login, logout", async ({ page }) => {
        await page.goto("http://localhost:3000/login");

        await page.getByRole("button", { name: "Get Started" }).click();

        // fill in form
        await page.locator('input[name="email"]').fill(email);
        await page.locator('input[name="password"]').fill("SecretPassword123!");
        await page.locator('input[name="confirmPassword"]').fill("SecretPassword123!");

        // Click on register button
        await page.getByRole("button", { name: "Sign up" }).click();

        const successAlert = page.getByRole("alert").filter({ hasText: "Successful Registration" });

        await successAlert.isVisible();

        // assert that redirected to dashboard
        await expect(page.getByTestId("dashboard")).toBeVisible({ timeout: 12000 });

        // Logout
        await page.getByRole("button", { name: "Logout" }).click();
        await expect(page.getByRole("heading", { name: "Sign in" })).toBeVisible();

        // Cant navigate to dasbhoard
        await page.goto("http://localhost:3000/dashboard");
        await expect(page.getByRole("heading", { name: "Sign in" })).toBeVisible();

        // Login
        await page.getByLabel("Email").fill(email);
        await page.getByLabel("Password", { exact: true }).fill("SecretPassword123!");
        await page.getByRole("button", { name: "Log In" }).click();

        await expect(page.getByTestId("dashboard")).toBeVisible();
    });
});
