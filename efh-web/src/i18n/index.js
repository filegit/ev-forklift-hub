import { computed, ref } from 'vue'

const zh = {
  common: {
    appName: '\u7535\u52a8\u53c9\u8f66\u534f\u540c\u5e73\u53f0',
    community: '\u8fd0\u8425\u793e\u533a',
    parts: '\u5907\u4ef6\u91c7\u8d2d',
    knowledge: '\u77e5\u8bc6\u5e93',
    assistant: 'AI\u52a9\u624b',
    service: '\u7ef4\u4fdd\u5de5\u5355',
    cart: '\u91c7\u8d2d\u8f66',
    publish: '\u53d1\u5e03',
    publishPost: '\u53d1\u5e03\u5e16\u5b50',
    profile: '\u4e2a\u4eba\u4e2d\u5fc3',
    orders: '\u6211\u7684\u8ba2\u5355',
    collections: '\u6211\u7684\u6536\u85cf',
    logout: '\u9000\u51fa\u767b\u5f55',
    login: '\u767b\u5f55',
    register: '\u6ce8\u518c',
    menu: '\u83dc\u5355',
    mine: '\u6211\u7684',
    home: '\u9996\u9875',
    cancel: '\u53d6\u6d88',
    confirm: '\u786e\u5b9a',
    delete: '\u5220\u9664',
    reply: '\u56de\u590d',
    submit: '\u63d0\u4ea4',
    close: '\u5173\u95ed',
    download: '\u4e0b\u8f7d',
    preview: '\u5728\u7ebf\u67e5\u770b',
    loading: '\u52a0\u8f7d\u4e2d',
    unknown: '\u672a\u77e5',
    noData: '\u6682\u65e0\u6570\u636e'
  },
  post: {
    title: '\u6807\u9898',
    titlePlaceholder: '\u8bf7\u8f93\u5165\u5e16\u5b50\u6807\u9898',
    category: '\u5206\u7c7b',
    content: '\u5185\u5bb9',
    contentPlaceholder: '\u8bf7\u8f93\u5165\u5e16\u5b50\u5185\u5bb9',
    tech: '\u6280\u672f\u4ea4\u6d41',
    trouble: '\u6545\u969c\u6c42\u52a9',
    experience: '\u7ecf\u9a8c\u5206\u4eab',
    other: '\u5176\u4ed6',
    publishTime: '\u53d1\u5e03\u65f6\u95f4',
    liked: '\u5df2\u8d5e',
    like: '\u70b9\u8d5e',
    collected: '\u5df2\u6536\u85cf',
    collect: '\u6536\u85cf',
    comments: '\u8bc4\u8bba',
    writeComment: '\u5199\u4e0b\u4f60\u7684\u8bc4\u8bba...',
    publishComment: '\u53d1\u8868\u8bc4\u8bba',
    loginToComment: '\u8bf7\u5148\u767b\u5f55\u540e\u518d\u53d1\u8868\u8bc4\u8bba',
    noComments: '\u6682\u65e0\u8bc4\u8bba',
    replyPlaceholder: '\u56de\u590d {name}...',
    replyTo: '\u56de\u590d {name}',
    showReplies: '{count} \u6761\u56de\u590d',
    fetchPostFailed: '\u83b7\u53d6\u5e16\u5b50\u8be6\u60c5\u5931\u8d25',
    fetchCommentsFailed: '\u83b7\u53d6\u8bc4\u8bba\u5217\u8868\u5931\u8d25',
    titleRequired: '\u8bf7\u8f93\u5165\u6807\u9898',
    contentRequired: '\u8bf7\u8f93\u5165\u5185\u5bb9',
    publishSuccess: '\u53d1\u5e03\u6210\u529f',
    commentRequired: '\u8bf7\u8f93\u5165\u8bc4\u8bba\u5185\u5bb9',
    commentSuccess: '\u8bc4\u8bba\u6210\u529f',
    replySuccess: '\u56de\u590d\u6210\u529f',
    deleteConfirm: '\u786e\u5b9a\u8981\u5220\u9664\u8fd9\u6761\u8bc4\u8bba\u5417\uff1f',
    deleteTitle: '\u63d0\u793a',
    deleteSuccess: '\u5220\u9664\u6210\u529f',
    deleteFailed: '\u5220\u9664\u5931\u8d25',
    logoutSuccess: '\u9000\u51fa\u6210\u529f'
  },
  knowledge: {
    detail: '\u6587\u6863\u8be6\u60c5',
    noSummary: '\u6682\u65e0\u6458\u8981',
    views: '\u6d4f\u89c8',
    downloads: '\u4e0b\u8f7d',
    uploader: '\u4e0a\u4f20\u4eba',
    unlockedTitle: '\u6587\u6863\u5df2\u89e3\u9501',
    unlockedSubTitle: '\u4f60\u53ef\u4ee5\u5728\u7ebf\u67e5\u770b\u6216\u4e0b\u8f7d\u5b8c\u6574\u6587\u6863',
    lockedTitle: '\u8be5\u6587\u6863\u9700\u8981\u89e3\u9501\u540e\u624d\u80fd\u67e5\u770b\u548c\u4e0b\u8f7d',
    unlockMethod: '\u89e3\u9501\u65b9\u5f0f\uff1a{label}',
    currentPoints: '\u5f53\u524d\u53ef\u7528\u79ef\u5206\uff1a{points}',
    pointsUnlock: '\u79ef\u5206\u89e3\u9501\uff08{points} \u79ef\u5206\uff09',
    alipayUnlock: '\u652f\u4ed8\u5b9d\u89e3\u9501\uff08￥{money}\uff09',
    loginToUnlock: '\u767b\u5f55\u540e\u89e3\u9501',
    confirmPoints: '\u786e\u8ba4\u4f7f\u7528 {points} \u79ef\u5206\u89e3\u9501\u8be5\u6587\u6863\uff1f',
    pointsTitle: '\u79ef\u5206\u89e3\u9501',
    unlockSuccess: '\u89e3\u9501\u6210\u529f',
    downloadStart: '\u5f00\u59cb\u4e0b\u8f7d',
    downloadFailed: '\u4e0b\u8f7d\u5931\u8d25',
    previewTitle: '\u5728\u7ebf\u67e5\u770b',
    previewLoading: '\u6b63\u5728\u52a0\u8f7d\u9884\u89c8...',
    previewFailed: '\u9884\u89c8\u52a0\u8f7d\u5931\u8d25\uff0c\u8bf7\u4e0b\u8f7d\u540e\u67e5\u770b',
    unsupportedTitle: '\u8be5\u683c\u5f0f\u6682\u4e0d\u652f\u6301\u6d4f\u89c8\u5668\u5185\u76f4\u63a5\u9884\u89c8',
    unsupportedDesc: 'Office\u3001\u538b\u7f29\u5305\u6216\u5de5\u7a0b\u56fe\u7eb8\u7c7b\u6587\u4ef6\u5efa\u8bae\u4e0b\u8f7d\u540e\u4f7f\u7528 WPS\u3001Office\u3001CAD \u7b49\u4e13\u4e1a\u8f6f\u4ef6\u67e5\u770b\u3002',
    textPreview: '\u6587\u672c\u9884\u89c8',
    openDownload: '\u4e0b\u8f7d\u6587\u4ef6'
  }
}

