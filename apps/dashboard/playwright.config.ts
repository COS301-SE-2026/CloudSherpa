import { defineConfig, devices } from "@playwright/test";

export default defineConfig({
    testDir: "./__tests__/e2e",
    fullyParallel: true,
    use: {
        baseURL: "http://localhost:3000",
        screenshot: "only-on-failure",
        video: "retain-on-failure",
        trace: "on-first-retry",
    },

    projects: [
        {
            name: "chromium",
            use: { ...devices["Desktop Chrome"] },
        },

        {
            name: "firefox",
            use: { ...devices["Desktop Firefox"] },
        },

        {
            name: "webkit",
            use: { ...devices["Desktop Safari"] },
        },
    ],
    expect: {
        timeout: 5000,
    },
    // Expects app to be already built
    webServer: {
        command: "npm run start",
        url: "http://localhost:3000",
        reuseExistingServer: true,
    },
});
