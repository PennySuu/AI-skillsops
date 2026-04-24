import { expect, test } from '@playwright/test'

test('installed user rates skill and sees avg updated', async ({ page }) => {
  let avgRating = 4.2
  let ratingCount = 2

  await page.route('**/v1/auth/csrf-token', async (route) => {
    await route.fulfill({
      status: 200,
      headers: {
        'Content-Type': 'application/json',
        'Set-Cookie': 'XSRF-TOKEN=e2e-token; Path=/',
      },
      body: JSON.stringify({ success: true, code: 'OK', message: 'success', data: 'e2e-token' }),
    })
  })

  await page.route('**/v1/auth/login', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        success: true,
        code: 'OK',
        message: 'success',
        data: { userId: 2201, username: 'rating_e2e', role: 'USER', expiresInSeconds: 1800 },
      }),
    })
  })

  await page.route('**/v1/market/skills?*', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        success: true,
        code: 'OK',
        message: 'success',
        data: {
          page: 0,
          size: 8,
          total: 1,
          items: [{ id: 701, name: 'Rating Demo Skill', description: 'demo', avgRating, ratingCount }],
        },
      }),
    })
  })

  await page.route('**/v1/market/skills/701', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        success: true,
        code: 'OK',
        message: 'success',
        data: {
          id: 701,
          name: 'Rating Demo Skill',
          description: 'detail',
          avgRating,
          ratingCount,
          versions: [{ version: '1.0.0', createdAt: '2026-04-24T10:00:00' }],
        },
      }),
    })
  })

  await page.route('**/v1/users/me/installs?*', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        success: true,
        code: 'OK',
        message: 'success',
        data: {
          page: 0,
          size: 20,
          total: 1,
          items: [
            {
              skillId: 701,
              skillName: 'Rating Demo Skill',
              installedAt: '2026-04-24T10:00:00',
              installedVersion: '1.0.0',
              latestVersion: '1.0.0',
              updateAvailable: false,
              offline: false,
            },
          ],
        },
      }),
    })
  })

  await page.route('**/v1/skills/701/ratings', async (route) => {
    avgRating = 5.0
    ratingCount = 3
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({ success: true, code: 'OK', message: 'success', data: null }),
    })
  })

  await page.goto('/market')
  await expect(page).toHaveURL(/\/login\?returnUrl=/)
  await page.getByPlaceholder('请输入用户名').fill('rating_e2e')
  await page.locator('input[type="password"]').fill('Passw0rd!')
  await page.getByRole('button', { name: '登录' }).click()
  await page.getByRole('button', { name: '查看详情' }).first().click()
  await expect(page).toHaveURL(/\/skills\/701$/)

  await page.getByRole('button', { name: '提交评分' }).click()
  await expect(page.getByText('综合评分 5.0（3 人）')).toBeVisible()
})