const en = {
  common: {
    appName: 'EV Forklift Hub',
    community: 'Community',
    parts: 'Parts',
    knowledge: 'Knowledge',
    assistant: 'AI Assistant',
    service: 'Service Orders',
    cart: 'Cart',
    publish: 'Publish',
    publishPost: 'New Post',
    profile: 'Profile',
    orders: 'Orders',
    collections: 'Collections',
    logout: 'Log out',
    login: 'Log in',
    register: 'Sign up',
    menu: 'Menu',
    mine: 'Me',
    home: 'Home',
    cancel: 'Cancel',
    confirm: 'Confirm',
    delete: 'Delete',
    reply: 'Reply',
    submit: 'Submit',
    close: 'Close',
    download: 'Download',
    preview: 'Preview',
    loading: 'Loading',
    unknown: 'Unknown',
    noData: 'No data'
  },
  post: {
    title: 'Title',
    titlePlaceholder: 'Enter a post title',
    category: 'Category',
    content: 'Content',
    contentPlaceholder: 'Share the issue, repair context, or operating experience',
    tech: 'Technical',
    trouble: 'Troubleshooting',
    experience: 'Experience',
    other: 'Other',
    publishTime: 'Published',
    liked: 'Liked',
    like: 'Like',
    collected: 'Collected',
    collect: 'Collect',
    comments: 'Comments',
    writeComment: 'Write a comment...',
    publishComment: 'Post comment',
    loginToComment: 'Please log in before commenting',
    noComments: 'No comments yet',
    replyPlaceholder: 'Reply to {name}...',
    replyTo: 'Reply to {name}',
    showReplies: '{count} replies',
    fetchPostFailed: 'Failed to load post',
    fetchCommentsFailed: 'Failed to load comments',
    titleRequired: 'Please enter a title',
    contentRequired: 'Please enter content',
    publishSuccess: 'Published',
    commentRequired: 'Please enter a comment',
    commentSuccess: 'Comment posted',
    replySuccess: 'Reply posted',
    deleteConfirm: 'Delete this comment?',
    deleteTitle: 'Confirm',
    deleteSuccess: 'Deleted',
    deleteFailed: 'Delete failed',
    logoutSuccess: 'Logged out'
  },
  knowledge: {
    detail: 'Document Detail',
    noSummary: 'No summary',
    views: 'views',
    downloads: 'downloads',
    uploader: 'Uploader',
    unlockedTitle: 'Document unlocked',
    unlockedSubTitle: 'You can preview or download the full document',
    lockedTitle: 'Unlock this document to preview and download it',
    unlockMethod: 'Unlock method: {label}',
    currentPoints: 'Available points: {points}',
    pointsUnlock: 'Unlock with points ({points})',
    alipayUnlock: 'Unlock with Alipay (￥{money})',
    loginToUnlock: 'Log in to unlock',
    confirmPoints: 'Use {points} points to unlock this document?',
    pointsTitle: 'Points unlock',
    unlockSuccess: 'Unlocked',
    downloadStart: 'Download started',
    downloadFailed: 'Download failed',
    previewTitle: 'Online Preview',
    previewLoading: 'Loading preview...',
    previewFailed: 'Preview failed. Please download the file.',
    unsupportedTitle: 'This format cannot be previewed directly in the browser',
    unsupportedDesc: 'For Office, archive, or CAD files, download and open with WPS, Office, CAD, or a dedicated viewer.',
    textPreview: 'Text preview',
    openDownload: 'Download file'
  }
}

