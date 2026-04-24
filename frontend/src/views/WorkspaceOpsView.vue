<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { NAlert, NCard, NEmpty, NSelect, NSpin, NStatistic, NSpace } from 'naive-ui'
import VChart from 'vue-echarts'
import { CanvasRenderer } from 'echarts/renderers'
import { BarChart, LineChart } from 'echarts/charts'
import { GridComponent, LegendComponent, TooltipComponent } from 'echarts/components'
import { use } from 'echarts/core'
import { getOpsDashboard } from '@/api/modules/ops'
import type { OpsDashboardDTO, OpsGranularity } from '@/types/api'
import { buildInstallTrendChartOption, buildTopSkillsBarChartOption } from '@/utils/opsChartAdapters'

use([CanvasRenderer, LineChart, BarChart, GridComponent, TooltipComponent, LegendComponent])

const loading = ref(false)
const dashboard = ref<OpsDashboardDTO | null>(null)
const errorMessage = ref('')
const granularity = ref<OpsGranularity>('day')

const granularityOptions = [
  { label: '按日', value: 'day' },
  { label: '按周', value: 'week' },
  { label: '按月', value: 'month' },
]

async function loadDashboard(): Promise<void> {
  loading.value = true
  errorMessage.value = ''
  try {
    const days = granularity.value === 'month' ? 30 : 7
    dashboard.value = await getOpsDashboard({ granularity: granularity.value, days })
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '加载运营数据失败'
  } finally {
    loading.value = false
  }
}

const trendOption = computed(() =>
  buildInstallTrendChartOption(dashboard.value?.installTrend ?? []),
)

const topSkillOption = computed(() =>
  buildTopSkillsBarChartOption(dashboard.value?.topSkills ?? []),
)

onMounted(() => {
  void loadDashboard()
})
</script>

<template>
  <n-space
    vertical
    :size="16"
  >
    <n-card title="运营看板">
      <template #header-extra>
        <n-select
          v-model:value="granularity"
          :options="granularityOptions"
          style="width: 140px"
          @update:value="loadDashboard"
        />
      </template>
      <n-alert
        v-if="errorMessage"
        type="error"
      >
        {{ errorMessage }}
      </n-alert>
      <n-spin :show="loading">
        <n-space
          v-if="dashboard"
          :size="12"
          wrap
        >
          <n-card
            v-for="item in dashboard.metrics"
            :key="item.key"
            size="small"
            style="width: 220px"
          >
            <n-statistic
              :label="item.label"
              :value="item.value"
            />
          </n-card>
        </n-space>
        <n-empty
          v-else-if="!loading"
          description="暂无运营数据"
        />
      </n-spin>
    </n-card>

    <n-space
      :size="16"
      wrap
    >
      <n-card
        title="安装趋势"
        style="min-width: 480px; flex: 1"
      >
        <v-chart
          v-if="dashboard && dashboard.installTrend.length > 0"
          :option="trendOption"
          style="height: 280px"
          autoresize
        />
        <n-empty
          v-else
          description="暂无趋势数据"
        />
      </n-card>

      <n-card
        title="热门技能 TopN"
        style="min-width: 480px; flex: 1"
      >
        <v-chart
          v-if="dashboard && dashboard.topSkills.length > 0"
          :option="topSkillOption"
          style="height: 280px"
          autoresize
        />
        <n-empty
          v-else
          description="暂无 TopN 数据"
        />
      </n-card>
    </n-space>

    <n-card title="活跃作者">
      <n-empty
        v-if="!dashboard || dashboard.activeAuthors.length === 0"
        description="暂无活跃作者数据"
      />
      <n-space
        v-else
        vertical
      >
        <div
          v-for="item in dashboard.activeAuthors"
          :key="item.authorId"
          :class="$style.authorRow"
        >
          <span>{{ item.username }}</span>
          <span>已发布 {{ item.publishedCount }}</span>
        </div>
      </n-space>
    </n-card>
  </n-space>
</template>

<style module lang="scss">
.authorRow {
  display: flex;
  justify-content: space-between;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  padding: 8px 0;
}
</style>
