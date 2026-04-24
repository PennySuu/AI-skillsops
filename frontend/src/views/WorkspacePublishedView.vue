<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import {
  NButton,
  NCard,
  NDataTable,
  NForm,
  NFormItem,
  NInput,
  NInputNumber,
  NModal,
  NTag,
  NText,
  useMessage,
} from 'naive-ui'
import {
  createSkill,
  createSkillVersion,
  submitSkillReview,
  updateSkill,
  type WorkspaceSkillItem,
} from '@/api/modules/workspace'

type RowData = WorkspaceSkillItem

const message = useMessage()
const rows = ref<RowData[]>([])

const createModalVisible = ref(false)
const editModalVisible = ref(false)
const versionModalVisible = ref(false)
const editingSkill = ref<RowData | null>(null)

const createForm = ref({
  name: '',
  description: '',
  resourceUrl: '',
  categoryId: 1,
})

const editForm = ref({
  name: '',
  description: '',
  resourceUrl: '',
})

const versionForm = ref({
  version: '',
  changelog: '',
  resourceUrl: '',
})

const hasRows = computed(() => rows.value.length > 0)

const statusTypeMap: Record<RowData['status'], 'default' | 'warning' | 'success' | 'error'> = {
  draft: 'default',
  pending: 'warning',
  published: 'success',
  offline: 'error',
}

const statusLabelMap: Record<RowData['status'], string> = {
  draft: '草稿',
  pending: '审核中',
  published: '已上架',
  offline: '已下架',
}

function openCreateModal(): void {
  createForm.value = {
    name: '',
    description: '',
    resourceUrl: '',
    categoryId: 1,
  }
  createModalVisible.value = true
}

async function handleCreate(): Promise<void> {
  await createSkill(createForm.value)
  rows.value.unshift({
    id: Date.now(),
    name: createForm.value.name,
    description: createForm.value.description,
    resourceUrl: createForm.value.resourceUrl,
    categoryId: createForm.value.categoryId,
    status: 'draft',
    currentVersion: '0.0.0',
    updatedAt: new Date().toISOString(),
  })
  createModalVisible.value = false
  message.success('草稿已创建')
}

function openEditModal(row: RowData): void {
  editingSkill.value = row
  editForm.value = {
    name: row.name,
    description: row.description,
    resourceUrl: row.resourceUrl,
  }
  editModalVisible.value = true
}

async function handleEdit(): Promise<void> {
  if (!editingSkill.value) {
    return
  }
  await updateSkill(editingSkill.value.id, editForm.value)
  const target = rows.value.find((item) => item.id === editingSkill.value?.id)
  if (target) {
    target.name = editForm.value.name
    target.description = editForm.value.description
    target.resourceUrl = editForm.value.resourceUrl
    target.updatedAt = new Date().toISOString()
  }
  editModalVisible.value = false
  message.success('已保存修改')
}

async function handleSubmitReview(row: RowData): Promise<void> {
  await submitSkillReview(row.id)
  row.status = 'pending'
  row.updatedAt = new Date().toISOString()
  row.rejectReason = undefined
  message.success('已提交审核')
}

function openVersionModal(row: RowData): void {
  editingSkill.value = row
  versionForm.value = {
    version: '',
    changelog: '',
    resourceUrl: row.resourceUrl,
  }
  versionModalVisible.value = true
}

async function handleCreateVersion(): Promise<void> {
  if (!editingSkill.value) {
    return
  }
  await createSkillVersion(editingSkill.value.id, versionForm.value)
  const target = rows.value.find((item) => item.id === editingSkill.value?.id)
  if (target) {
    target.currentVersion = versionForm.value.version
    target.updatedAt = new Date().toISOString()
  }
  versionModalVisible.value = false
  message.success('版本发布成功')
}

const columns = [
  {
    title: '名称',
    key: 'name',
  },
  {
    title: '状态',
    key: 'status',
    render: (row: RowData) => statusLabelMap[row.status],
  },
  {
    title: '当前版本',
    key: 'currentVersion',
  },
  {
    title: '更新时间',
    key: 'updatedAt',
    render: (row: RowData) => new Date(row.updatedAt).toLocaleString(),
  },
]

onMounted(() => {
  const raw = window.localStorage.getItem('skillsops.workspace.published')
  if (!raw) {
    return
  }
  try {
    const parsed = JSON.parse(raw) as RowData[]
    rows.value = parsed
  } catch {
    rows.value = []
  }
})

watch(
  rows,
  (value) => {
    window.localStorage.setItem('skillsops.workspace.published', JSON.stringify(value))
  },
  { deep: true },
)
</script>

