<template>
  <div class="assistant-page">
    <section class="assistant-shell">
      <div class="chat-panel">
        <div class="panel-header">
          <div>
            <h2>叉车 AI Agent</h2>
            <p>面向售后、配件、知识库和社区问答的业务协同助手</p>
          </div>
          <div class="header-actions">
            <el-button text size="small" @click="clearHistory">清空本机记录</el-button>
            <el-radio-group v-model="scope" size="small">
              <el-radio-button label="all">全部</el-radio-button>
              <el-radio-button label="knowledge">知识库</el-radio-button>
              <el-radio-button label="community">社区</el-radio-button>
            </el-radio-group>
          </div>
        </div>

        <div class="chat-body" ref="chatBodyRef">
          <div v-if="messages.length === 0" class="welcome">
            <el-icon :size="44" color="#2563eb"><ChatDotRound /></el-icon>
            <h3>新能源叉车智能客服已就绪</h3>
            <p>可以询问故障诊断、保养规范、订单物流、售后工单，也可以附带图片 URL 做多模态诊断入口。</p>
            <div class="quick-questions">
              <el-tag v-for="q in quickQuestions" :key="q" class="quick-tag" @click="askQuick(q)">
                {{ q }}
              </el-tag>
            </div>
          </div>

          <div v-for="(msg, index) in messages" :key="index" :class="['msg-row', msg.role]">
            <div class="bubble">
              <div class="msg-text" v-html="formatText(msg.content)"></div>
              <div v-if="msg.meta" class="answer-meta">
                <el-tag size="small" effect="plain">{{ msg.meta.intent || 'GENERAL_QA' }}</el-tag>
                <el-tag v-if="msg.meta.llmUsed === false" size="small" type="warning" effect="plain">兜底模式</el-tag>
              </div>
              <div v-if="msg.sources && msg.sources.length" class="sources">
                <div class="sources-title">参考来源</div>
                <div
                  v-for="source in msg.sources"
                  :key="source.type + '-' + source.id + '-' + source.title"
                  class="source-item"
                  @click="goSource(source)"
                >
                  <el-tag size="small" :type="sourceTagType(source.type)">{{ sourceLabel(source.type) }}</el-tag>
                  <span class="source-title">{{ source.title || '未命名资料' }}</span>
                  <el-tag v-if="source.type === 'knowledge' && !source.unlocked" size="small" type="warning">需解锁</el-tag>
                </div>
              </div>
            </div>
          </div>

          <div v-if="loading" class="msg-row assistant">
            <div class="bubble loading-bubble">
              <el-icon class="is-loading"><Loading /></el-icon>
              {{ currentStage || '正在执行 Agent 工作流...' }}
            </div>
          </div>
        </div>

        <div class="input-tools">
          <el-input v-model="imageUrlText" placeholder="可选：粘贴故障图片 URL，多个用逗号分隔" clearable />
          <el-checkbox v-model="allowCreateTicket">允许确认后创建售后工单</el-checkbox>
        </div>

        <div class="chat-input">
          <el-input
            v-model="question"
            type="textarea"
            :rows="3"
            placeholder="输入问题，例如：PO202607100001 的配件订单物流到哪了？或：叉车 48V 电池充不进电怎么排查？"
            @keydown.enter.exact.prevent="handleSend"
          />
          <el-button type="primary" :loading="loading" @click="handleSend">发送</el-button>
        </div>
      </div>

      <aside class="trace-panel">
        <div class="trace-header">
          <h3>Agent 执行链路</h3>
          <el-tag size="small" type="success" effect="plain">可观测</el-tag>
        </div>

        <div class="trace-section">
          <div class="trace-title">会话</div>
          <div class="session-line">{{ sessionId || '尚未开始' }}</div>
        </div>

        <div class="trace-section">
          <div class="trace-title">阶段事件</div>
          <div v-if="stageEvents.length === 0" class="empty-trace">等待一次对话开始</div>
          <div v-for="(stage, index) in stageEvents" :key="index" class="stage-item">
            <span class="stage-dot"></span>
            <span>{{ stage }}</span>
          </div>
        </div>

        <div class="trace-section">
          <div class="trace-title">规划步骤</div>
          <div v-if="lastPlan.length === 0" class="empty-trace">暂无计划</div>
          <el-tag v-for="step in lastPlan" :key="step" class="plan-tag" size="small" effect="plain">
            {{ step }}
          </el-tag>
        </div>

        <div class="trace-section">
          <div class="trace-title">工具调用</div>
          <div v-if="lastToolCalls.length === 0" class="empty-trace">暂无工具调用</div>
          <div v-for="tool in lastToolCalls" :key="tool" class="tool-line">{{ tool }}</div>
        </div>
      </aside>
    </section>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ChatDotRound, Loading } from '@element-plus/icons-vue'
