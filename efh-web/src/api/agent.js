import request from '@/utils/request'
import { apiUrl } from '@/utils/apiBase'

export const sendChat = (data) => {
  return request({
    url: '/agent/api/agent/chat',
    method: 'post',
    data,
    timeout: 120000
  })
}

export const sendChatStream = (data, handlers = {}) => {
  const userStore = JSON.parse(localStorage.getItem('user') || '{}')
  const token = userStore?.token
  const controller = new AbortController()

  fetch(apiUrl('/api/agent/api/agent/chat/stream'), {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {})
    },
    body: JSON.stringify(data),
    signal: controller.signal
  }).then(async (res) => {
    if (!res.ok) {
      throw new Error(`HTTP ${res.status}`)
    }
    const reader = res.body.getReader()
    const decoder = new TextDecoder()
    let buffer = ''
    while (true) {
      const { done, value } = await reader.read()
      if (done) break
      buffer += decoder.decode(value, { stream: true })
      const parts = buffer.split('\n\n')
      buffer = parts.pop()
      for (const part of parts) {
        const lines = part.split('\n')
        const eventLine = lines.find(line => line.startsWith('event:'))
        const dataLines = lines.filter(line => line.startsWith('data:'))
        const eventName = eventLine ? eventLine.replace(/^event:\s*/, '') : 'message'
        const data = dataLines.map(line => line.replace(/^data:\s*/, '')).join('\n')

        if (eventName === 'stage') {
          handlers.onStage && handlers.onStage(data)
        } else if (eventName === 'meta') {
          try {
            handlers.onMeta && handlers.onMeta(JSON.parse(data))
          } catch (error) {
            handlers.onMeta && handlers.onMeta(null)
          }
        } else if (eventName === 'done') {
          handlers.onDone && handlers.onDone(data)
        } else if (data) {
          handlers.onDelta && handlers.onDelta(data)
        }
      }
    }
    handlers.onClose && handlers.onClose()
  }).catch((error) => {
    handlers.onError && handlers.onError(error)
  })

  return () => controller.abort()
}

export const getAgentHealth = () => {
  return request({
    url: '/agent/api/agent/health',
    method: 'get'
  })
}

export const getChatHistory = (sessionId) => {
  return request({
    url: `/agent/api/agent/chat/history/${sessionId}`,
    method: 'get'
  })
}
