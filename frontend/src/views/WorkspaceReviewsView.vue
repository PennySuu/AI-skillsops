<script setup lang="ts">
import { onMounted, ref } from 'vue'
import {
  NButton,
  NCard,
  NDataTable,
  NForm,
  NFormItem,
  NInput,
  NModal,
  NSpace,
  NText,
  useMessage,
} from 'naive-ui'
import { approveReview, getPendingReviews, rejectReview, type PendingReviewItem } from '@/api/modules/reviews'

const message = useMessage()
const loading = ref(false)
const rows = ref<PendingReviewItem[]>([])
const rejectModalVisible = ref(false)
const rejectTarget = ref<PendingReviewItem | null>(null)
const rejectReason = ref('')
const rejectError = ref('')

const columns = [
  { title: '审核单 ID', key: 'reviewId' },
  { title: 'Skill ID', key: 'skillId' },
  { title: '提交人', key: 'submittedBy' },
  {
    title: '提交时间',
    key: 'createdAt',
    render: (row: PendingReviewItem) => new Date(row.createdAt).toLocaleString(),
  },
]

async function loadPending(): Promise<void> {
  loading.value = true
  try {
    rows.value = await getPendingReviews()
  } finally {
    loading.value = false
  }
}

async function handleApprove(row: PendingReviewItem): Promise<void> {
  await approveReview(row.reviewId)
  rows.value = rows.value.filter((item) => item.reviewId !== row.reviewId)
  message.success(`审核单 ${row.reviewId} 已通过`)
}

function openRejectModal(row: PendingReviewItem): void {
  rejectTarget.value = row
  rejectReason.value = ''
  rejectError.value = ''
  rejectModalVisible.value = true
}

async function handleRejectConfirm(): Promise<void> {
  if (!rejectTarget.value) {
    return
  }
  const reason = rejectReason.value.trim()
  if (reason.length < 10 || reason.length > 200) {
    rejectError.value = '拒绝理由需为 10-200 字'
    return
  }
  await rejectReview(rejectTarget.value.reviewId, reason)
  rows.value = rows.value.filter((item) => item.reviewId !== rejectTarget.value?.reviewId)
  rejectModalVisible.value = false
  message.success(`审核单 ${rejectTarget.value.reviewId} 已拒绝`)
}

onMounted(() => {
  void loadPending()
})
</script>

<template>
  <n-card title="待审核队列（管理员）">
    <template #header-extra>
      <n-button
        tertiary
        @click="loadPending"
      >
        刷新
      </n-button>
    </template>

    <n-data-table
      :loading="loading"
      :columns="columns"
      :data="rows"
      :pagination="{ pageSize: 10 }"
    >
      <template #empty>
        <n-text depth="3">
          暂无待审核记录。
        </n-text>
      </template>
    </n-data-table>

    <div
      v-if="rows.length > 0"
      :class="$style.actionList"
    >
      <n-card
        v-for="row in rows"
        :key="row.reviewId"
        size="small"
      >
        <n-space justify="space-between">
          <n-text>审核单 #{{ row.reviewId }} · Skill #{{ row.skillId }}</n-text>
          <n-space>
            <n-button
              size="small"
              type="primary"
              @click="handleApprove(row)"
            >
              通过
            </n-button>
            <n-button
              size="small"
              type="error"
              secondary
              :data-test-open-reject="row.reviewId"
              @click="openRejectModal(row)"
            >
              拒绝
            </n-button>
          </n-space>
        </n-space>
      </n-card>
    </div>
  </n-card>

  <n-modal
    v-model:show="rejectModalVisible"
    preset="card"
    title="填写拒绝理由"
    style="max-width: 520px"
  >
    <n-form>
      <n-form-item
        label="拒绝理由"
        :validation-status="rejectError ? 'error' : undefined"
        :feedback="rejectError"
      >
        <n-input
          v-model:value="rejectReason"
          type="textarea"
          :maxlength="200"
          show-count
          :data-test-reject-input="rejectTarget?.reviewId ?? 0"
        />
      </n-form-item>
    </n-form>
    <template #footer>
      <div :class="$style.footer">
        <n-button @click="rejectModalVisible = false">
          取消
        </n-button>
        <n-button
          type="error"
          :data-test-confirm-reject="rejectTarget?.reviewId ?? 0"
          @click="handleRejectConfirm"
        >
          确认拒绝
        </n-button>
      </div>
    </template>
  </n-modal>
</template>

<style module lang="scss">
.actionList {
  margin-top: 16px;
  display: grid;
  gap: 8px;
}

.footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>
