import { test, expect, Page } from "@playwright/test";

async function createNewDashboard(page: Page) {
    const uniqueDashboardName = `testDash-${Date.now()}`;
    await page.getByRole("button", { name: "Dashboard Selector" }).click();
    await page.getByLabel("createNewDashOption").click();
    await page.getByLabel("createDashInput").fill(uniqueDashboardName);
    await page.getByRole("button", { name: "Create Dashboard" }).click();
    return uniqueDashboardName;
}

async function createNewChartWidget(page: Page) {
    await page.getByLabel("editbtn").click();
    await page.getByRole("button", { name: "Add Chart" }).click();
    await page.getByRole("button", { name: "Save" }).click();
}

async function configureChartWidgetName(page: Page) {
    await page.getByRole("textbox").fill("stuff");
    await page.getByLabel("save changes button").click();
    await expect(page.getByText("stuff")).toBeVisible();
}

test.describe("dashboard", () => {
    //login with demo before tests
    test.beforeEach(async ({ page, request }) => {
        const loginResponse = await request.post("http://localhost:8083/auth/login", {
            data: {
                email: "demo@gmail.com",
                password: "Demo-Password@2",
            },
        });

        expect(loginResponse.ok()).toBeTruthy();

        const storageState = await request.storageState();
        await page.context().addCookies(storageState.cookies);

        await page.goto("http://localhost:3000/dashboard");
        await expect(page.getByTestId("dashboard")).toBeVisible();
    });

    test("Create Dashboard", async ({ page }) => {
        const name = await createNewDashboard(page);
        await expect(page.getByLabel("dashboard selector dropdown")).toContainText(name);
    });

    test("Select preset time Window", async ({ page }) => {
        await page.getByLabel("window-selector").click();
        await page.getByLabel("1 hour").click();
        await expect(page.getByLabel("window-selector")).toContainText("1 hour");
    });

    //still need one for custom time window

    test("don't persist widget", async ({ page }) => {
        //create dash
        await createNewDashboard(page);
        //create chart widget
        await page.getByLabel("editbtn").click();
        await page.getByRole("button", { name: "Add Chart" }).click();
        await expect(page.getByText("New Chart")).toBeVisible;
        await page.getByLabel("editbtn").click();
        await expect(page.getByText("New Chart")).not.toBeVisible;
    });

    test("Create Dash & widget", async ({ page }) => {
        //create dash
        await createNewDashboard(page);
        //create chart widget
        await createNewChartWidget(page);
        await expect(page.getByText("New Chart")).toBeVisible;
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
