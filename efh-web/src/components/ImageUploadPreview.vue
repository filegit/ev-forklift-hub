<template>
  <div class="image-upload-preview">
    <el-upload
      action="#"
      :auto-upload="false"
      :show-file-list="false"
      accept="image/*"
      :on-change="handleChange"
    >
      <el-button>
        <el-icon><Picture /></el-icon>
        {{ buttonText }}
      </el-button>
    </el-upload>
    <div v-if="previews.length" class="preview-grid">
      <div v-for="item in previews" :key="item.url" class="preview-item">
        <img :src="item.url" :alt="item.name" />
        <button type="button" @click="remove(item.url)">×</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onBeforeUnmount } from 'vue'
import { Picture } from '@element-plus/icons-vue'

defineProps({
  buttonText: { type: String, default: '上传图片' }
})

const emit = defineEmits(['change'])
const previews = ref([])

const sync = () => emit('change', previews.value.map(item => ({ name: item.name, url: item.url })))

const handleChange = (uploadFile) => {
  const file = uploadFile.raw
  if (!file) return
  const url = URL.createObjectURL(file)
  previews.value.push({ name: file.name, url })
  sync()
}

const remove = (url) => {
  URL.revokeObjectURL(url)
  previews.value = previews.value.filter(item => item.url !== url)
  sync()
}

onBeforeUnmount(() => {
  previews.value.forEach(item => URL.revokeObjectURL(item.url))
})
</script>

<style scoped>
.image-upload-preview {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.preview-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(88px, 1fr));
  gap: 8px;
}

.preview-item {
  position: relative;
  aspect-ratio: 1;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid var(--efh-border-light);
  background: #f8fafc;
}

.preview-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.preview-item button {
  position: absolute;
  top: 4px;
  right: 4px;
  width: 22px;
  height: 22px;
  border: none;
  border-radius: 50%;
  color: #fff;
  background: rgba(15, 23, 42, 0.65);
  cursor: pointer;
}
</style>
