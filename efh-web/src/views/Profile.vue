<template>
  <div class="profile-page efh-page">
    <el-card class="profile-card">
      <!-- 个人信息 -->
      <div class="profile-header">
        <el-avatar :size="80">{{ userInfo?.nickname?.charAt(0) }}</el-avatar>
        <div class="profile-info">
          <h2>{{ userInfo?.nickname }}</h2>
          <p>用户名: {{ userInfo?.username }}</p>
          <p>手机号: {{ userInfo?.phone }}</p>
        </div>
        <el-button type="primary" @click="showEditDialog = true">编辑信息</el-button>
      </div>

      <!-- 统计信息 -->
      <el-divider />
      
      <div class="stats">
        <div class="stat-item">
          <div class="stat-value">{{ stats.postCount }}</div>
          <div class="stat-label">发布帖子</div>
        </div>
        <div class="stat-item">
          <div class="stat-value">{{ stats.commentCount }}</div>
          <div class="stat-label">发表评论</div>
        </div>
        <div class="stat-item">
          <div class="stat-value">{{ userPoints?.availablePoints || 0 }}</div>
          <div class="stat-label">可用积分</div>
        </div>
        <div class="stat-item">
          <div class="stat-value">{{ userPoints?.totalPoints || 0 }}</div>
          <div class="stat-label">总积分</div>
        </div>
      </div>

      <!-- 标签页 -->
      <el-divider />
      
      <el-tabs v-model="activeTab">
        <!-- 我的帖子 -->
        <el-tab-pane label="我的帖子" name="posts">
          <div class="post-list">
            <div class="post-item" v-for="post in myPosts" :key="post.id" @click="goToPost(post.id)">
              <h4>{{ post.title }}</h4>
              <p>{{ post.content }}</p>
              <div class="post-meta">
                <span><el-icon><View /></el-icon> {{ post.viewCount }}</span>
                <span><el-icon><ChatDotRound /></el-icon> {{ post.commentCount }}</span>
                <span><el-icon><Pointer /></el-icon> {{ post.likeCount }}</span>
              </div>
            </div>
            <el-empty v-if="myPosts.length === 0" description="暂无帖子" />
          </div>
        </el-tab-pane>

        <!-- 我的评论 -->
        <el-tab-pane label="我的评论" name="comments">
          <div class="comment-list">
            <div class="comment-item" v-for="comment in myComments" :key="comment.id">
              <p>{{ comment.content }}</p>
              <div class="comment-meta">
                <span>{{ formatTime(comment.createTime) }}</span>
                <el-button text type="danger" @click="deleteCommentById(comment.id)">删除</el-button>
              </div>
            </div>
            <el-empty v-if="myComments.length === 0" description="暂无评论" />
          </div>
        </el-tab-pane>

        <!-- 我的收藏 -->
        <el-tab-pane label="我的收藏" name="collections">
          <div class="collection-list">
            <div class="collection-item" v-for="item in myCollections" :key="item.id" @click="goToPost(item.postId)">
              <h4>{{ item.postTitle }}</h4>
              <p>{{ item.postContent }}</p>
              <div class="collection-meta">
                <span>收藏于: {{ formatTime(item.createTime) }}</span>
              </div>
            </div>
            <el-empty v-if="myCollections.length === 0" description="暂无收藏" />
          </div>
        </el-tab-pane>

        <!-- 购买积分 -->
        <el-tab-pane label="购买积分" name="purchase">
          <div class="exchange-section">
            <div class="exchange-info">
              <p>当前可用积分: <span class="points">{{ userPoints?.availablePoints || 0 }}</span></p>
              <p class="hint">模拟支付，购买后积分立即到账</p>
            </div>
            <div class="exchange-items">
              <div class="exchange-item" v-for="pkg in purchasePackages" :key="pkg.id">
                <h4>{{ pkg.name }}</h4>
                <p>支付 {{ pkg.money }} 获得 {{ pkg.points }} 积分</p>
                <div class="exchange-footer">
                  <span class="points-cost">{{ pkg.money }}</span>
                  <el-button type="success" size="small" @click="handlePurchase(pkg.id)">购买</el-button>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <!-- 积分兑换 -->
        <el-tab-pane label="积分兑换" name="exchange">
          <div class="exchange-section">
            <div class="exchange-info">
              <p>可用积分: <span class="points">{{ userPoints?.availablePoints || 0 }}</span></p>
            </div>
            <div class="exchange-items">
              <div class="exchange-item" v-for="item in exchangeItems" :key="item.id">
                <h4>{{ item.name }}</h4>
                <p>{{ item.description }}</p>
                <div class="exchange-footer">
                  <span class="points-cost">{{ item.points }} 积分</span>
                  <el-button 
                    type="primary" 
                    size="small"
                    @click="handleExchange(item.id)"
                    :disabled="!canExchange(item.points)"
                  >
                    兑换
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <!-- 兑换记录 -->
        <el-tab-pane label="兑换记录" name="exchanges">
          <el-table :data="exchangeRecords">
            <el-table-column prop="itemName" label="物品名称" />
            <el-table-column prop="points" label="消耗积分" />
            <el-table-column prop="status" label="状态">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)">{{ getStatusLabel(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="兑换时间">
              <template #default="{ row }">
                {{ formatTime(row.createTime) }}
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 编辑信息对话框 -->
    <el-dialog v-model="showEditDialog" title="编辑个人信息" width="500px">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="昵称">
          <el-input v-model="editForm.nickname" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="editForm.phone" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" @click="handleUpdateProfile">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { getProfile, updateProfile } from '@/api/profile'