const messages = { zh, en }
const savedLocale = localStorage.getItem('efh-locale')
const locale = ref(savedLocale === 'en' ? 'en' : 'zh')

const format = (value, params = {}) => {
  if (typeof value !== 'string') return value
  return Object.keys(params).reduce((text, key) => text.replaceAll(`{${key}}`, params[key]), value)
}

const readMessage = (lang, key) => {
  return key.split('.').reduce((obj, part) => (obj ? obj[part] : undefined), messages[lang])
}

export const setLocale = (nextLocale) => {
  locale.value = nextLocale === 'en' ? 'en' : 'zh'
  localStorage.setItem('efh-locale', locale.value)
  document.documentElement.lang = locale.value === 'en' ? 'en' : 'zh-CN'
}

export const toggleLocale = () => {
  setLocale(locale.value === 'zh' ? 'en' : 'zh')
}

export const useI18n = () => {
  const t = (key, params) => {
    const value = readMessage(locale.value, key) ?? readMessage('zh', key) ?? key
    return format(value, params)
  }

  return {
    locale,
    localeLabel: computed(() => (locale.value === 'zh' ? '\u4e2d\u6587' : 'EN')),
    nextLocaleLabel: computed(() => (locale.value === 'zh' ? 'EN' : '\u4e2d\u6587')),
    t,
    setLocale,
    toggleLocale
  }
}

setLocale(locale.value)
