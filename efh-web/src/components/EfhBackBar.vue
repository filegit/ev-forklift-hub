<template>
  <div class="efh-back-bar">
    <el-button plain @click="goBack">
      <el-icon><ArrowLeft /></el-icon>
      {{ label }}
    </el-button>
    <div v-if="title || subtitle" class="back-title">
      <strong v-if="title">{{ title }}</strong>
      <span v-if="subtitle">{{ subtitle }}</span>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'

const props = defineProps({
  label: { type: String, default: '返回' },
  title: { type: String, default: '' },
  subtitle: { type: String, default: '' },
  fallback: { type: String, default: '/' }
})

const router = useRouter()
const goBack = () => {
  if (window.history.length > 1) {
    router.back()
    return
  }
  router.push(props.fallback)
}
</script>

<style scoped>
.efh-back-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.back-title {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.back-title strong {
  font-size: 18px;
  color: var(--efh-text);
}

.back-title span {
  font-size: 13px;
  color: var(--efh-text-muted);
}
</style>