import { getUserPoints, exchangePoints, getExchangeRecords, purchasePoints } from '@/api/points'
import { getMyPosts } from '@/api/post'
import { getMyComments, deleteComment } from '@/api/comment'
import { getMyCollections } from '@/api/collection'
import { ChatDotRound, Pointer, View } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const userInfo = ref(null)
const userPoints = ref(null)
const myPosts = ref([])
const myComments = ref([])
const myCollections = ref([])
const exchangeRecords = ref([])
const exchangeItems = ref([
  { id: 1, name: '优惠券', description: '100元优惠券', points: 500 },
  { id: 2, name: '会员卡', description: '一个月会员', points: 1000 },
  { id: 3, name: '礼品卡', description: '200元礼品卡', points: 2000 }
])

const purchasePackages = ref([
  { id: 1, name: '100积分', money: '10元', points: 100 },
  { id: 2, name: '500积分', money: '45元', points: 500 },
  { id: 3, name: '1000积分', money: '88元', points: 1000 }
])

const activeTab = ref('posts')
const showEditDialog = ref(false)
const editForm = ref({
  nickname: '',
  phone: ''
})

const stats = ref({
  postCount: 0,
  commentCount: 0
})

const fetchUserInfo = async () => {
  try {
    const res = await getProfile()
    userInfo.value = res.data
    editForm.value = { nickname: res.data.nickname, phone: res.data.phone }
  } catch (error) {
    ElMessage.error('获取用户信息失败')
  }
}

const fetchUserPoints = async () => {
  try {
    const res = await getUserPoints()
    userPoints.value = res.data
  } catch (error) {
    console.error(error)
  }
}

const fetchExchangeRecords = async () => {
  try {
    const res = await getExchangeRecords({ page: 1, size: 20 })
    exchangeRecords.value = res.data.records || []
  } catch (error) {
    console.error(error)
  }
}

const fetchMyPosts = async () => {
  try {
    const res = await getMyPosts({ page: 1, size: 50 })
    myPosts.value = res.data.records || []
    stats.value.postCount = res.data.total || myPosts.value.length
  } catch (error) {
    console.error(error)
  }
}

const fetchMyComments = async () => {
  try {
    const res = await getMyComments({ page: 1, size: 50 })
    myComments.value = res.data.records || []
    stats.value.commentCount = res.data.total || myComments.value.length
  } catch (error) {
    console.error(error)
  }
}

