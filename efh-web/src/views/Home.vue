<template>
  <div class="home">
    <!-- 分类筛选 -->
    <el-card class="filter-card">
      <el-radio-group v-model="currentCategory" @change="handleCategoryChange">
        <el-radio-button :label="0">全部</el-radio-button>
        <el-radio-button :label="1">技术交流</el-radio-button>
        <el-radio-button :label="2">故障求助</el-radio-button>
        <el-radio-button :label="3">经验分享</el-radio-button>
        <el-radio-button :label="4">其他</el-radio-button>
      </el-radio-group>
    </el-card>
    
    <!-- 帖子列表 -->
    <el-card class="post-list" v-loading="loading">
      <div class="post-item" v-for="post in postList" :key="post.id" @click="goToDetail(post.id)">
        <div class="post-header">
          <h3 class="post-title">{{ post.title }}</h3>
          <el-tag :type="getCategoryType(post.category)">
            {{ getCategoryName(post.category) }}
          </el-tag>
        </div>
        <div class="post-content">{{ post.content }}</div>
        <div class="post-footer">
          <div class="post-stats">
            <span><el-icon><View /></el-icon> {{ post.viewCount }}</span>
            <span><el-icon><ChatDotRound /></el-icon> {{ post.commentCount }}</span>
            <span><el-icon><Star /></el-icon> {{ post.likeCount }}</span>
          </div>
          <div class="post-time">{{ formatTime(post.createTime) }}</div>
        </div>
      </div>
      
      <el-empty v-if="!loading && postList.length === 0" description="暂无帖子" />
    </el-card>
    
    <!-- 分页 -->
    <div class="pagination">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="fetchPostList"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getPostList } from '@/api/post'
import { ElMessage } from 'element-plus'

const router = useRouter()
const loading = ref(false)
const postList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const currentCategory = ref(0)

const fetchPostList = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value
    }
    if (currentCategory.value > 0) {
      params.category = currentCategory.value
    }
    
    const res = await getPostList(params)
    postList.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    ElMessage.error('获取帖子列表失败')
  } finally {
    loading.value = false
  }
}

const handleCategoryChange = () => {
  currentPage.value = 1
  fetchPostList()
}

const goToDetail = (id) => {
  router.push(`/post/${id}`)
}

const getCategoryName = (category) => {
  const map = { 1: '技术交流', 2: '故障求助', 3: '经验分享', 4: '其他' }
  return map[category] || '未知'
}

const getCategoryType = (category) => {
  const map = { 1: 'primary', 2: 'warning', 3: 'success', 4: 'info' }
  return map[category] || 'info'
}

const formatTime = (time) => {
  return new Date(time).toLocaleString('zh-CN')
}

onMounted(() => {
  fetchPostList()
})
</script>

<style scoped>
.home {
  max-width: 900px;
  margin: 0 auto;
}

.filter-card {
  margin-bottom: 20px;
}

.post-list {
  min-height: 400px;
}

.post-item {
  padding: 20px;
  border-bottom: 1px solid #ebeef5;
  cursor: pointer;
  transition: background 0.3s;
}

.post-item:hover {
  background: #f5f7fa;
}

.post-item:last-child {
  border-bottom: none;
}

.post-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.post-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.post-content {
  color: #606266;
  line-height: 1.6;
  margin-bottom: 15px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.post-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #909399;
  font-size: 14px;
}

.post-stats {
  display: flex;
  gap: 20px;
}

.post-stats span {
  display: flex;
  align-items: center;
  gap: 5px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>
