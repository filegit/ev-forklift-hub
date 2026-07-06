<template>
  <div class="assistant-page">
    <el-card class="chat-card">
      <template #header>
        <div class="card-header">
          <div>
            <h2>叉车 AI 助手</h2>
            <p class="subtitle">基于知识库与社区内容的智能问答（RAG）</p>
          </div>
          <el-radio-group v-model="scope" size="small">
            <el-radio-button label="all">全部</el-radio-button>
            <el-radio-button label="knowledge">知识库</el-radio-button>
            <el-radio-button label="community">社区</el-radio-button>
          </el-radio-group>
        </div>
      </template>

      <div class="chat-body" ref="chatBodyRef">
        <div v-if="messages.length === 0" class="welcome">
          <el-icon :size="48" color="#409eff"><ChatDotRound /></el-icon>
          <h3>你好，我是新能源叉车 AI 助手</h3>
          <p>我可以帮你检索知识库文档和社区讨论，回答电池、维修、保养等问题。</p>
          <div class="quick-questions">
            <el-tag
              v-for="q in quickQuestions"
              :key="q"
              class="quick-tag"
              @click="askQuick(q)"
            >{{ q }}</el-tag>
          </div>
        </div>

        <div v-for="(msg, index) in messages" :key="index" :class="['msg-row', msg.role]">
          <div class="bubble">
            <div class="msg-text" v-html="formatText(msg.content)"></div>
            <div v-if="msg.sources && msg.sources.length" class="sources">
              <div class="sources-title">参考来源</div>
              <div
                v-for="source in msg.sources"
                :key="source.type + '-' + source.id"
                class="source-item"
                @click="goSource(source)"
              >
                <el-tag size="small" :type="sourceTagType(source.type)">{{ sourceLabel(source.type) }}</el-tag>
                <span class="source-title">{{ source.title }}</span>
                <el-tag v-if="source.type === 'knowledge' && !source.unlocked" size="small" type="warning">需解锁</el-tag>
              </div>
            </div>
          </div>
        </div>

        <div v-if="loading" class="msg-row assistant">
          <div class="bubble loading-bubble">
            <el-icon class="is-loading"><Loading /></el-icon>
            正在检索资料并生成回答...
          </div>
        </div>
      </div>

      <div class="chat-input">
        <el-input
          v-model="question"
          type="textarea"
          :rows="2"
          placeholder="输入你的问题，例如：锂电池充不进电怎么办？"
          @keydown.enter.exact.prevent="handleSend"
        />
        <el-button type="primary" :loading="loading" @click="handleSend">发送</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { sendChat } from '@/api/agent'

const router = useRouter()
const sessionId = ref(localStorage.getItem('agent_session_id') || '')
const question = ref('')
const scope = ref('all')
const loading = ref(false)
const messages = ref([])
const chatBodyRef = ref(null)

const quickQuestions = [
  '锂电池充不进电怎么办？',
  '如何选择电动叉车？',
  '叉车电池如何保养？',
  '48V电池常见故障有哪些？'
]

const scrollToBottom = async () => {
  await nextTick()
  if (chatBodyRef.value) {
    chatBodyRef.value.scrollTop = chatBodyRef.value.scrollHeight
  }
}

const formatText = (text) => {
  if (!text) return ''
  return text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/\n/g, '<br/>')
}

const sourceLabel = (type) => {
  const map = { knowledge: '知识库', post: '帖子', comment: '评论' }
  return map[type] || type
}

const sourceTagType = (type) => {
  const map = { knowledge: 'success', post: 'primary', comment: 'info' }
  return map[type] || 'info'
}

const goSource = (source) => {
  if (source.link) {
    router.push(source.link)
  }
}

const askQuick = (q) => {
  question.value = q
  handleSend()
}

const handleSend = async () => {
  const q = question.value.trim()
  if (!q || loading.value) return

  messages.value.push({ role: 'user', content: q })
  question.value = ''
  loading.value = true
  await scrollToBottom()

  try {
    const res = await sendChat({ question: q, scope: scope.value, sessionId: sessionId.value || undefined })
    if (res.data.sessionId) {
      sessionId.value = res.data.sessionId
      localStorage.setItem('agent_session_id', res.data.sessionId)
    }
    messages.value.push({
      role: 'assistant',
      content: res.data.answer,
      sources: res.data.sources || []
    })
  } catch (error) {
    ElMessage.error('问答失败，请稍后重试')
  } finally {
    loading.value = false
    scrollToBottom()
  }
}
</script>

<style scoped>
.assistant-page {
  max-width: 960px;
  margin: 0 auto;
}

.chat-card {
  min-height: 70vh;
  display: flex;
  flex-direction: column;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}

.card-header h2 {
  margin: 0;
  font-size: 20px;
}

.subtitle {
  margin: 4px 0 0;
  color: #909399;
  font-size: 13px;
}

.chat-body {
  flex: 1;
  min-height: 420px;
  max-height: 520px;
  overflow-y: auto;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
  margin-bottom: 16px;
}

.welcome {
  text-align: center;
  padding: 40px 20px;
  color: #606266;
}

.welcome h3 {
  margin: 16px 0 8px;
  color: #303133;
}

.quick-questions {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 8px;
  margin-top: 20px;
}

.quick-tag {
  cursor: pointer;
}

.msg-row {
  display: flex;
  margin-bottom: 14px;
}

.msg-row.user {
  justify-content: flex-end;
}

.msg-row.assistant {
  justify-content: flex-start;
}

.bubble {
  max-width: 85%;
  padding: 12px 16px;
  border-radius: 12px;
  line-height: 1.6;
  font-size: 14px;
}

.user .bubble {
  background: #409eff;
  color: #fff;
  border-bottom-right-radius: 4px;
}

.assistant .bubble {
  background: #fff;
  color: #303133;
  border: 1px solid #ebeef5;
  border-bottom-left-radius: 4px;
}

.loading-bubble {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #909399;
}

.sources {
  margin-top: 12px;
  padding-top: 10px;
  border-top: 1px dashed #ebeef5;
}

.sources-title {
  font-size: 12px;
  color: #909399;
  margin-bottom: 8px;
}

.source-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 0;
  cursor: pointer;
  font-size: 13px;
}

.source-item:hover .source-title {
  color: #409eff;
}

.source-title {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.chat-input {
  display: flex;
  gap: 12px;
  align-items: flex-end;
}

.chat-input .el-textarea {
  flex: 1;
}

@media (max-width: 768px) {
  .assistant-page {
    margin: 0 -4px;
  }

  .card-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .chat-body {
    min-height: 50vh;
    max-height: none;
    padding: 12px;
  }

  .bubble {
    max-width: 92%;
  }

  .chat-input {
    flex-direction: column;
    align-items: stretch;
  }

  .chat-input .el-button {
    width: 100%;
  }

  .welcome {
    padding: 24px 12px;
  }
}
</style>
