import { defineConfig } from '@playwright/test';

// Runs against a started server (scripts/run-local.ps1, the jar with the screens in it):
//   CRM_URL=http://localhost:18090 npx playwright test
// It drives the Chrome installed on the machine (channel 'chrome'), so no browser download.
export default defineConfig({
  testDir: './e2e',
  timeout: 60_000,
  workers: 1,
  reporter: [['list']],
  use: {
    baseURL: process.env.CRM_URL ?? 'http://localhost:18090',
    channel: 'chrome',
    headless: true,
    viewport: { width: 1440, height: 900 },
    locale: 'ko-KR',
    acceptDownloads: true,
    screenshot: 'only-on-failure',
  },
});
