import { test, expect, Page } from "@playwright/test";
import crypto from "crypto";

async function registerAndLoginNewUser(page: Page) {
    const uniqueId = crypto.randomUUID();
    const email = `e2e-dash-${uniqueId}@example.com`;
    const password = "SafePassword123!"; // Keep standard to pass validation rules

    await page.goto("http://localhost:3000/login");
    await page.getByRole("button", { name: "Get Started" }).click();

    //fill reg form
    await page.locator('input[name="email"]').fill(email);
    await page.locator('input[name="password"]').fill(password);
    await page.locator('input[name="confirmPassword"]').fill(password);

    //register
    await page.getByRole("button", { name: "Sign up" }).click();

    //auto logs in
    await expect(
        page.getByRole("alert").filter({ hasText: "Successful Registration" })
    ).toBeVisible();

    await expect(page.getByTestId("dashboard")).toBeVisible({ timeout: 15000 });

    return { email, password };
}

async function createNewDashboard(page: Page) {
    const uniqueDashboardName = `testDash-${Date.now()}`;
    await page.getByRole("button", { name: "Dashboard Selector" }).click();
    await page.getByLabel("createNewDashOption").click();
    await page.getByLabel("createDashInput").fill(uniqueDashboardName);
    await page.getByRole("button", { name: "Create Dashboard" }).click({ force: true });
    return uniqueDashboardName;
}

async function createNewChartWidget(page: Page) {
    await page.getByLabel("editbtn").click();
    await page.getByRole("button", { name: "Add Chart" }).click();
    await page.getByRole("button", { name: "Save" }).click();
    await expect(page.getByText("New Chart").first()).toBeVisible();
}

async function configureChartWidgetName(page: Page) {
    const uniqueWidgetName = `testDash-${Date.now()}`;
    await page.getByRole("textbox").fill(uniqueWidgetName);
    await page.getByLabel("save changes button").click();
    await expect(page.getByText(uniqueWidgetName)).toBeVisible();
    return uniqueWidgetName;
}

test.describe("dashboard", () => {
    test.beforeEach(async ({ page }) => {
        await registerAndLoginNewUser(page);
    });

    test("Create Dashboard", async ({ page }) => {
        const name = await createNewDashboard(page);
        await expect(page.getByLabel("dashboard selector dropdown")).toContainText(name);
    });

    test("Select preset time Window", async ({ page }) => {
        await createNewDashboard(page);
        await page.getByLabel("window selector button").click();
        await page.getByLabel("1 hour").click();
        await expect(page.getByLabel("window selector")).toContainText("1 hour");
    });

    //still need one for custom time window

    test("don't persist widget", async ({ page }) => {
        //create dash
        await createNewDashboard(page);
        //create chart widget
        await page.getByLabel("editbtn").click();
        await page.getByRole("button", { name: "Add Chart" }).click();
        await expect(page.getByText("New Chart")).toBeVisible();
        await page.getByLabel("editbtn").click();
        await expect(page.getByText("New Chart")).not.toBeVisible();
    });

    test("Create Dash & widget", async ({ page }) => {
        //create dash
        await createNewDashboard(page);
        //create chart widget
        await createNewChartWidget(page);
        await expect(page.getByText("New Chart")).toBeVisible();
    });

    test("Configure name Chart Widget", async ({ page }) => {
        //create dash
        await createNewDashboard(page);
        //create new chart widget
        await createNewChartWidget(page);
        //configure new chart widget
        await page.getByRole("button", { name: "chart options button" }).click();
        await page.getByLabel("configure widget button").click();
        await configureChartWidgetName(page);
    });

    test("Configure name Chart Widget v2", async ({ page }) => {
        //create dash
        await createNewDashboard(page);
        //create new chart widget
        await createNewChartWidget(page);
        //configure new chart widget
        await page.getByText("New Chart").click({ button: "right" });
        await page.getByRole("menuitem", { name: "configure widget button" }).click();
        await configureChartWidgetName(page);
    });
});
