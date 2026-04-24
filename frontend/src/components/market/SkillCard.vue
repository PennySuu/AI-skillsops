<script setup lang="ts">
import { NButton, NCard, NSpace, NTag, NText } from 'naive-ui'
import type { SkillSummaryDTO } from '@/types/api'

defineProps<{
  item: SkillSummaryDTO
}>()

const emit = defineEmits<{
  openDetail: [skillId: number]
}>()

function handleOpenDetail(skillId: number): void {
  emit('openDetail', skillId)
}
</script>

<template>
  <n-card
    :title="item.name"
    :class="$style.card"
  >
    <n-text depth="3">
      {{ item.description || '暂无描述' }}
    </n-text>
    <n-space
      align="center"
      justify="space-between"
      :class="$style.footer"
    >
      <n-space align="center">
        <n-tag
          type="warning"
          size="small"
        >
          评分 {{ item.avgRating.toFixed(1) }}
        </n-tag>
        <n-text depth="3">
          {{ item.ratingCount }} 条评价
        </n-text>
      </n-space>
      <n-button
        size="small"
        tertiary
        type="primary"
        @click="handleOpenDetail(item.id)"
      >
        查看详情
      </n-button>
    </n-space>
  </n-card>
</template>

<style module lang="scss">
.card {
  min-height: 180px;
}

.footer {
  margin-top: 16px;
}
</style>
