import request from '@/utils/request'

export const sendChat = (data) => {
  return request({
    url: '/agent/api/agent/chat',
    method: 'post',
    data,
    timeout: 120000
  })
}

export const getAgentHealth = () => {
  return request({
    url: '/agent/api/agent/health',
    method: 'get'
  })
}
