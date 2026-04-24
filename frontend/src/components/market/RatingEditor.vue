<script setup lang="ts">
import { ref } from 'vue'
import { NAlert, NButton, NCard, NInput, NRate, NSpace, NText, useMessage } from 'naive-ui'
import { upsertSkillRating } from '@/api/modules/rating'

const props = defineProps<{
  skillId: number
  installed: boolean
}>()

const emit = defineEmits<{
  optimistic: [nextScore: number]
  rollback: [previousScore: number]
  submitted: [score: number]
}>()

const message = useMessage()
const loading = ref(false)
const score = ref<number>(5)
const comment = ref('')

async function handleSubmit(): Promise<void> {
  if (!props.installed) {
    message.warning('请先安装后再评分')
    return
  }
  const previousScore = score.value
  const previousComment = comment.value
  loading.value = true
  try {
    emit('optimistic', score.value)
    await upsertSkillRating(props.skillId, {
      score: score.value,
      comment: comment.value || undefined,
    })
    emit('submitted', score.value)
    message.success('评分已提交')
  } catch (error: unknown) {
    emit('rollback', previousScore)
    score.value = previousScore
    comment.value = previousComment
    const code =
      typeof error === 'object' &&
      error !== null &&
      'response' in error &&
      typeof (error as { response?: { data?: { code?: string } } }).response?.data?.code === 'string'
        ? (error as { response?: { data?: { code?: string } } }).response?.data?.code
        : undefined
    if (code === 'RATING_REQUIRES_INSTALL') {
      message.error('请先安装后再评分')
      return
    }
    const text = error instanceof Error ? error.message : '评分失败，请稍后重试'
    message.error(text)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <n-card title="评分">
    <n-alert
      v-if="!installed"
      type="warning"
      :show-icon="false"
      style="margin-bottom: 12px"
    >
      仅安装后可评分，请先在本地安装该技能。
    </n-alert>
    <n-space vertical>
      <n-text depth="3">
        你的评分（1-5）
      </n-text>
      <n-rate v-model:value="score" />
      <n-input
        v-model:value="comment"
        type="textarea"
        placeholder="可选：写下使用反馈"
      />
      <n-button
        type="primary"
        :disabled="!installed"
        :loading="loading"
        @click="handleSubmit"
      >
        提交评分
      </n-button>
    </n-space>
  </n-card>
</template>
