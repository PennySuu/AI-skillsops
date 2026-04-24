import { expect, test } from '@playwright/test'

test('admin opens ops dashboard with seeded metrics and charts', async ({ page }) => {
  const seedDashboard = {
    granularity: 'day',
    days: 7,
    metrics: [
      { key: 'publishedSkills', label: '已上架技能数', value: 3 },
      { key: 'installCount', label: '近窗口安装数', value: 12 },
      { key: 'ratingCount', label: '近窗口评分数', value: 4 },
    ],
    installTrend: [
      { bucket: '2026-04-22', installs: 2 },
      { bucket: '2026-04-23', installs: 5 },
    ],
    topSkills: [
      { skillId: 701, skillName: 'Seed Skill A', installCount: 8 },
      { skillId: 702, skillName: 'Seed Skill B', installCount: 4 },
    ],
    activeAuthors: [
      { authorId: 101, username: 'author_seed', publishedCount: 2 },
    ],
  }

  await page.route('**/v1/auth/csrf-token', async (route) => {
    await route.fulfill({
      status: 200,
      headers: {
        'Content-Type': 'application/json',
        'Set-Cookie': 'XSRF-TOKEN=e2e-ops-token; Path=/',
      },
      body: JSON.stringify({
        success: true,
        code: 'OK',
        message: 'success',
        data: 'e2e-ops-token',
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
          userId: 9001,
          username: 'admin_ops_e2e',
          role: 'ADMIN',
          expiresInSeconds: 1800,
        },
      }),
    })
  })

  await page.route('**/v1/admin/ops/dashboard**', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        success: true,
        code: 'OK',
        message: 'success',
        data: seedDashboard,
      }),
    })
  })

  await page.goto('/login?returnUrl=%2Fworkspace%2Fops')
  await page.getByPlaceholder('请输入用户名').fill('admin_ops_e2e')
  await page.locator('input[type="password"]').fill('Passw0rd!')
  await page.getByRole('button', { name: '登录' }).click()
  await expect(page).toHaveURL(/\/workspace\/ops$/)

  await expect(page.getByRole('heading', { name: '运营看板', exact: true })).toBeVisible()
  await expect(page.getByText('已上架技能数')).toBeVisible()
  await expect(page.locator('.n-statistic').filter({ hasText: '已上架技能数' })).toContainText('3')
  await expect(page.getByText('安装趋势')).toBeVisible()
  await expect(page.getByText('热门技能 TopN')).toBeVisible()
  const trendCard = page.locator('.n-card').filter({ hasText: '安装趋势' })
  const topCard = page.locator('.n-card').filter({ hasText: '热门技能 TopN' })
  await expect(trendCard.locator('canvas')).toBeVisible()
  await expect(topCard.locator('canvas')).toBeVisible()
  await expect(page.getByText('活跃作者')).toBeVisible()
  await expect(page.getByText('author_seed')).toBeVisible()
})
