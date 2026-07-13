export const formatDateTime = (time) => {
  if (!time) return '-'
  const date = new Date(time)
  if (Number.isNaN(date.getTime())) return String(time).replace('T', ' ').slice(0, 16)
  return date.toLocaleString('zh-CN', { hour12: false })
}

export const postCategories = [
  { label: '全部', value: 0, type: 'info' },
  { label: '技术交流', value: 1, type: 'primary' },
  { label: '故障求助', value: 2, type: 'warning' },
  { label: '经验分享', value: 3, type: 'success' },
  { label: '其他', value: 4, type: 'info' },
  { label: '保养经验', value: 5, type: 'success' },
  { label: '故障维修案例', value: 6, type: 'warning' },
  { label: '备件选型', value: 7, type: 'primary' }
]

export const getPostCategory = (value) => {
  return postCategories.find(item => item.value === Number(value)) || { label: '未知', value, type: 'info' }
}

export const knowledgeDirectories = [
  {
    label: '常规维修案例库',
    value: 'repair',
    children: ['维修手册', '故障案例', '技术规范', '安全指南']
  },
  {
    label: '电池保养资料库',
    value: 'battery',
    children: ['铅酸电池保养', '锂电池保养', '充电规范']
  }
]