import { sendChat, sendChatStream, getChatHistory } from '@/api/agent'

const router = useRouter()
const sessionId = ref(localStorage.getItem('agent_session_id') || '')
const question = ref('')
const imageUrlText = ref('')
const scope = ref('all')
const allowCreateTicket = ref(false)
const loading = ref(false)
const messages = ref([])
const stageEvents = ref([])
const lastPlan = ref([])
const lastToolCalls = ref([])
const currentStage = ref('')
const chatBodyRef = ref(null)
let abortStream = null

const historyKey = () => `agent_messages_${sessionId.value || 'anonymous'}`

const quickQuestions = [
  '叉车 48V 电池充不进电怎么排查？',
  'PO202607100001 的配件订单物流到哪了？',
  '我要报修，叉车升降无力，需要上门维修',
  '结合知识库和社区经验，比较铅酸电池和锂电池保养差异'
]

onMounted(async () => {
  loadLocalHistory()
  if (sessionId.value) {
    await loadRemoteHistory()
  }
  await scrollToBottom()
})

watch(messages, () => {
  saveLocalHistory()
}, { deep: true })

const loadLocalHistory = () => {
  if (!sessionId.value) return
  try {
    const raw = localStorage.getItem(historyKey())
    if (raw) {
      messages.value = JSON.parse(raw)
    }
  } catch (error) {
    localStorage.removeItem(historyKey())
  }
}

const saveLocalHistory = () => {
  if (!sessionId.value || messages.value.length === 0) return
  localStorage.setItem(historyKey(), JSON.stringify(messages.value.slice(-40)))
}

const loadRemoteHistory = async () => {
  try {
    const res = await getChatHistory(sessionId.value)
    const rows = res.data?.messages || []
    if (rows.length) {
      messages.value = rows
        .filter(item => item.role === 'user' || item.role === 'assistant')
        .map(item => ({
          role: item.role,
          content: item.content,
          sources: [],
          meta: null
        }))
    }
  } catch (error) {
    // Local history already gives the user continuity when Redis/MySQL is unavailable.
  }
}

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
  const map = { knowledge: '知识库', post: '帖子', comment: '评论', tool: '工具', tool_error: '异常', image: '图片' }
  return map[type] || type
}

const sourceTagType = (type) => {
  const map = { knowledge: 'success', post: 'primary', comment: 'info', tool: 'warning', tool_error: 'danger', image: 'info' }
  return map[type] || 'info'
}

const goSource = (source) => {
  if (source.link && source.link !== '/') {
    router.push(source.link)
  }
}

const askQuick = (q) => {
  question.value = q
  handleSend()
}

const buildPayload = (q) => ({
  question: q,
  scope: scope.value,
  sessionId: sessionId.value || undefined,
  allowCreateTicket: allowCreateTicket.value,
  imageUrls: imageUrlText.value
    .split(/[,\n，]/)
    .map(item => item.trim())
    .filter(Boolean)
})

const handleSend = async () => {
  const q = question.value.trim()
  if (!q || loading.value) return

  messages.value.push({ role: 'user', content: q })
  question.value = ''
  loading.value = true
  stageEvents.value = []
  lastPlan.value = []
  lastToolCalls.value = []
  currentStage.value = ''
  await scrollToBottom()

  const assistantMsg = { role: 'assistant', content: '', sources: [], meta: null }
  messages.value.push(assistantMsg)
  const payload = buildPayload(q)

  abortStream = sendChatStream(payload, {
    onStage: async (stage) => {
      currentStage.value = stage
      stageEvents.value.push(stage)
      await scrollToBottom()
    },
    onDelta: async (delta) => {
      assistantMsg.content += delta
      await scrollToBottom()
    },
    onMeta: async (meta) => {
      if (!meta) return
      applyResponseMeta(meta, assistantMsg)
      await scrollToBottom()
    },
    onDone: async () => {
      if (!assistantMsg.content) {
        await fallbackSend(payload, assistantMsg)
      }
    },
    onError: async () => {
      await fallbackSend(payload, assistantMsg)
    },
    onClose: async () => {
      loading.value = false
      currentStage.value = ''
      saveLocalHistory()
      await scrollToBottom()
    }
  })
}

const applyResponseMeta = (data, assistantMsg) => {
  if (data.sessionId) {
    const oldKey = historyKey()
    sessionId.value = data.sessionId
    localStorage.setItem('agent_session_id', data.sessionId)
    if (oldKey !== historyKey()) {
      localStorage.removeItem(oldKey)
    }
  }
  assistantMsg.sources = data.sources || []
  assistantMsg.meta = { intent: data.intent, llmUsed: data.llmUsed }
  stageEvents.value = data.stageEvents || stageEvents.value
  lastPlan.value = data.plan || []
  lastToolCalls.value = data.toolCalls || []
  saveLocalHistory()
}

