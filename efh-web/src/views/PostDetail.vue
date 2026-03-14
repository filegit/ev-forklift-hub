<template>
  <div class="post-detail">
    <el-card class="post-card" v-loading="loading">
      <!-- 帖子内容 -->
      <div class="post-header">
        <h1 class="post-title">{{ post.title }}</h1>
        <el-tag :type="getCategoryType(post.category)">
          {{ getCategoryName(post.category) }}
        </el-tag>
      </div>
      
      <div class="post-meta">
        <span>发布时间：{{ formatTime(post.createTime) }}</span>
        <div class="post-stats">
          <span><el-icon><View /></el-icon> {{ post.viewCount }}</span>
          <span><el-icon><ChatDotRound /></el-icon> {{ post.commentCount }}</span>
          <span><el-icon><ThumbsUp /></el-icon> {{ post.likeCount }}</span>
        </div>
      </div>
      
      <el-divider />
      
      <div class="post-content">{{ post.content }}</div>
      
      <div class="post-actions">
        <el-button 
          :type="isLiked ? 'primary' : 'default'"
          @click="handleLike"
          :disabled="!userStore.token"
        >
          <el-icon><ThumbsUp /></el-icon>
          {{ isLiked ? '已赞' : '点赞' }} ({{ post.likeCount }})
        </el-button>
        
        <el-button 
          :type="isCollected ? 'primary' : 'default'"
          @click="handleCollection"
          :disabled="!userStore.token"
        >
          <el-icon><Star /></el-icon>
          {{ isCollected ? '已收藏' : '收藏' }}
        </el-button>
      </div>
    </el-card>
    
    <!-- 评论区 -->
    <el-card class="comment-card">
      <h3>评论 ({{ commentList.length }})</h3>
      
      <!-- 发表评论 -->
      <div class="comment-input" v-if="userStore.token">
        <el-input
          v-model="commentContent"
          type="textarea"
          :rows="3"
          placeholder="写下你的评论..."
        />
        <el-button type="primary" @click="handleComment" :loading="commenting">
          发表评论
        </el-button>
      </div>
      <el-alert v-else type="info" :closable="false">
        请先<router-link to="/login">登录</router-link>后再发表评论
      </el-alert>
      
      <!-- 评论列表 -->
      <div class="comment-list">
        <div class="comment-item" v-for="comment in commentList" :key="comment.id">
          <div class="comment-header">
            <el-avatar :size="40">{{ comment.userId }}</el-avatar>
            <div class="comment-info">
              <div class="comment-user">用户{{ comment.userId }}</div>
              <div class="comment-time">{{ formatTime(comment.createTime) }}</div>
            </div>
            <div class="comment-actions">
              <el-button 
                text 
                :type="isCommentLiked(comment.id) ? 'primary' : 'default'"
                @click="handleCommentLike(comment.id)"
                v-if="userStore.token"
              >
                <el-icon><ThumbsUp /></el-icon>
                {{ comment.likeCount }}
              </el-button>
              <el-button
                v-if="userStore.userInfo?.id === comment.userId"
                type="danger"
                text
                @click="handleDeleteComment(comment.id)"
              >
                删除
              </el-button>
            </div>
          </div>
          <div class="comment-content">{{ comment.content }}</div>
        </div>
        
        <el-empty v-if="commentList.length === 0" description="暂无评论" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const userStore = useUserStore()
const loading = ref(false)
const commenting = ref(false)
const post = ref({})
const commentList = ref([])
const commentContent = ref('')
const isLiked = ref(false)
const isCollected = ref(false)
const commentLikes = ref(new Set())

const fetchPostDetail = async () => {
  loading.value = true
  try {
    const res = await fetch(`/api/community/api/post/${route.params.id}`)
    const data = await res.json()
    post.value = data.data
  } catch (error) {
    ElMessage.error('获取帖子详情失败')
  } finally {
    loading.value = false
  }
}

const fetchCommentList = async () => {
  try {
    const res = await fetch(`/api/community/api/comment/list?postId=${route.params.id}`)
    const data = await res.json()
    commentList.value = data.data.records || []
  } catch (error) {
    ElMessage.error('获取评论列表失败')
  }
}

const checkLikeStatus = async () => {
  if (!userStore.token) return
  
  try {
    const res = await fetch(`/api/like/post/${route.params.id}/check`, {
      headers: { 'Authorization': `Bearer ${userStore.token}` }
    })
    const data = await res.json()
    isLiked.value = data.data
  } catch (error) {
    console.error(error)
  }
}

