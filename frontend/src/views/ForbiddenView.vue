<script setup lang="ts">
import { NButton, NCard, NText } from 'naive-ui'
</script>

<template>
  <n-card title="403">
    <n-text>权限不足，无法访问该页面。</n-text>
    <div style="margin-top: 12px">
      <router-link to="/market">
        <n-button type="primary">
          返回市场
        </n-button>
      </router-link>
    </div>
  </n-card>
</template>
