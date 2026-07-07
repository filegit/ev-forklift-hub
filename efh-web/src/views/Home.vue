<template>
  <div class="home efh-page">
    <section class="workbench">
      <div>
        <span class="efh-kicker">EV FORKLIFT OPERATIONS</span>
        <h1>新能源叉车业务协同中台</h1>
        <p>把运营社区、备件采购、维保工单、知识库和 AI 助手串成一条车企售后业务链路。</p>
      </div>
      <div class="quick-actions">
        <el-button type="primary" @click="$router.push('/parts')">备件采购</el-button>
        <el-button @click="$router.push('/service')">创建维保工单</el-button>
      </div>
    </section>

    <section class="app-download efh-section">
      <div>
        <span class="efh-kicker">MOBILE APP</span>
        <h2>手机端安装使用</h2>
        <p>安卓可直接下载安装包；苹果手机可打开 iOS 入口并添加到主屏幕，联网使用同一套系统。</p>
      </div>
      <div class="download-actions">
        <a class="download-button primary" href="/downloads/ev-forklift-hub-debug.apk" download>
          <span>Android</span>
          <strong>下载安卓安装包</strong>
        </a>
        <a class="download-button" href="/ios.html" target="_blank" rel="noopener">
          <span>iPhone</span>
          <strong>打开苹果使用入口</strong>
        </a>
      </div>
    </section>

    <div class="efh-metric-grid efh-section">
      <div class="efh-metric">
        <label>业务闭环</label>
        <strong>下单-支付-物流</strong>
      </div>
      <div class="efh-metric">
        <label>核心场景</label>
        <strong>车企售后</strong>
      </div>
      <div class="efh-metric">
        <label>AI 能力</label>
        <strong>故障问答</strong>
      </div>
      <div class="efh-metric">
        <label>知识沉淀</label>
        <strong>维修案例</strong>
      </div>
    </div>

    <el-card class="filter-card efh-section">
      <el-radio-group v-model="currentCategory" @change="handleCategoryChange">
        <el-radio-button :label="0">全部</el-radio-button>
        <el-radio-button :label="1">技术交流</el-radio-button>
        <el-radio-button :label="2">故障求助</el-radio-button>
        <el-radio-button :label="3">经验分享</el-radio-button>
        <el-radio-button :label="4">其他</el-radio-button>
      </el-radio-group>
    </el-card>

    <el-card class="post-list efh-section" v-loading="loading">
      <div class="efh-post-item" v-for="post in postList" :key="post.id" @click="goToDetail(post.id)">
        <div class="post-header">
          <h3 class="efh-post-title">{{ post.title }}</h3>
          <el-tag :type="getCategoryType(post.category)" size="small" effect="light">
            {{ getCategoryName(post.category) }}
          </el-tag>
        </div>
        <div class="efh-post-content">{{ post.content }}</div>
        <div class="efh-post-meta">
          <div class="efh-post-stats">
            <span><el-icon><View /></el-icon> {{ post.viewCount }}</span>
            <span><el-icon><ChatDotRound /></el-icon> {{ post.commentCount }}</span>
            <span><el-icon><Star /></el-icon> {{ post.likeCount }}</span>
          </div>
          <div class="post-time">{{ formatTime(post.createTime) }}</div>
        </div>
      </div>

      <el-empty v-if="!loading && postList.length === 0" description="暂无帖子" />
    </el-card>

    <div class="efh-pagination">
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
import { View, ChatDotRound, Star } from '@element-plus/icons-vue'

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
    postList.value = res.data.records || []
    total.value = res.data.total || 0
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
  max-width: var(--efh-max-width);
}

.workbench,
.app-download {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 20px;
  padding: 24px;
  margin-bottom: 16px;
  border-radius: 8px;
  border: 1px solid var(--efh-border-light);
  background: #fff;
}

.workbench {
  background: linear-gradient(135deg, #ffffff 0%, #edf8f5 100%);
}

.workbench h1,
.app-download h2 {
  margin: 0;
  line-height: 1.2;
}

.workbench h1 {
  font-size: 30px;
}

.app-download h2 {
  font-size: 22px;
}

.workbench p,
.app-download p {
  max-width: 680px;
  margin: 10px 0 0;
  color: var(--efh-text-secondary);
}

.quick-actions,
.download-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.download-actions {
  justify-content: flex-end;
}

.download-button {
  min-width: 176px;
  padding: 12px 14px;
  border-radius: 8px;
  border: 1px solid var(--efh-border);
  background: #fff;
  color: var(--efh-text);
  text-decoration: none;
  transition: border-color 0.2s, transform 0.2s;
}

.download-button.primary {
  background: var(--efh-primary);
  border-color: var(--efh-primary);
  color: #fff;
}

.download-button span {
  display: block;
  font-size: 12px;
  opacity: 0.78;
}

.download-button strong {
  display: block;
  margin-top: 2px;
  font-size: 15px;
}

@media (hover: hover) {
  .download-button:hover {
    transform: translateY(-1px);
    border-color: var(--efh-primary);
  }
}

.filter-card :deep(.el-card__body) {
  padding: 14px 16px;
}

.post-list :deep(.el-card__body) {
  padding: 0;
}

.post-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 10px;
  margin-bottom: 4px;
}

.post-time {
  font-size: 12px;
  white-space: nowrap;
}

@media (max-width: 768px) {
  .workbench,
  .app-download {
    flex-direction: column;
  }

  .workbench h1 {
    font-size: 23px;
  }

  .app-download h2 {
    font-size: 20px;
  }

  .download-actions,
  .download-button {
    width: 100%;
  }

  .post-header {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
