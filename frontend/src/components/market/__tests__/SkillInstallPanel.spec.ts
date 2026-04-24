import { describe, expect, it, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import naive, { NMessageProvider } from 'naive-ui'
import { defineComponent, h } from 'vue'
import SkillInstallPanel from '@/components/market/SkillInstallPanel.vue'

const issueInstallCommandMock = vi.fn()

vi.mock('@/api/modules/market', () => ({
  issueInstallCommand: (...args: unknown[]) => issueInstallCommandMock(...args),
}))

describe('SkillInstallPanel', () => {
  beforeEach(() => {
    issueInstallCommandMock.mockReset()
    vi.stubGlobal('navigator', {
      clipboard: {
        writeText: vi.fn().mockResolvedValue(undefined),
      },
    })
  })

  it('generates and copies install command', async () => {
    issueInstallCommandMock.mockResolvedValue({ command: 'npx skills add https://skillsops.local/install/a1' })
    const wrapper = mount(
      defineComponent({
        render: () => h(NMessageProvider, null, { default: () => h(SkillInstallPanel, { skillId: 101 }) }),
      }),
      { global: { plugins: [naive] } },
    )

    const generateButton = wrapper.findAll('button').find((item) => item.text().includes('生成命令'))
    expect(generateButton).toBeTruthy()
    await generateButton!.trigger('click')
    await vi.waitFor(() => {
      expect(issueInstallCommandMock).toHaveBeenCalledTimes(1)
    })

    expect(wrapper.html()).toContain('npx skills add')

    const copyButton = wrapper.findAll('button').find((item) => item.text().includes('复制命令'))
    expect(copyButton).toBeTruthy()
    await copyButton!.trigger('click')
    await vi.waitFor(() => {
      expect(navigator.clipboard.writeText).toHaveBeenCalledTimes(1)
    })
  })
})
