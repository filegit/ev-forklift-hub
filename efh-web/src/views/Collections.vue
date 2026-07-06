<template>
  <div class="collections-page">
    <el-card class="collections-card">
      <h2>我的收藏</h2>
      
      <div class="collection-list">
        <div class="collection-item" v-for="item in collections" :key="item.id">
          <div class="item-header">
            <h3 @click="goToPost(item.postId)">{{ item.postTitle }}</h3>
            <el-button text type="danger" @click="removeCollection(item.id)">取消收藏</el-button>
          </div>
          <p class="item-content">{{ item.postContent }}</p>
          <div class="item-footer">
            <span>收藏于: {{ formatTime(item.createTime) }}</span>
          </div>
        </div>
        
        <el-empty v-if="collections.length === 0" description="暂无收藏" />
      </div>
      
      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="fetchCollections"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getMyCollections, collectPost } from '@/api/collection'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const collections = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const fetchCollections = async () => {
  try {
    const res = await getMyCollections({ page: currentPage.value, size: pageSize.value })
    collections.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (error) {
    ElMessage.error('获取收藏列表失败')
  }
}

const removeCollection = async (id) => {
  try {
    const postId = collections.value.find(c => c.id === id).postId
    await collectPost(postId)
    ElMessage.success('已取消收藏')
    fetchCollections()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const goToPost = (postId) => {
  router.push(`/post/${postId}`)
}

const formatTime = (time) => {
  return new Date(time).toLocaleString('zh-CN')
}

onMounted(() => {
  fetchCollections()
})
</script>

<style scoped>
.collections-page {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px;
}

.collections-card {
  margin-bottom: 20px;
}

.collections-card h2 {
  margin-top: 0;
  color: #303133;
}

.collection-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
  margin: 20px 0;
}

.collection-item {
  padding: 15px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  transition: all 0.3s;
}

.collection-item:hover {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.item-header h3 {
  margin: 0;
  color: #303133;
  cursor: pointer;
  flex: 1;
}

.item-header h3:hover {
  color: #409eff;
}

.item-content {
  margin: 10px 0;
  color: #606266;
  line-height: 1.6;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.item-footer {
  font-size: 12px;
  color: #909399;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>
