import { beforeEach, describe, expect, it, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia } from 'pinia'
import naive, { NMessageProvider } from 'naive-ui'
import { defineComponent, h } from 'vue'
import WorkspacePublishedView from '@/views/WorkspacePublishedView.vue'

vi.mock('@/api/modules/workspace', () => ({
  createSkill: vi.fn(),
  updateSkill: vi.fn(),
  submitSkillReview: vi.fn(),
  createSkillVersion: vi.fn(),
}))

describe('WorkspacePublishedView', () => {
  beforeEach(() => {
    window.localStorage.clear()
  })

  it('disables edit action when skill is pending', async () => {
    window.localStorage.setItem(
      'skillsops.workspace.published',
      JSON.stringify([
        {
          id: 1001,
          name: 'Pending Skill',
          description: 'desc',
          resourceUrl: 'https://example.com/a',
          categoryId: 1,
          status: 'pending',
          currentVersion: '1.0.0',
          updatedAt: new Date().toISOString(),
        },
      ]),
    )

    const wrapper = mount(
      defineComponent({
        render: () => h(NMessageProvider, null, { default: () => h(WorkspacePublishedView) }),
      }),
      {
      global: {
        plugins: [createPinia(), naive],
        stubs: {
          NDataTable: true,
        },
      },
      },
    )
    await vi.waitFor(() => {
      expect(wrapper.find('[data-test-edit="1001"]').attributes('disabled')).toBeDefined()
      expect(wrapper.find('[data-test-pending-tip="1001"]').text()).toContain('审核中不可编辑')
    })
  })
})