const checkCollectionStatus = async () => {
  if (!userStore.token) return
  
  try {
    const res = await fetch(`/api/collection/check/${route.params.id}`, {
      headers: { 'Authorization': `Bearer ${userStore.token}` }
    })
    const data = await res.json()
    isCollected.value = data.data
  } catch (error) {
    console.error(error)
  }
}

const checkCommentLikes = async () => {
  if (!userStore.token) return
  
  for (const comment of commentList.value) {
    try {
      const res = await fetch(`/api/like/comment/${comment.id}/check`, {
        headers: { 'Authorization': `Bearer ${userStore.token}` }
      })
      const data = await res.json()
      if (data.data) {
        commentLikes.value.add(comment.id)
      }
    } catch (error) {
      console.error(error)
    }
  }
}

const handleLike = async () => {
  try {
    const res = await fetch(`/api/like/post/${route.params.id}`, {
      method: 'POST',
      headers: { 'Authorization': `Bearer ${userStore.token}` }
    })
    const data = await res.json()
    if (data.code === 200) {
      isLiked.value = !isLiked.value
      if (isLiked.value) {
        post.value.likeCount++
        ElMessage.success('点赞成功')
      } else {
        post.value.likeCount--
        ElMessage.success('已取消点赞')
      }
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleCollection = async () => {
  try {
    const res = await fetch(`/api/collection/${route.params.id}`, {
      method: 'POST',
      headers: { 'Authorization': `Bearer ${userStore.token}` }
    })
    const data = await res.json()
    if (data.code === 200) {
      isCollected.value = !isCollected.value
      ElMessage.success(isCollected.value ? '收藏成功' : '已取消收藏')
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleComment = async () => {
  if (!commentContent.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }
  
  commenting.value = true
  try {
    const res = await fetch('/api/community/api/comment', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${userStore.token}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        postId: route.params.id,
        content: commentContent.value,
        parentId: 0
      })
    })
    const data = await res.json()
    if (data.code === 200) {
      ElMessage.success('评论成功')
      commentContent.value = ''
      fetchCommentList()
      post.value.commentCount++
    }
  } catch (error) {
    ElMessage.error('评论失败')
  } finally {
    commenting.value = false
  }
}

const handleCommentLike = async (commentId) => {
  try {
    const res = await fetch(`/api/like/comment/${commentId}`, {
      method: 'POST',
      headers: { 'Authorization': `Bearer ${userStore.token}` }
    })
    const data = await res.json()
    if (data.code === 200) {
      const comment = commentList.value.find(c => c.id === commentId)
      if (commentLikes.value.has(commentId)) {
        commentLikes.value.delete(commentId)
        comment.likeCount--
      } else {
        commentLikes.value.add(commentId)
        comment.likeCount++
      }
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const isCommentLiked = (commentId) => {
  return commentLikes.value.has(commentId)
}

const handleDeleteComment = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这条评论吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const res = await fetch(`/api/community/api/comment/${id}`, {
      method: 'DELETE',
      headers: { 'Authorization': `Bearer ${userStore.token}` }
    })
    const data = await res.json()
    if (data.code === 200) {
      ElMessage.success('删除成功')
      fetchCommentList()
      post.value.commentCount--
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
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
  fetchPostDetail()
  fetchCommentList()
  checkLikeStatus()
  checkCollectionStatus()
  setTimeout(() => checkCommentLikes(), 500)
})
</script>

<style scoped>
.post-detail {
  max-width: 900px;
  margin: 0 auto;
}

.post-card {
  margin-bottom: 20px;
}

.post-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.post-title {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.post-meta {
  display: flex;
  justify-content: space-between;
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

.post-content {
  font-size: 16px;
  line-height: 1.8;
  color: #606266;
  white-space: pre-wrap;
  margin: 20px 0;
}

.post-actions {
  margin-top: 20px;
  display: flex;
  gap: 10px;
}

.comment-card {
  margin-top: 20px;
}

.comment-input {
  margin: 20px 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.comment-input .el-button {
  align-self: flex-end;
}

.comment-list {
  margin-top: 20px;
}

.comment-item {
  padding: 15px 0;
  border-bottom: 1px solid #ebeef5;
}

.comment-item:last-child {
  border-bottom: none;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.comment-info {
  flex: 1;
}

.comment-user {
  font-weight: 600;
  color: #303133;
}

.comment-time {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}

.comment-actions {
  display: flex;
  gap: 10px;
}

.comment-content {
  color: #606266;
  line-height: 1.6;
  margin-left: 50px;
}
</style>
