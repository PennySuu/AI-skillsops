import { test, expect } from '@playwright/test'

test('register then redirect to market', async ({ page }) => {
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

  await page.route('**/v1/auth/register', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        success: true,
        code: 'OK',
        message: 'success',
        data: {
          userId: 100,
          username: 'e2e_user',
          role: 'USER',
          expiresInSeconds: 1800,
        },
      }),
    })
  })

  await page.goto('/register')
  await page.getByPlaceholder('3-32 位字母数字或下划线').fill('e2e_user')
  await page.locator('input[type="password"]').first().fill('Passw0rd!')
  await page.locator('input[type="password"]').nth(1).fill('Passw0rd!')
  await page.getByRole('button', { name: '注册' }).click()

  await expect(page).toHaveURL(/\/market$/)
  await expect(page.getByText('前端工程骨架已就绪')).toBeVisible()
})