const fetchMyCollections = async () => {
  try {
    const res = await getMyCollections({ page: 1, size: 50 })
    myCollections.value = res.data.records || []
  } catch (error) {
    console.error(error)
  }
}

const handleUpdateProfile = async () => {
  try {
    await updateProfile(editForm.value)
    ElMessage.success('更新成功')
    showEditDialog.value = false
    fetchUserInfo()
  } catch (error) {
    ElMessage.error('更新失败')
  }
}

const handleExchange = async (itemId) => {
  try {
    await exchangePoints(itemId)
    ElMessage.success('兑换成功')
    fetchUserPoints()
    fetchExchangeRecords()
  } catch (error) {
    ElMessage.error('兑换失败')
  }
}

const handlePurchase = async (packageId) => {
  try {
    const res = await purchasePoints(packageId)
    userPoints.value = res.data
    ElMessage.success('购买成功，积分已到账')
  } catch (error) {
    ElMessage.error('购买失败')
  }
}

const canExchange = (points) => {
  return userPoints.value && userPoints.value.availablePoints >= points
}

const goToPost = (postId) => {
  router.push(`/post/${postId}`)
}

const deleteCommentById = async (commentId) => {
  try {
    await deleteComment(commentId)
    ElMessage.success('删除成功')
    myComments.value = myComments.value.filter(c => c.id !== commentId)
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

const formatTime = (time) => {
  return new Date(time).toLocaleString('zh-CN')
}

const getStatusType = (status) => {
  const map = { pending: 'warning', completed: 'success', cancelled: 'danger' }
  return map[status] || 'info'
}

const getStatusLabel = (status) => {
  const map = { pending: '待发货', completed: '已完成', cancelled: '已取消' }
  return map[status] || '未知'
}

onMounted(() => {
  fetchUserInfo()
  fetchUserPoints()
  fetchExchangeRecords()
  fetchMyPosts()
  fetchMyComments()
  fetchMyCollections()
})
</script>

<style scoped>
.profile-page {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
}

.profile-card {
  margin-bottom: 20px;
}

.profile-header {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 20px;
}

.profile-info {
  flex: 1;
}

.profile-info h2 {
  margin: 0 0 10px 0;
  color: #303133;
}

.profile-info p {
  margin: 5px 0;
  color: #606266;
  font-size: 14px;
}

.stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin: 20px 0;
}

.stat-item {
  text-align: center;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 4px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #409eff;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  color: #606266;
}

.post-list, .comment-list, .collection-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.post-item, .collection-item {
  padding: 15px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.post-item:hover, .collection-item:hover {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.post-item h4, .collection-item h4 {
  margin: 0 0 10px 0;
  color: #303133;
}

.post-item p, .collection-item p {
  margin: 0 0 10px 0;
  color: #606266;
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.post-meta, .collection-meta {
  display: flex;
  gap: 15px;
  font-size: 12px;
  color: #909399;
}

.comment-item {
  padding: 15px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
}

.comment-item p {
  margin: 0 0 10px 0;
  color: #606266;
}

.comment-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: #909399;
}

.exchange-section {
  padding: 20px;
}

.exchange-info {
  margin-bottom: 20px;
  padding: 15px;
  background: #f0f9ff;
  border-radius: 4px;
}

.exchange-info p {
  margin: 0;
  font-size: 16px;
}

.points {
  color: #409eff;
  font-weight: bold;
  font-size: 20px;
}

.hint {
  margin-top: 8px;
  font-size: 13px;
  color: #909399;
}

.exchange-items {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 15px;
}

.exchange-item {
  padding: 15px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  display: flex;
  flex-direction: column;
}

.exchange-item h4 {
  margin: 0 0 10px 0;
  color: #303133;
}

.exchange-item p {
  margin: 0 0 10px 0;
  color: #606266;
  font-size: 14px;
  flex: 1;
}

.exchange-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.points-cost {
  color: #409eff;
  font-weight: bold;
}

@media (max-width: 768px) {
  .profile-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .profile-header .el-button {
    width: 100%;
  }

  .stats {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }

  .exchange-items {
    grid-template-columns: 1fr;
  }
}
</style>
