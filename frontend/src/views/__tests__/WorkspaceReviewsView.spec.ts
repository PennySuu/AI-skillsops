import { beforeEach, describe, expect, it, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia } from 'pinia'
import naive, { NMessageProvider } from 'naive-ui'
import { defineComponent, h } from 'vue'
import WorkspaceReviewsView from '@/views/WorkspaceReviewsView.vue'

const getPendingReviews = vi.fn()
const approveReview = vi.fn()
const rejectReview = vi.fn()

vi.mock('@/api/modules/reviews', () => ({
  getPendingReviews: (...args: unknown[]) => getPendingReviews(...args),
  approveReview: (...args: unknown[]) => approveReview(...args),
  rejectReview: (...args: unknown[]) => rejectReview(...args),
}))

describe('WorkspaceReviewsView', () => {
  beforeEach(() => {
    getPendingReviews.mockResolvedValue([
      {
        reviewId: 11,
        skillId: 22,
        submittedBy: 3,
        createdAt: new Date().toISOString(),
      },
    ])
    approveReview.mockResolvedValue(undefined)
    rejectReview.mockResolvedValue(undefined)
  })

  it('shows validation feedback when reject reason is too short', async () => {
    const wrapper = mount(
      defineComponent({
        render: () => h(NMessageProvider, null, { default: () => h(WorkspaceReviewsView) }),
      }),
      {
      global: {
        plugins: [createPinia(), naive],
          stubs: {
            teleport: true,
            NDataTable: true,
          },
      },
      },
    )

    await vi.waitFor(() => {
      expect(wrapper.text()).toContain('审核单 #11')
    })

    await wrapper.find('[data-test-open-reject="11"]').trigger('click')
    await vi.waitFor(() => {
      expect(wrapper.find('[data-test-reject-input="11"] textarea').exists()).toBe(true)
    })
    await wrapper.find('[data-test-reject-input="11"] textarea').setValue('too short')
    await wrapper.find('[data-test-confirm-reject="11"]').trigger('click')

    expect(wrapper.text()).toContain('拒绝理由需为 10-200 字')
    expect(rejectReview).not.toHaveBeenCalled()
  })
})
