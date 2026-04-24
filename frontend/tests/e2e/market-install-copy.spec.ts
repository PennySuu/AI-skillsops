import { expect, test } from '@playwright/test'

test('market to detail and copy install command', async ({ page }) => {
  await page.addInitScript(() => {
    Object.defineProperty(navigator, 'clipboard', {
      value: {
        writeText: () => Promise.resolve(),
      },
      configurable: true,
    })
  })

  await page.route('**/v1/auth/csrf-token', async (route) => {
    await route.fulfill({
      status: 200,
      headers: {
        'Content-Type': 'application/json',
        'Set-Cookie': 'XSRF-TOKEN=e2e-token; Path=/',
      },
      body: JSON.stringify({
        success: true,
        code: 'OK',
        message: 'success',
        data: 'e2e-token',
      }),
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
        data: {
          userId: 1101,
          username: 'market_e2e',
          role: 'USER',
          expiresInSeconds: 1800,
        },
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
          items: [
            {
              id: 501,
              name: 'Skill Install Demo',
              description: 'demo skill',
              avgRating: 4.7,
              ratingCount: 8,
            },
          ],
        },
      }),
    })
  })

  await page.route('**/v1/market/skills/501', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        success: true,
        code: 'OK',
        message: 'success',
        data: {
          id: 501,
          name: 'Skill Install Demo',
          description: 'detail text',
          avgRating: 4.7,
          ratingCount: 8,
          versions: [
            { version: '1.0.0', createdAt: '2026-04-20T09:00:00' },
            { version: '1.1.0', createdAt: '2026-04-24T10:00:00' },
          ],
        },
      }),
    })
  })

  await page.route('**/v1/skills/501/install-command', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        success: true,
        code: 'OK',
        message: 'success',
        data: {
          command: 'npx skills add https://skillsops.local/install/demo-token',
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
              skillId: 501,
              skillName: 'Skill Install Demo',
              installedAt: '2026-04-24T10:00:00',
              installedVersion: '1.0.0',
              latestVersion: '1.1.0',
              updateAvailable: true,
              offline: false,
            },
          ],
        },
      }),
    })
  })

  await page.goto('/market')
  await expect(page).toHaveURL(/\/login\?returnUrl=/)
  await page.getByPlaceholder('请输入用户名').fill('market_e2e')
  await page.locator('input[type="password"]').fill('Passw0rd!')
  await page.getByRole('button', { name: '登录' }).click()
  await expect(page).toHaveURL(/\/market$/)

  await page.getByRole('button', { name: '查看详情' }).first().click()
  await expect(page).toHaveURL(/\/skills\/501$/)

  await page.getByRole('button', { name: '生成命令' }).click()
  await expect(page.locator('input[value^="npx skills add"]')).toBeVisible()
  await page.getByRole('button', { name: '复制命令' }).click()
})