<template>
  <n-card title="我的发布">
    <template #header-extra>
      <n-button
        type="primary"
        @click="openCreateModal"
      >
        新建 Skill
      </n-button>
    </template>

    <n-text depth="3">
      当前版本先展示你在本地工作台创建/编辑过的 Skill；后续将对接「我的发布列表」后端查询接口。
    </n-text>

    <n-data-table
      style="margin-top: 16px"
      :columns="columns"
      :data="rows"
      :pagination="{ pageSize: 8 }"
    >
      <template #empty>
        <n-text depth="3">
          暂无发布记录，点击右上角“新建 Skill”。
        </n-text>
      </template>
    </n-data-table>

    <div
      v-if="hasRows"
      :class="$style.cards"
    >
      <n-card
        v-for="row in rows"
        :key="row.id"
        size="small"
        :title="row.name"
      >
        <template #header-extra>
          <n-tag :type="statusTypeMap[row.status]">
            {{ statusLabelMap[row.status] }}
          </n-tag>
        </template>
        <n-text depth="3">
          {{ row.description }}
        </n-text>
        <div :class="$style.metaRow">
          <n-text depth="3">
            版本：{{ row.currentVersion }}
          </n-text>
          <n-text depth="3">
            更新时间：{{ new Date(row.updatedAt).toLocaleString() }}
          </n-text>
        </div>
        <n-text
          v-if="row.rejectReason"
          type="error"
        >
          审核拒绝原因：{{ row.rejectReason }}
        </n-text>
        <div :class="$style.actionRow">
          <n-button
            size="small"
            :disabled="row.status === 'pending'"
            :data-test-edit="row.id"
            @click="openEditModal(row)"
          >
            编辑
          </n-button>
          <n-button
            size="small"
            type="primary"
            secondary
            :disabled="row.status === 'pending'"
            @click="handleSubmitReview(row)"
          >
            提交审核
          </n-button>
          <n-button
            size="small"
            tertiary
            @click="openVersionModal(row)"
          >
            发布新版本
          </n-button>
        </div>
        <n-text
          v-if="row.status === 'pending'"
          type="warning"
          :data-test-pending-tip="row.id"
        >
          审核中不可编辑，请等待审核结果。
        </n-text>
      </n-card>
    </div>
  </n-card>

  <n-modal
    v-model:show="createModalVisible"
    preset="card"
    title="新建 Skill 草稿"
    style="max-width: 560px"
  >
    <n-form>
      <n-form-item label="名称">
        <n-input v-model:value="createForm.name" />
      </n-form-item>
      <n-form-item label="描述">
        <n-input
          v-model:value="createForm.description"
          type="textarea"
        />
      </n-form-item>
      <n-form-item label="资源地址">
        <n-input v-model:value="createForm.resourceUrl" />
      </n-form-item>
      <n-form-item label="分类 ID">
        <n-input-number
          v-model:value="createForm.categoryId"
          :min="1"
        />
      </n-form-item>
    </n-form>
    <template #footer>
      <div :class="$style.modalFooter">
        <n-button @click="createModalVisible = false">
          取消
        </n-button>
        <n-button
          type="primary"
          @click="handleCreate"
        >
          创建
        </n-button>
      </div>
    </template>
  </n-modal>

  <n-modal
    v-model:show="editModalVisible"
    preset="card"
    title="编辑 Skill"
    style="max-width: 560px"
  >
    <n-form>
      <n-form-item label="名称">
        <n-input v-model:value="editForm.name" />
      </n-form-item>
      <n-form-item label="描述">
        <n-input
          v-model:value="editForm.description"
          type="textarea"
        />
      </n-form-item>
      <n-form-item label="资源地址">
        <n-input v-model:value="editForm.resourceUrl" />
      </n-form-item>
    </n-form>
    <template #footer>
      <div :class="$style.modalFooter">
        <n-button @click="editModalVisible = false">
          取消
        </n-button>
        <n-button
          type="primary"
          @click="handleEdit"
        >
          保存
        </n-button>
      </div>
    </template>
  </n-modal>

  <n-modal
    v-model:show="versionModalVisible"
    preset="card"
    title="发布新版本"
    style="max-width: 560px"
  >
    <n-form>
      <n-form-item label="版本号">
        <n-input v-model:value="versionForm.version" />
      </n-form-item>
      <n-form-item label="更新说明">
        <n-input
          v-model:value="versionForm.changelog"
          type="textarea"
        />
      </n-form-item>
      <n-form-item label="资源地址">
        <n-input v-model:value="versionForm.resourceUrl" />
      </n-form-item>
    </n-form>
    <template #footer>
      <div :class="$style.modalFooter">
        <n-button @click="versionModalVisible = false">
          取消
        </n-button>
        <n-button
          type="primary"
          @click="handleCreateVersion"
        >
          发布
        </n-button>
      </div>
    </template>
  </n-modal>
</template>

<style module lang="scss">
.cards {
  margin-top: 16px;
  display: grid;
  gap: 12px;
}

.metaRow {
  margin: 8px 0;
  display: flex;
  justify-content: space-between;
  gap: 16px;
}

.actionRow {
  margin: 12px 0;
  display: flex;
  gap: 8px;
}

.modalFooter {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>
