import { describe, expect, it } from 'vitest'
import { buildInstallTrendChartOption, buildTopSkillsBarChartOption } from '@/utils/opsChartAdapters'

describe('opsChartAdapters', () => {
  it('maps install trend points to line chart axes and series', () => {
    const option = buildInstallTrendChartOption([
      { bucket: '2026-04-20', installs: 2 },
      { bucket: '2026-04-21', installs: 5 },
    ])
    expect(option.xAxis.data).toEqual(['2026-04-20', '2026-04-21'])
    expect(option.series[0].data).toEqual([2, 5])
    expect(option.series[0].type).toBe('line')
    expect(option.series[0].name).toBe('安装量')
  })

  it('returns empty chart data when trend list is empty', () => {
    const option = buildInstallTrendChartOption([])
    expect(option.xAxis.data).toEqual([])
    expect(option.series[0].data).toEqual([])
  })

  it('maps top skills to bar chart categories and values', () => {
    const option = buildTopSkillsBarChartOption([
      { skillId: 1, skillName: 'Alpha', installCount: 10 },
      { skillId: 2, skillName: 'Beta', installCount: 3 },
    ])
    expect(option.xAxis.data).toEqual(['Alpha', 'Beta'])
    expect(option.series[0].data).toEqual([10, 3])
    expect(option.series[0].type).toBe('bar')
  })

  it('returns empty bar chart when top list is empty', () => {
    const option = buildTopSkillsBarChartOption([])
    expect(option.xAxis.data).toEqual([])
    expect(option.series[0].data).toEqual([])
  })
})
