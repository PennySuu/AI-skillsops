import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia } from 'pinia'
import { createRouter, createWebHistory } from 'vue-router'
import DefaultAppLayout from '../DefaultAppLayout.vue'

describe('DefaultAppLayout', () => {
  it('should display brand when mounted', () => {
    const router = createRouter({
      history: createWebHistory(),
      routes: [{ path: '/', component: { template: '<div />' } }],
    })

    const wrapper = mount(DefaultAppLayout, {
      global: {
        plugins: [createPinia(), router],
      },
    })
    expect(wrapper.text()).toContain('SkillsOps')
  })
})
