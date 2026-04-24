<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  NAlert,
  NButton,
  NEmpty,
  NGrid,
  NGridItem,
  NInput,
  NPagination,
  NSelect,
  NSkeleton,
  NSpace,
  NSpin,
} from 'naive-ui'
import { getMarketSkills } from '@/api/modules/market'
import SkillCard from '@/components/market/SkillCard.vue'
import type { SkillSummaryDTO } from '@/types/api'

const router = useRouter()
const loading = ref(false)
const errorMessage = ref('')
const items = ref<SkillSummaryDTO[]>([])
const total = ref(0)

const query = reactive({
  page: 1,
  size: 8,
  category: '',
  q: '',
  sort: 'updatedAt,desc',
})

const hasData = computed(() => items.value.length > 0)
const sortOptions = [
  { label: '最近更新', value: 'updatedAt,desc' },
  { label: '名称升序', value: 'name,asc' },
  { label: '名称降序', value: 'name,desc' },
]

async function loadMarketList(): Promise<void> {
  loading.value = true
  errorMessage.value = ''
  try {
    const response = await getMarketSkills({
      page: query.page - 1,
      size: query.size,
      category: query.category || undefined,
      q: query.q || undefined,
      sort: query.sort,
    })
    items.value = response.items
    total.value = response.total
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '加载失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

function handleSearch(): void {
  query.page = 1
  void loadMarketList()
}

function handleOpenDetail(skillId: number): void {
  void router.push(`/skills/${skillId}`)
}

function handlePageChange(page: number): void {
  query.page = page
  void loadMarketList()
}

onMounted(() => {
  void loadMarketList()
})
</script>

<template>
  <section :class="$style.page">
    <n-space
      vertical
      :size="16"
    >
      <n-space>
        <n-input
          v-model:value="query.q"
          placeholder="搜索技能名或描述"
          clearable
          style="width: 280px"
          @keyup.enter="handleSearch"
        />
        <n-input
          v-model:value="query.category"
          placeholder="分类 ID（可选）"
          clearable
          style="width: 160px"
          @keyup.enter="handleSearch"
        />
        <n-select
          v-model:value="query.sort"
          :options="sortOptions"
          style="width: 180px"
          @update:value="handleSearch"
        />
        <n-button
          type="primary"
          @click="handleSearch"
        >
          查询
        </n-button>
      </n-space>

      <n-alert
        v-if="errorMessage"
        type="error"
      >
        {{ errorMessage }}
      </n-alert>
      <n-button
        v-if="errorMessage"
        tertiary
        type="primary"
        @click="loadMarketList"
      >
        重试
      </n-button>

      <n-spin :show="loading">
        <n-grid
          v-if="hasData"
          cols="1 s:2"
          :x-gap="16"
          :y-gap="16"
          responsive="screen"
        >
          <n-grid-item
            v-for="item in items"
            :key="item.id"
          >
            <SkillCard
              :item="item"
              @open-detail="handleOpenDetail"
            />
          </n-grid-item>
        </n-grid>

        <n-space
          v-else-if="loading"
          vertical
          :size="12"
        >
          <n-skeleton
            v-for="index in 4"
            :key="index"
            text
            :repeat="3"
          />
        </n-space>

        <n-empty
          v-else
          description="暂无可展示技能"
        />
      </n-spin>

      <n-pagination
        :page="query.page"
        :page-size="query.size"
        :item-count="total"
        @update:page="handlePageChange"
      />
    </n-space>
  </section>
</template>

<style module lang="scss">
.page {
  margin-top: 16px;
}
</style>
