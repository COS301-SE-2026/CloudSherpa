import { test, expect } from '@playwright/test';

test('displays cloud costs', async ({ page }) => {
  await page.goto('http://localhost:3000');

  // Expect a title "to contain" a substring.
  await expect(page.getByRole('heading', {name: 'Cloud Costs'} )).toBeVisible();
});