const fallbackSend = async (payload, assistantMsg) => {
  try {
    const res = await sendChat(payload)
    const data = res.data || {}
    assistantMsg.content = data.answer || '未返回答案'
    applyResponseMeta(data, assistantMsg)
  } catch (error) {
    assistantMsg.content = '问答失败，请稍后重试。'
    ElMessage.error('问答失败，请稍后重试')
  } finally {
    loading.value = false
    currentStage.value = ''
    abortStream = null
    saveLocalHistory()
    await scrollToBottom()
  }
}

const clearHistory = () => {
  if (sessionId.value) {
    localStorage.removeItem(historyKey())
  }
  localStorage.removeItem('agent_session_id')
  sessionId.value = ''
  messages.value = []
  stageEvents.value = []
  lastPlan.value = []
  lastToolCalls.value = []
  ElMessage.success('已清空本机咨询记录')
}
</script>

<style scoped>
.assistant-page {
  max-width: 1240px;
  margin: 0 auto;
}

.assistant-shell {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 320px;
  gap: 18px;
}

.chat-panel,
.trace-panel {
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  box-shadow: 0 12px 28px rgba(15, 23, 42, 0.06);
}

.chat-panel {
  padding: 18px;
}

.panel-header,
.trace-header,
.header-actions {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 16px;
}

.header-actions {
  align-items: center;
  margin-bottom: 0;
}

.panel-header h2,
.trace-header h3 {
  margin: 0;
  color: #111827;
}

.panel-header h2 {
  font-size: 22px;
}

.panel-header p {
  margin: 6px 0 0;
  color: #6b7280;
  font-size: 13px;
}

.chat-body {
  min-height: 470px;
  max-height: 590px;
  overflow-y: auto;
  padding: 16px;
  background: #f8fafc;
  border: 1px solid #eef2f7;
  border-radius: 8px;
}

.welcome {
  text-align: center;
  padding: 42px 20px;
  color: #4b5563;
}

.welcome h3 {
  margin: 14px 0 8px;
  color: #111827;
}

.welcome p {
  margin: 0 auto;
  max-width: 560px;
  line-height: 1.7;
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
  max-width: 84%;
  padding: 12px 14px;
  border-radius: 8px;
  line-height: 1.65;
  font-size: 14px;
}

.user .bubble {
  background: #2563eb;
  color: #ffffff;
}

.assistant .bubble {
  background: #ffffff;
  color: #1f2937;
  border: 1px solid #e5e7eb;
}

.loading-bubble {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #6b7280;
}

.answer-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 10px;
}

.sources {
  margin-top: 12px;
  padding-top: 10px;
  border-top: 1px dashed #e5e7eb;
}

.sources-title,
.trace-title {
  font-size: 12px;
  color: #6b7280;
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

.source-title {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.input-tools {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  align-items: center;
  gap: 12px;
  margin: 14px 0 10px;
}

.chat-input {
  display: flex;
  gap: 12px;
  align-items: stretch;
}

.chat-input .el-textarea {
  flex: 1;
}

.chat-input .el-button {
  width: 92px;
}

.trace-panel {
  padding: 16px;
}

.trace-section {
  border-top: 1px solid #eef2f7;
  padding-top: 14px;
  margin-top: 14px;
}

.session-line {
  color: #6b7280;
  font-size: 12px;
  word-break: break-all;
}

.stage-item {
  display: grid;
  grid-template-columns: 10px minmax(0, 1fr);
  gap: 8px;
  align-items: start;
  color: #374151;
  font-size: 13px;
  line-height: 1.6;
  margin-bottom: 8px;
}

.stage-dot {
  width: 7px;
  height: 7px;
  margin-top: 7px;
  border-radius: 50%;
  background: #2563eb;
}

.plan-tag {
  margin: 0 6px 6px 0;
}

.tool-line {
  font-size: 13px;
  color: #374151;
  padding: 6px 8px;
  background: #f8fafc;
  border-radius: 6px;
  margin-bottom: 6px;
}

.empty-trace {
  color: #9ca3af;
  font-size: 13px;
}

@media (max-width: 960px) {
  .assistant-shell {
    grid-template-columns: 1fr;
  }

  .panel-header,
  .input-tools,
  .chat-input {
    grid-template-columns: 1fr;
    flex-direction: column;
    align-items: stretch;
  }

  .header-actions {
    flex-direction: column-reverse;
    align-items: stretch;
  }

  .bubble {
    max-width: 94%;
  }

  .chat-input .el-button {
    width: 100%;
  }
}
</style>
