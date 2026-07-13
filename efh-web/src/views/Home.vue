<template>
  <div class="home efh-page">
    <section class="workbench">
      <div>
        <span class="efh-kicker">EV FORKLIFT OPERATIONS</span>
        <h1>新能源叉车业务协同中台</h1>
        <p>把售后工单、备件商城、社区互动和 AI 助手固定成一条车企售后业务闭环。</p>
      </div>
      <div class="quick-actions">
        <el-button type="primary" @click="$router.push('/assistant')">
          <el-icon><ChatDotRound /></el-icon>
          AI 问答
        </el-button>
        <el-button @click="$router.push('/service')">创建工单</el-button>
        <el-button @click="$router.push('/parts')">备件采购</el-button>
      </div>
    </section>

    <section class="module-grid efh-section">
      <button v-for="item in businessModules" :key="item.path" type="button" @click="$router.push(item.path)">
        <el-icon><component :is="item.icon" /></el-icon>
        <strong>{{ item.title }}</strong>
        <span>{{ item.desc }}</span>
      </button>
    </section>

    <section class="app-download efh-section">
      <div>
        <span class="efh-kicker">MOBILE APP</span>
        <h2>移动端安装使用</h2>
        <p>Android 提供 APK 下载，iOS 提供添加桌面快捷方式指引，两端共用后台数据。</p>
      </div>
      <div class="download-actions">
        <a class="download-button primary" href="/downloads/ev-forklift-hub-debug.apk" download>
          <span>Android</span>
          <strong>下载安卓安装包</strong>
        </a>
        <el-button @click="$router.push('/mobile')">iOS 使用指引</el-button>
      </div>
    </section>

    <section class="topic-card efh-section" @click="goTopic">
      <div>
        <el-tag type="success">置顶专题</el-tag>
        <h2>如何选择合适的电动叉车</h2>
        <p>从载荷、工况、续航、电池类型、备件成本和售后响应六个维度建立选型判断。</p>
        <span>支持上传工况图片 / 文字留言互动</span>
      </div>
      <el-button type="primary">进入专题</el-button>
    </section>

    <el-card class="filter-card efh-section">
      <el-radio-group v-model="currentCategory" @change="handleCategoryChange">
        <el-radio-button :label="1">技术交流</el-radio-button>
        <el-radio-button
          v-for="item in categoryOptions"
          :key="item.value"
          :label="item.value"
        >
          {{ item.label }}
        </el-radio-button>
      </el-radio-group>
    </el-card>

    <el-card class="post-list efh-section" v-loading="loading">
      <div class="efh-post-item" v-for="post in postList" :key="post.id" @click="goToDetail(post.id)">
        <div class="post-header">
          <h3 class="efh-post-title">{{ post.title }}</h3>
          <el-tag :type="getCategory(post.category).type" size="small" effect="light">
            {{ getCategory(post.category).label }}
          </el-tag>
        </div>
        <div class="efh-post-content">{{ post.content }}</div>
        <div class="efh-post-meta">
          <div class="efh-post-stats">
            <span><el-icon><View /></el-icon> {{ post.viewCount || 0 }}</span>
            <span><el-icon><ChatDotRound /></el-icon> {{ post.commentCount || 0 }}</span>
            <span><el-icon><Star /></el-icon> {{ post.likeCount || 0 }}</span>
          </div>
          <div class="post-time">发布于 {{ formatDateTime(post.createTime) }}</div>
        </div>
      </div>

      <EfhEmptyState v-if="!loading && postList.length === 0" title="暂无帖子" description="当前分类还没有内容。" />
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

    <button type="button" class="assistant-fab" @click="$router.push('/assistant')">
      <el-icon><ChatDotRound /></el-icon>
      AI
    </button>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getPostList } from '@/api/post'
import { ElMessage } from 'element-plus'
import { View, ChatDotRound, Star, Tools, ShoppingBag, House, Reading } from '@element-plus/icons-vue'
import { formatDateTime, getPostCategory, postCategories } from '@/utils/format'
import EfhEmptyState from '@/components/EfhEmptyState.vue'

const router = useRouter()
const loading = ref(false)
const postList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const currentCategory = ref(1)
const topicPostId = ref(null)

const businessModules = [
  { title: '售后工单', desc: '报修、派工、服务闭环', path: '/service', icon: Tools },
  { title: '备件商城', desc: '下单、支付、物流', path: '/parts', icon: ShoppingBag },
  { title: '社区', desc: '经验交流与案例沉淀', path: '/', icon: House },
  { title: 'AI 助手', desc: '知识检索与工单协助', path: '/assistant', icon: ChatDotRound },
  { title: '知识库', desc: '维修案例与电池资料', path: '/knowledge', icon: Reading }
]

const categoryOptions = postCategories.filter(item => item.value === 0 || item.value >= 5 || [2, 3, 4].includes(item.value))
const getCategory = getPostCategory

const fetchPostList = async () => {
  loading.value = true
  try {
    const params = { page: currentPage.value, size: pageSize.value }
    if (currentCategory.value > 0) params.category = currentCategory.value
    const res = await getPostList(params)
    postList.value = res.data.records || []
    total.value = res.data.total || 0
    const topic = postList.value.find(post => post.title?.includes('如何选择合适的电动叉车'))
    if (topic) topicPostId.value = topic.id
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

const goToDetail = (id) => router.push(`/post/${id}`)

const goTopic = () => {
  if (topicPostId.value) {
    router.push(`/post/${topicPostId.value}`)
    return
  }
  router.push({ path: '/', query: { topic: 'forklift-select' } })
}

onMounted(fetchPostList)
</script>

<style scoped>
.workbench,
.app-download,
.topic-card {
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
.app-download h2,
.topic-card h2 {
  margin: 0;
  line-height: 1.2;
}

.workbench h1 {
  font-size: 30px;
}

.workbench p,
.app-download p,
.topic-card p {
  max-width: 680px;
  margin: 10px 0 0;
  color: var(--efh-text-secondary);
}

.quick-actions,
.download-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.module-grid {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 12px;
}

.module-grid button {
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-height: 120px;
  padding: 16px;
  border: 1px solid var(--efh-border-light);
  border-radius: 8px;
  background: #fff;
  text-align: left;
  cursor: pointer;
}

.module-grid .el-icon {
  color: var(--efh-primary);
  font-size: 24px;
}

.module-grid span,
.topic-card span {
  color: var(--efh-text-muted);
  font-size: 13px;
}

.download-button {
  min-width: 176px;
  padding: 10px 14px;
  border-radius: 8px;
  border: 1px solid var(--efh-border);
  background: #fff;
  color: var(--efh-text);
  text-decoration: none;
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

.topic-card {
  cursor: pointer;
  background: #fff7ed;
  border-color: #fed7aa;
}

.filter-card :deep(.el-card__body),
.post-list :deep(.el-card__body) {
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

.assistant-fab {
  position: fixed;
  right: 24px;
  bottom: 88px;
  z-index: 800;
  width: 56px;
  height: 56px;
  border: none;
  border-radius: 50%;
  background: var(--efh-primary);
  color: #fff;
  box-shadow: var(--efh-shadow-lg);
  cursor: pointer;
  font-weight: 700;
}

@media (max-width: 900px) {
  .module-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .workbench,
  .app-download,
  .topic-card {
    flex-direction: column;
  }

  .workbench h1 {
    font-size: 23px;
  }

  .download-actions,
  .download-button,
  .quick-actions .el-button {
    width: 100%;
  }

  .post-header {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
