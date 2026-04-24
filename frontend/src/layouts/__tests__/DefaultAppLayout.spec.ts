import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import DefaultAppLayout from '../DefaultAppLayout.vue'

describe('DefaultAppLayout', () => {
  it('should display brand when mounted', () => {
    const wrapper = mount(DefaultAppLayout, {
      global: {
        stubs: {
          RouterView: { template: '<div />' },
        },
      },
    })
    expect(wrapper.text()).toContain('SkillsOps')
  })
})
