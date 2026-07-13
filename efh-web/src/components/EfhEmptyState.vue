<template>
  <div class="efh-empty-state">
    <el-icon><Document /></el-icon>
    <strong>{{ title }}</strong>
    <p>{{ description }}</p>
    <slot />
  </div>
</template>

<script setup>
import { Document } from '@element-plus/icons-vue'

defineProps({
  title: { type: String, default: '暂无数据' },
  description: { type: String, default: '当前没有可展示的内容。' }
})
</script>

<style scoped>
.efh-empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 180px;
  padding: 28px 16px;
  color: var(--efh-text-muted);
  text-align: center;
}

.efh-empty-state .el-icon {
  width: 54px;
  height: 54px;
  margin-bottom: 10px;
  border-radius: 8px;
  background: var(--efh-primary-soft);
  color: var(--efh-primary);
}

.efh-empty-state strong {
  color: var(--efh-text);
}

.efh-empty-state p {
  margin: 6px 0 14px;
}
</style>
