import request from '@/utils/request'

export const sendChat = (data) => {
  return request({
    url: '/agent/api/agent/chat',
    method: 'post',
    data,
    timeout: 120000
  })
}

/** SSE 流式对话 */
export const sendChatStream = (data, onDelta, onDone, onError) => {
  const userStore = JSON.parse(localStorage.getItem('user') || '{}')
  const token = userStore?.token
  const controller = new AbortController()
  fetch('/agent/api/agent/chat/stream', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {})
    },
    body: JSON.stringify(data),
    signal: controller.signal
  }).then(async (res) => {
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
        if (part.startsWith('data:')) {
          onDelta && onDelta(part.replace(/^data:\s*/, ''))
        }
        if (part.startsWith('event: done')) {
          const line = part.split('\n').find(l => l.startsWith('data:'))
          onDone && onDone(line ? line.replace(/^data:\s*/, '') : '')
        }
      }
    }
  }).catch(onError)
  return () => controller.abort()
}

export const getAgentHealth = () => {
  return request({
    url: '/agent/api/agent/health',
    method: 'get'
  })
}
