import { expect, test, type Page } from '@playwright/test';
import ExcelJS from 'exceljs';

/**
 * The screens as a user meets them: sign-in, the grid's server paging kept in the address,
 * the two Excel downloads, the language switch, and what a customer account does not see.
 * Runs on the sample data; the demo passwords are the README's.
 */

async function signIn(page: Page, login: string) {
  await page.goto('/login');
  await page.getByRole('textbox').first().fill(login);
  await page.locator('input[type="password"]').fill('demo1234');
  await page.locator('form button[type="submit"]').click();
  await page.waitForURL((url) => !url.pathname.startsWith('/login'));
}

async function setLanguage(page: Page, lang: 'ko' | 'en') {
  await page.evaluate((l) => localStorage.setItem('crm.lang', l), lang);
}

async function readXlsx(path: string) {
  const wb = new ExcelJS.Workbook();
  await wb.xlsx.readFile(path);
  const ws = wb.worksheets[0];
  const rows: unknown[][] = [];
  ws.eachRow((r) => rows.push((r.values as unknown[]).slice(1)));
  return rows;
}

test.beforeEach(async ({ page }) => {
  await page.goto('/login');
  await setLanguage(page, 'ko');
});

test('the grid pages on the server and keeps the page in the address', async ({ page }) => {
  await signIn(page, 'minjun');
  await page.goto('/opportunity');
  const range = page.locator('.crm-grid-range');
  await expect(range).toContainText('1-20');
  const total = Number((await range.innerText()).replace(/[^0-9 ]/g, ' ').trim().split(/\s+/).pop());
  expect(total).toBeGreaterThan(20);

  await page.locator('.ant-pagination-item-2').click();
  await expect(range).toContainText('21-40');
  await expect(page).toHaveURL(/currentPage=2/);

  await page.reload();
  await expect(range).toContainText('21-40');
  await expect(page.locator('.ant-table-tbody tr.ant-table-row')).toHaveCount(20);
});

test('sorting by a column asks the server for that order', async ({ page }) => {
  await signIn(page, 'minjun');
  await page.goto('/opportunity');
  const header = page.locator('.ant-table-thead th', { hasText: '예상 금액' }).first();
  await header.click();
  await header.click();
  await expect(page).toHaveURL(/sorters.*amount.*desc/);
  const titles = (await page.locator('.ant-table-thead tr').first().locator('th').allInnerTexts()).map((h) => h.trim());
  const col = titles.indexOf('예상 금액') + 1;
  await expect(page.locator('.ant-table-tbody tr.ant-table-row').first()).toBeVisible();
  const amounts = await page.locator(`.ant-table-tbody tr.ant-table-row td:nth-child(${col})`).allInnerTexts();
  const values = amounts.map((a) => Number(a.replace(/[^0-9.-]/g, '')));   // $431,000 / ₩431,000,000
  expect(values.every((v) => Number.isFinite(v))).toBeTruthy();
  expect(values).toEqual([...values].sort((a, b) => b - a));
});

test('excel: the table as shown, and every matching row', async ({ page }) => {
  await signIn(page, 'minjun');
  await page.goto('/opportunity');
  const range = page.locator('.crm-grid-range');
  await expect(range).toContainText('1-20');
  const total = Number((await range.innerText()).replace(/[^0-9 ]/g, ' ').trim().split(/\s+/).pop());
  const headers = (await page.locator('.ant-table-thead th').allInnerTexts()).map((h) => h.trim()).filter(Boolean);

  await page.getByRole('button', { name: /엑셀/ }).click();
  const [shown] = await Promise.all([page.waitForEvent('download'), page.getByText('지금 보이는 표').click()]);
  const shownRows = await readXlsx(await shown.path());
  expect(shownRows[0]).toEqual(headers);
  expect(shownRows.length - 1).toBe(20);

  await page.getByRole('button', { name: /엑셀/ }).click();
  const [all] = await Promise.all([page.waitForEvent('download'), page.getByText('조건 결과 전체').click()]);
  const allRows = await readXlsx(await all.path());
  expect(allRows[0]).toEqual(headers);
  expect(allRows.length - 1).toBe(total);
});

test('the language switch relabels the screens from the DSL', async ({ page }) => {
  await signIn(page, 'minjun');
  await page.goto('/account');
  await expect(page.locator('.ant-table-thead')).toContainText('고객사명');
  await page.locator('.crm-lang').getByText('English').click();
  await expect(page.locator('.ant-table-thead')).toContainText('Account name');
  await expect(page.locator('.crm-sider')).toContainText('Opportunity');
});

