<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import {
  NButton,
  NCard,
  NDataTable,
  NForm,
  NFormItem,
  NInput,
  NModal,
  NPopconfirm,
  NSwitch,
  useMessage,
} from 'naive-ui'
import { createCategory, getCategories, patchCategoryStatus, updateCategory } from '@/api/modules/categories'
import type { CategoryItemDTO } from '@/types/api'

const message = useMessage()
const loading = ref(false)
const rows = ref<CategoryItemDTO[]>([])
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

const createVisible = ref(false)
const editVisible = ref(false)
const editing = ref<CategoryItemDTO | null>(null)
const createForm = ref({ name: '', enabled: true })
const editForm = ref({ name: '' })

const hasRows = computed(() => rows.value.length > 0)

async function loadRows(): Promise<void> {
  loading.value = true
  try {
    const result = await getCategories({ page: page.value - 1, size: pageSize.value })
    rows.value = result.items
    total.value = result.total
  } catch (error) {
    const text = error instanceof Error ? error.message : '加载分类失败'
    message.error(text)
  } finally {
    loading.value = false
  }
}

function openCreateModal(): void {
  createForm.value = { name: '', enabled: true }
  createVisible.value = true
}

async function handleCreate(): Promise<void> {
  await createCategory(createForm.value)
  createVisible.value = false
  message.success('分类已创建')
  await loadRows()
}

function openEditModal(row: CategoryItemDTO): void {
  editing.value = row
  editForm.value = { name: row.name }
  editVisible.value = true
}

async function handleEdit(): Promise<void> {
  if (!editing.value) {
    return
  }
  await updateCategory(editing.value.id, editForm.value)
  editVisible.value = false
  message.success('分类名称已更新')
  await loadRows()
}

async function handlePatchStatus(row: CategoryItemDTO, enabled: boolean): Promise<void> {
  await patchCategoryStatus(row.id, { enabled })
  row.enabled = enabled
  message.success(enabled ? '分类已启用' : '分类已停用')
}

const columns = [
  { title: 'ID', key: 'id' },
  { title: '名称', key: 'name' },
  {
    title: '状态',
    key: 'enabled',
    render: (row: CategoryItemDTO) => (row.enabled ? '启用' : '停用'),
  },
]

onMounted(() => {
  void loadRows()
})

function handlePageChange(value: number): void {
  page.value = value
  void loadRows()
}
</script>

<template>
  <n-card title="分类管理">
    <template #header-extra>
      <n-button
        type="primary"
        @click="openCreateModal"
      >
        新建分类
      </n-button>
    </template>
    <n-data-table
      :loading="loading"
      :columns="columns"
      :data="rows"
      :pagination="{ page: page, pageSize: pageSize, itemCount: total, onUpdatePage: handlePageChange }"
    />
    <div
      v-if="hasRows"
      :class="$style.cardList"
    >
      <n-card
        v-for="row in rows"
        :key="row.id"
        size="small"
        :title="row.name"
      >
        <div :class="$style.actions">
          <n-button
            size="small"
            @click="openEditModal(row)"
          >
            编辑
          </n-button>
          <n-popconfirm @positive-click="() => handlePatchStatus(row, !row.enabled)">
            <template #trigger>
              <n-button
                size="small"
                tertiary
              >
                {{ row.enabled ? '停用' : '启用' }}
              </n-button>
            </template>
            确认{{ row.enabled ? '停用' : '启用' }}该分类？
          </n-popconfirm>
          <n-switch
            :value="row.enabled"
            @update:value="(value: boolean) => handlePatchStatus(row, value)"
          />
        </div>
      </n-card>
    </div>
  </n-card>

  <n-modal
    v-model:show="createVisible"
    preset="card"
    title="新建分类"
    style="max-width: 420px"
  >
    <n-form>
      <n-form-item label="分类名称">
        <n-input v-model:value="createForm.name" />
      </n-form-item>
      <n-form-item label="默认启用">
        <n-switch v-model:value="createForm.enabled" />
      </n-form-item>
    </n-form>
    <template #footer>
      <div :class="$style.modalFooter">
        <n-button @click="createVisible = false">
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
    v-model:show="editVisible"
    preset="card"
    title="编辑分类"
    style="max-width: 420px"
  >
    <n-form>
      <n-form-item label="分类名称">
        <n-input v-model:value="editForm.name" />
      </n-form-item>
    </n-form>
    <template #footer>
      <div :class="$style.modalFooter">
        <n-button @click="editVisible = false">
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
</template>

<style module lang="scss">
.cardList {
  margin-top: 12px;
  display: grid;
  gap: 10px;
}

.actions {
  display: flex;
  gap: 8px;
  align-items: center;
}

.modalFooter {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>
