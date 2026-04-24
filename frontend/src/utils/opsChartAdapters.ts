import type { OpsTopSkillDTO, OpsTrendPointDTO } from '@/types/api'

/** 将安装趋势点转为 ECharts 折线图 option（纯数据适配，不含渲染副作用） */
export function buildInstallTrendChartOption(points: OpsTrendPointDTO[]) {
  return {
    tooltip: { trigger: 'axis' as const },
    xAxis: { type: 'category' as const, data: points.map((item) => item.bucket) },
    yAxis: { type: 'value' as const },
    series: [
      {
        type: 'line' as const,
        data: points.map((item) => item.installs),
        smooth: true,
        name: '安装量',
      },
    ],
  }
}

/** 将 Top 技能列表转为 ECharts 柱状图 option */
export function buildTopSkillsBarChartOption(rows: OpsTopSkillDTO[]) {
  return {
    tooltip: { trigger: 'axis' as const },
    xAxis: { type: 'category' as const, data: rows.map((item) => item.skillName) },
    yAxis: { type: 'value' as const },
    series: [
      {
        type: 'bar' as const,
        data: rows.map((item) => item.installCount),
        name: '安装次数',
      },
    ],
  }
}