test('an enumeration shows its label in the chosen language (label / label_en)', async ({ page }) => {
  await signIn(page, 'minjun');
  await page.goto('/pipeline');
  await expect(page.locator('main, .crm-content').first()).toContainText('발굴');
  await page.locator('.crm-lang').getByText('English').click();
  await expect(page.locator('main, .crm-content').first()).toContainText('Prospecting');
  await expect(page.locator('main, .crm-content').first()).toContainText('Negotiation');
  await page.screenshot({ path: 'test-results/pipeline-en.png' });
});

/** A call to the API in the browser's session: the session cookie and the XSRF token it holds. */
async function api(page: Page, path: string, data: unknown) {
  const xsrf = (await page.context().cookies()).find((c) => c.name === 'XSRF-TOKEN')?.value ?? '';
  const res = await page.request.post(path, { data, headers: { 'X-XSRF-TOKEN': xsrf } });
  expect(res.ok(), `${path}: ${res.status()}`).toBeTruthy();
  return res.json();
}

test('a sales rep reads every inquiry and answers one (@access answer=REP)', async ({ page }) => {
  await signIn(page, 'customer');
  const ticket = await api(page, '/api/crm/ticket', { subject: 'E2E inquiry', body: 'Where is my invoice?', category: 'BIL' });
  await page.context().clearCookies();                           // signed out: the next sign-in is the rep's

  await signIn(page, 'minjun');
  await expect(page.locator('.crm-sider')).toContainText('상담');
  await page.goto(`/ticket/show/${ticket.ticketId}`);
  await page.getByRole('button', { name: '답변 완료' }).click();
  await expect(page.locator('.crm-path-step.current')).toHaveText('답변 완료');
});

test('a masked phone left as it is keeps the stored number', async ({ page }) => {
  await signIn(page, 'minjun');
  const account = (await api(page, '/api/crm/account/search', { pageSize: 1 })).content[0];
  const phone = `010-${String(Date.now()).slice(-4)}-${String(Date.now()).slice(-8, -4)}`;
  const contact = await api(page, '/api/crm/contact', { name: 'E2E masked', phone, accountId: account.accountId });

  await page.goto(`/contact/show/${contact.contactId}`);
  await page.getByRole('button', { name: '편집' }).click();
  const input = page.locator('.ant-drawer').getByLabel('전화번호');
  await expect(input).toHaveValue(contact.phone);                 // the masked text, as answered
  await page.locator('.ant-drawer').getByRole('button', { name: '저장' }).click();
  await expect(page.getByText('저장했습니다.')).toBeVisible();

  const found = await api(page, '/api/crm/contact/search', { phone });
  expect(found.totalElements).toBe(1);                            // the stored number is still the real one
});

test('the form refuses what the DSL refuses before anything is sent (validateXxxCodes)', async ({ page }) => {
  await signIn(page, 'minjun');
  const deal = (await api(page, '/api/crm/opportunity/search', { pageSize: 1 })).content[0];
  const writes: string[] = [];
  page.on('request', (r) => { if (r.method() === 'PUT' && r.url().includes('/api/crm/opportunity')) writes.push(r.url()); });

  await page.goto(`/opportunity/show/${deal.opportunityId}`);
  await page.getByRole('button', { name: '편집' }).click();
  const drawer = page.locator('.ant-drawer');
  await drawer.getByLabel('성공 확률(%)').fill('150');            // @min(0) @max(100) in the DSL
  await drawer.getByRole('button', { name: '저장' }).click();
  await expect(drawer.getByText('성공 확률(%)은(는) 100 이하여야 합니다.')).toBeVisible();
  expect(writes).toHaveLength(0);
});

test('sign-up refuses a password the DSL pattern refuses, in the chosen language', async ({ page }) => {
  const posts: string[] = [];
  page.on('request', (r) => { if (r.method() === 'POST' && r.url().endsWith('/api/crm/member')) posts.push(r.url()); });
  await page.goto('/signup');
  await page.getByLabel('아이디').fill(`e2e${String(Date.now()).slice(-8)}`);
  await page.getByLabel('비밀번호').fill('lettersonly');          // @pattern: a letter and a digit
  await page.getByLabel('이름').fill('E2E sign-up');
  await page.getByLabel('이메일').fill('e2e@example.com');
  await page.locator('form button[type="submit"]').click();
  await expect(page.getByText('비밀번호의 형식이 맞지 않습니다.')).toBeVisible();
  expect(posts).toHaveLength(0);
});

test('a customer account sees its requests, not the sales records', async ({ page }) => {
  await signIn(page, 'customer');
  await expect(page).toHaveURL(/\/ticket/);
  await expect(page.locator('.crm-sider')).not.toContainText('영업 기회');
  await page.goto('/opportunity');
  await expect(page.getByText('이 화면을 볼 권한이 없습니다.')).toBeVisible();
});
