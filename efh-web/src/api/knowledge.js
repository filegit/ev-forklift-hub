import request from '@/utils/request'
import axios from 'axios'
import { useUserStore } from '@/stores/user'
import { apiUrl } from '@/utils/apiBase'

const authHeaders = () => {
  const userStore = useUserStore()
  return userStore.token ? { Authorization: `Bearer ${userStore.token}` } : {}
}

export const getKnowledgeList = (params) => {
  return request({ url: '/knowledge/api/knowledge/doc/list', method: 'get', params })
}

export const getKnowledgeCategories = () => {
  return request({ url: '/knowledge/api/knowledge/doc/categories', method: 'get' })
}

export const getKnowledgeDetail = (id) => {
  return request({ url: `/knowledge/api/knowledge/doc/${id}`, method: 'get' })
}

export const unlockByPoints = (id) => {
  return request({ url: `/knowledge/api/knowledge/doc/${id}/unlock/points`, method: 'post' })
}

export const unlockByAlipay = (id) => {
  return request({ url: `/knowledge/api/knowledge/doc/${id}/unlock/alipay`, method: 'post' })
}

export const downloadKnowledgeDoc = async (id, fileName) => {
  const res = await axios({
    url: apiUrl(`/api/knowledge/api/knowledge/doc/${id}/download`),
    method: 'get',
    responseType: 'blob',
    headers: authHeaders()
  })
  const blob = new Blob([res.data])
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = fileName || 'document'
  link.click()
  URL.revokeObjectURL(link.href)
}

export const previewKnowledgeDoc = async (id) => {
  return axios({
    url: apiUrl(`/api/knowledge/api/knowledge/doc/${id}/preview`),
    method: 'get',
    responseType: 'blob',
    headers: authHeaders()
  })
}

export const adminGetDocList = (params) => {
  return request({ url: '/knowledge/api/knowledge/admin/doc/list', method: 'get', params })
}

export const adminUploadDoc = (formData) => {
  return request({
    url: '/knowledge/api/knowledge/admin/doc',
    method: 'post',
    data: formData
  })
}

export const adminUpdateDoc = (id, data) => {
  return request({ url: `/knowledge/api/knowledge/admin/doc/${id}`, method: 'put', data })
}

export const adminPublishDoc = (id) => {
  return request({ url: `/knowledge/api/knowledge/admin/doc/${id}/publish`, method: 'post' })
}

export const adminOfflineDoc = (id) => {
  return request({ url: `/knowledge/api/knowledge/admin/doc/${id}/offline`, method: 'post' })
}

export const adminDeleteDoc = (id) => {
  return request({ url: `/knowledge/api/knowledge/admin/doc/${id}`, method: 'delete' })
}
