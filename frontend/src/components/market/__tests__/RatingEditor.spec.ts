/* eslint-disable vue/one-component-per-file */
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import naive, { NMessageProvider } from 'naive-ui'
import { defineComponent, h } from 'vue'
import RatingEditor from '@/components/market/RatingEditor.vue'

const upsertSkillRatingMock = vi.fn()

vi.mock('@/api/modules/rating', () => ({
  upsertSkillRating: (...args: unknown[]) => upsertSkillRatingMock(...args),
}))

describe('RatingEditor', () => {
  beforeEach(() => {
    upsertSkillRatingMock.mockReset()
  })

  it('shows install warning and disables submit when not installed', async () => {
    const wrapper = mount(
      defineComponent({
        render: () => h(NMessageProvider, null, { default: () => h(RatingEditor, { skillId: 1, installed: false }) }),
      }),
      { global: { plugins: [naive] } },
    )
    expect(wrapper.text()).toContain('仅安装后可评分')
    const submitButton = wrapper.findAll('button').find((item) => item.text().includes('提交评分'))
    expect(submitButton).toBeTruthy()
    expect(submitButton!.attributes('disabled')).toBeDefined()
  })

  it('rolls back when submit fails', async () => {
    upsertSkillRatingMock.mockRejectedValue(new Error('failed'))
    const wrapper = mount(
      defineComponent({
        render: () => h(NMessageProvider, null, { default: () => h(RatingEditor, { skillId: 2, installed: true }) }),
      }),
      { global: { plugins: [naive] } },
    )

    const submitButton = wrapper.findAll('button').find((item) => item.text().includes('提交评分'))
    expect(submitButton).toBeTruthy()
    await submitButton!.trigger('click')

    await vi.waitFor(() => {
      expect(upsertSkillRatingMock).toHaveBeenCalledTimes(1)
    })
    const rollbackEvents = wrapper.findComponent(RatingEditor).emitted('rollback')
    expect(rollbackEvents).toBeTruthy()
  })
})
