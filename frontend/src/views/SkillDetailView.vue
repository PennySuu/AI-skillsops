<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { NAlert, NButton, NCard, NEmpty, NResult, NSpace, NText, NTimeline, NTimelineItem } from 'naive-ui'
import { getMarketSkillDetail, getMyInstalls } from '@/api/modules/market'
import SkillInstallPanel from '@/components/market/SkillInstallPanel.vue'
import RatingEditor from '@/components/market/RatingEditor.vue'
import type { SkillDetailDTO } from '@/types/api'

const route = useRoute()
const loading = ref(false)
const errorMessage = ref('')
const detail = ref<SkillDetailDTO | null>(null)
const installed = ref(false)
const optimisticScore = ref<number | null>(null)

const offline = computed(() => route.query.offline === '1')

async function loadDetail(): Promise<void> {
  const skillId = Number(route.params.skillId)
  if (!Number.isFinite(skillId) || skillId <= 0) {
    errorMessage.value = '无效的技能 ID'
    return
  }
  loading.value = true
  errorMessage.value = ''
  try {
    detail.value = await getMarketSkillDetail(skillId)
    optimisticScore.value = null
    const installs = await getMyInstalls({ page: 0, size: 100 })
    installed.value = installs.items.some((item) => item.skillId === skillId)
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '详情加载失败'
  } finally {
    loading.value = false
  }
}

function handleOptimistic(score: number): void {
  optimisticScore.value = score
}

function handleRollback(): void {
  optimisticScore.value = null
}

function handleSubmitted(): void {
  void loadDetail()
}

const avgRatingText = computed(() => {
  if (!detail.value) {
    return '0.0'
  }
  if (optimisticScore.value == null) {
    return detail.value.avgRating.toFixed(1)
  }
  return optimisticScore.value.toFixed(1)
})

onMounted(() => {
  void loadDetail()
})
</script>

<template>
  <section :class="$style.page">
    <n-space
      vertical
      :size="16"
    >
      <n-alert
        v-if="offline"
        type="warning"
      >
        该技能已下架，仅可查看历史信息，不可安装。
      </n-alert>

      <n-result
        v-if="errorMessage"
        status="error"
        title="加载失败"
        :description="errorMessage"
      >
        <template #footer>
          <n-button
            type="primary"
            @click="loadDetail"
          >
            重试
          </n-button>
        </template>
      </n-result>

      <n-card
        v-else-if="detail"
        :title="detail.name"
      >
        <n-space
          vertical
          :size="16"
        >
          <n-text>{{ detail.description || '暂无描述' }}</n-text>
          <n-text depth="3">
            综合评分 {{ avgRatingText }}（{{ detail.ratingCount }} 人）
          </n-text>
          <SkillInstallPanel
            :skill-id="detail.id"
            :offline="offline"
          />
          <RatingEditor
            :skill-id="detail.id"
            :installed="installed"
            @optimistic="handleOptimistic"
            @rollback="handleRollback"
            @submitted="handleSubmitted"
          />
          <n-text depth="3">
            版本时间线
          </n-text>
          <n-timeline>
            <n-timeline-item
              v-for="version in detail.versions"
              :key="`${version.version}-${version.createdAt}`"
              :content="`v${version.version}`"
              :time="version.createdAt"
            />
          </n-timeline>
        </n-space>
      </n-card>

      <n-empty
        v-else-if="!loading"
        description="暂无详情数据"
      />
    </n-space>
  </section>
</template>

<style module lang="scss">
.page {
  margin-top: 16px;
}
</style>
