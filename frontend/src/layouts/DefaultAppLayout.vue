<script setup lang="ts">
import { NConfigProvider, NLayout, NLayoutContent, NLayoutHeader, dateZhCN, zhCN } from 'naive-ui'
</script>

<template>
  <n-config-provider
    :locale="zhCN"
    :date-locale="dateZhCN"
  >
    <n-layout style="min-height: 100vh">
      <n-layout-header
        bordered
        :class="$style.shellHeader"
        style="height: 56px; padding: 0 24px; display: flex; align-items: center"
      >
        <strong style="color: var(--skillsops-color-primary)">SkillsOps</strong>
      </n-layout-header>
      <n-layout-content
        content-style="padding: 24px; max-width: 1200px; margin: 0 auto; width: 100%"
      >
        <router-view />
      </n-layout-content>
    </n-layout>
  </n-config-provider>
</template>

<style module lang="scss">
.shellHeader {
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
}
</style>
