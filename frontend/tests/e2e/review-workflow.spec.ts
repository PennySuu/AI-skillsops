import { expect, test } from '@playwright/test'

test('author submits review, admin approves and rejects', async ({ page }) => {
  const pendingReviews: Array<{
    reviewId: number
    skillId: number
    submittedBy: number
    createdAt: string
  }> = []
  let reviewSeq = 1

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
    const payload = route.request().postDataJSON() as { username: string }
    const isAdmin = payload.username.includes('admin')
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        success: true,
        code: 'OK',
        message: 'success',
        data: {
          userId: isAdmin ? 9001 : 1001,
          username: payload.username,
          role: isAdmin ? 'ADMIN' : 'USER',
          expiresInSeconds: 1800,
        },
      }),
    })
  })

  await page.route('**/v1/auth/logout', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({ success: true, code: 'OK', message: 'success', data: null }),
    })
  })

  await page.route('**/v1/skills', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({ success: true, code: 'OK', message: 'success', data: null }),
    })
  })

  await page.route('**/v1/skills/*/submit-review', async (route) => {
    const skillId = Number(route.request().url().match(/\/v1\/skills\/(\d+)\/submit-review/)?.[1] ?? 0)
    pendingReviews.push({
      reviewId: reviewSeq++,
      skillId,
      submittedBy: 1001,
      createdAt: new Date().toISOString(),
    })
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({ success: true, code: 'OK', message: 'success', data: null }),
    })
  })

  await page.route('**/v1/reviews/pending', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        success: true,
        code: 'OK',
        message: 'success',
        data: pendingReviews,
      }),
    })
  })

  await page.route('**/v1/reviews/*/approve', async (route) => {
    const reviewId = Number(route.request().url().match(/\/v1\/reviews\/(\d+)\/approve/)?.[1] ?? 0)
    const index = pendingReviews.findIndex((item) => item.reviewId === reviewId)
    if (index >= 0) {
      pendingReviews.splice(index, 1)
    }
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({ success: true, code: 'OK', message: 'success', data: null }),
    })
  })

  await page.route('**/v1/reviews/*/reject', async (route) => {
    const reviewId = Number(route.request().url().match(/\/v1\/reviews\/(\d+)\/reject/)?.[1] ?? 0)
    const index = pendingReviews.findIndex((item) => item.reviewId === reviewId)
    if (index >= 0) {
      pendingReviews.splice(index, 1)
    }
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({ success: true, code: 'OK', message: 'success', data: null }),
    })
  })

  await page.goto('/workspace/published')
  await expect(page).toHaveURL(/\/login\?returnUrl=/)
  await page.getByPlaceholder('请输入用户名').fill('author_e2e')
  await page.locator('input[type="password"]').fill('Passw0rd!')
  await page.getByRole('button', { name: '登录' }).click()
  await expect(page).toHaveURL(/\/workspace\/published$/)

  await page.getByRole('button', { name: '新建 Skill' }).click()
  const createModalA = page.locator('.n-modal').last()
  await createModalA.locator('input').nth(0).fill('Skill-A')
  await createModalA.locator('textarea').nth(0).fill('desc A')
  await createModalA.locator('input').nth(1).fill('https://example.com/a')
  await page.getByRole('button', { name: '创建' }).click()

  await page.locator('button:has-text("提交审核"):not([disabled])').first().click()
  await expect.poll(() => pendingReviews.length).toBe(1)
  await expect(page.getByText('审核中不可编辑，请等待审核结果。')).toBeVisible()

  await page.getByRole('button', { name: '新建 Skill' }).click()
  const createModalB = page.locator('.n-modal').last()
  await createModalB.locator('input').nth(0).fill('Skill-B')
  await createModalB.locator('textarea').nth(0).fill('desc B')
  await createModalB.locator('input').nth(1).fill('https://example.com/b')
  await page.getByRole('button', { name: '创建' }).click()
  await page.locator('button:has-text("提交审核"):not([disabled])').first().click()
  await expect.poll(() => pendingReviews.length).toBe(2)

  await page.getByRole('button', { name: '退出登录' }).click()
  await page.goto('/login?returnUrl=%2Fworkspace%2Freviews')
  await page.getByPlaceholder('请输入用户名').fill('admin_e2e')
  await page.locator('input[type="password"]').fill('Passw0rd!')
  await page.getByRole('button', { name: '登录' }).click()
  await expect(page).toHaveURL(/\/workspace\/reviews$/)

  await page.getByRole('button', { name: '刷新' }).click()
  await expect(page.locator('[data-test-open-reject="2"]')).toBeVisible()

  await page.getByRole('button', { name: '通过' }).first().click()
  await page.locator('[data-test-open-reject="2"]').click()
  await page.locator('[data-test-reject-input="2"] textarea').fill('不符合规范，请补充说明后重提')
  await page.locator('[data-test-confirm-reject="2"]').click()

  await page.getByRole('button', { name: '刷新' }).click()
  await expect(page.getByText('暂无待审核记录。')).toBeVisible()
})
