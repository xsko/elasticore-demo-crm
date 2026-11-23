import { expect, test } from '@playwright/test';
import ExcelJS from 'exceljs';

/**
 * Run by scripts/dsl-change-demo.ps1 after it has added one field to the DSL and rebuilt:
 * the field must appear in the list, the form, the Excel file and in both languages,
 * with no screen code changed. Skipped otherwise.
 */
const field = process.env.DSL_FIELD;      // e.g. "segment"
const ko = process.env.DSL_LABEL_KO ?? '';
const en = process.env.DSL_LABEL_EN ?? '';

test.skip(!field, 'run by scripts/dsl-change-demo.ps1');

test('a field added to the DSL appears in the list, the form, Excel and both languages', async ({ page }) => {
  await page.goto('/login');
  await page.evaluate(() => localStorage.setItem('crm.lang', 'ko'));
  await page.evaluate(() => localStorage.removeItem('grid:Account'));
  await page.goto('/login');
  await page.getByRole('textbox').first().fill('minjun');
  await page.locator('input[type="password"]').fill('demo1234');
  await page.locator('form button[type="submit"]').click();
  await page.waitForURL((u) => !u.pathname.startsWith('/login'));

  await page.goto('/account');
  await expect(page.locator('.ant-table-thead')).toContainText(ko);          // the list's column
  await expect(page.locator('.crm-filterbar')).toContainText(ko);            // its search (@search)

  await page.getByRole('button', { name: /새로 만들기/ }).click();
  await expect(page.locator('.ant-drawer')).toContainText(ko);               // the form's input
  await page.keyboard.press('Escape');

  await page.getByRole('button', { name: /엑셀/ }).click();
  const [file] = await Promise.all([page.waitForEvent('download'), page.getByText('조건 결과 전체').click()]);
  const wb = new ExcelJS.Workbook();
  await wb.xlsx.readFile(await file.path());
  const header = (wb.worksheets[0].getRow(1).values as unknown[]).slice(1);
  expect(header).toContain(ko);                                              // the Excel header

  await page.locator('.crm-lang').getByText('English').click();
  await expect(page.locator('.ant-table-thead')).toContainText(en);          // the English label
});
