<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { NAlert, NButton, NCard, NEmpty, NResult, NSpace, NText, NTimeline, NTimelineItem } from 'naive-ui'
import { getMarketSkillDetail } from '@/api/modules/market'
import type { SkillDetailDTO } from '@/types/api'

const route = useRoute()
const loading = ref(false)
const errorMessage = ref('')
const detail = ref<SkillDetailDTO | null>(null)

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
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '详情加载失败'
  } finally {
    loading.value = false
  }
}

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
        <n-space vertical>
          <n-text>{{ detail.description || '暂无描述' }}</n-text>
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
