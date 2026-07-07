export const API_ORIGIN = import.meta.env.VITE_API_ORIGIN || ''

export const apiUrl = (path = '') => {
  if (!path) return API_ORIGIN || ''
  if (/^https?:\/\//i.test(path)) return path

  const normalizedPath = path.startsWith('/') ? path : `/${path}`
  if (!API_ORIGIN) return normalizedPath

  const origin = API_ORIGIN.replace(/\/$/, '')
  return `${origin}${normalizedPath}`
}

export const apiBaseURL = () => apiUrl('/api')
