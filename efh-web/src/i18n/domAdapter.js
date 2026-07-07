import { nextTick, watch } from 'vue'

const zhToEn = {
  '电动叉车协同平台': 'EV Forklift Hub',
  '电叉协同平台': 'EV Forklift Hub',
  '叉车社区': 'Forklift Community',
  '新能源叉车 · 配件 · 知识 · 服务': 'EV forklifts · Parts · Knowledge · Service',
  '加入社区，共享叉车知识与经验': 'Join the community and share forklift knowledge',
  '运营社区': 'Community',
  '备件采购': 'Parts Procurement',
  '知识库': 'Knowledge Base',
  'AI助手': 'AI Assistant',
  'AI 助手': 'AI Assistant',
  '维保工单': 'Service Orders',
  '采购车': 'Procurement Cart',
  '发帖': 'Post',
  '发布': 'Publish',
  '发布帖子': 'New Post',
  '个人中心': 'Profile',
  '我的订单': 'My Orders',
  '我的收藏': 'My Collections',
  '退出登录': 'Log out',
  '登录': 'Log in',
  '注册': 'Sign up',
  '菜单': 'Menu',
  '首页': 'Home',
  '我的': 'Me',
  '取消': 'Cancel',
  '确定': 'Confirm',
  '保存': 'Save',
  '删除': 'Delete',
  '编辑信息': 'Edit Profile',
  '标题': 'Title',
  '分类': 'Category',
  '内容': 'Content',
  '技术交流': 'Technical',
  '故障求助': 'Troubleshooting',
  '经验分享': 'Experience',
  '其他': 'Other',
  '发布时间': 'Published',
  '点赞': 'Like',
  '已赞': 'Liked',
  '收藏': 'Collect',
  '已收藏': 'Collected',
  '评论': 'Comments',
  '发表评论': 'Post Comment',
  '回复': 'Reply',
  '暂无评论': 'No comments yet',
  '暂无帖子': 'No posts yet',
  '新能源叉车业务协同中台': 'EV Forklift Business Collaboration Hub',
  '把运营社区、备件采购、维保工单、知识库和 AI 助手串成一条车企售后业务链路。': 'Connect community, parts procurement, service orders, knowledge base, and AI assistance into one aftersales workflow.',
  '业务闭环': 'Business Loop',
  '下单-支付-物流': 'Order-Pay-Logistics',
  '核心场景': 'Core Scenario',
  '车企售后': 'OEM Aftersales',
  'AI 能力': 'AI Capability',
  '故障问答': 'Fault Q&A',
  '知识沉淀': 'Knowledge Assets',
  '维修案例': 'Repair Cases',
  '全部': 'All',
  '技术知识库': 'Technical Knowledge Base',
  '维修手册、技术规范、培训资料，支持积分或付费解锁': 'Repair manuals, specifications, and training materials with point or paid unlocks.',
  '管理文档': 'Manage Documents',
  '搜索': 'Search',
  '暂无摘要': 'No summary',
  '浏览': 'Views',
  '查看详情': 'View Detail',
  '暂无文档': 'No documents',
  '文档详情': 'Document Detail',
  '在线查看': 'Preview',
  '下载': 'Download',
  '文档已解锁': 'Document Unlocked',
  '你可以在线查看或下载完整文档': 'You can preview or download the full document.',
  '该文档需要解锁后才能查看和下载': 'Unlock this document to preview and download it.',
  '登录后解锁': 'Log in to Unlock',
  '积分解锁': 'Unlock with Points',
  '在线预览': 'Online Preview',
  '该格式暂不支持浏览器内直接预览': 'This format cannot be previewed in the browser',
  '下载文件': 'Download File',
  '知识库管理': 'Knowledge Admin',
  '返回知识库': 'Back to Knowledge',
  '上传新文档': 'Upload New Document',
  '文档标题': 'Document Title',
  '选择分类': 'Select Category',
  '摘要': 'Summary',
  '访问方式': 'Access',
  '免费': 'Free',
  '积分解锁': 'Points',
  '付费解锁': 'Paid',
  '积分或付费': 'Points or Paid',
  '积分价格': 'Point Price',
  '现金价格': 'Cash Price',
  '文档文件': 'Document File',
  '选择文件': 'Choose File',
  '上传（草稿）': 'Upload Draft',
  '文档列表': 'Documents',
  '草稿': 'Draft',
  '已发布': 'Published',
  '已下架': 'Offline',
  '访问': 'Access',
  '状态': 'Status',
  '操作': 'Actions',
  '下架': 'Offline',
  '配件管理': 'Parts Admin',
  '返回配件商城': 'Back to Parts',
  '上架新配件': 'Add Part',
  '配件名称': 'Part Name',
  '品牌': 'Brand',
  '型号': 'Model',
  '价格': 'Price',
  '库存': 'Stock',
  '描述': 'Description',
  '图片 URL': 'Image URL',
  '上架配件': 'Publish Part',
  '重置': 'Reset',
  '已上架配件': 'Published Parts',
  '名称': 'Name',
  '销量': 'Sales',
  '上架': 'Online',
  '下架': 'Offline',
  '备件批量采购': 'Bulk Parts Procurement',
  '统一勾选需要补库或抢修的备件，提交后按供应商拆单履约。': 'Select parts for replenishment or urgent repairs; orders are fulfilled by supplier.',
  '去结算': 'Checkout',
  '备件': 'Part',
  '单价': 'Unit Price',
  '数量': 'Quantity',
  '小计': 'Subtotal',
  '移除': 'Remove',
  '采购车暂无备件': 'No parts in cart',
  '去选备件': 'Browse Parts',
  '已选': 'Selected',
  '合计': 'Total',
  '确认收货信息与备件清单': 'Confirm Shipping and Parts',
  '订单确认': 'Order Confirmation',
  '收货地点': 'Shipping Address',
  '默认': 'Default',
  '暂无收货地址': 'No shipping address',
  '新增收货地址': 'Add Address',
  '备件清单': 'Parts List',
  '履约备注': 'Fulfillment Notes',
  '金额确认': 'Amount',
  '商品总额': 'Goods Total',
  '运费': 'Shipping',
  '应付金额': 'Amount Due',
  '提交订单': 'Submit Order',
  '收货人': 'Receiver',
  '手机号': 'Phone',
  '省份': 'Province',
  '城市': 'City',
  '区县': 'District',
  '详细地址': 'Detail Address',
  '默认地址': 'Default Address',
  '订单详情': 'Order Detail',
  '提交订单': 'Submit Order',
  '付款成功': 'Paid',
  '仓库发货': 'Warehouse Shipped',
  '签收完成': 'Delivered',
  '支付确认': 'Payment',
  '订单已创建，等待企业支付': 'Order created, waiting for enterprise payment',
  '订单号': 'Order No.',
  '支付宝网页支付': 'Alipay Web',
  '演示支付': 'Demo Pay',
  '跳转支付宝支付': 'Pay with Alipay',
  '确认演示支付': 'Confirm Demo Pay',
  '稍后支付，查看订单': 'Pay Later, View Orders',
  '真实接入建议': 'Production Integration',
  '支付成功': 'Payment Successful',
  '订单已支付，商家正在安排发货': 'Order paid. Merchant is arranging shipment.',
  '查看订单': 'View Order',
  '继续购物': 'Continue Shopping',
  '支付失败': 'Payment Failed',
  '请返回订单重新支付': 'Return to orders and pay again.',
  '等待支付结果': 'Waiting for Payment Result',
  '正在确认支付状态，请稍候...': 'Confirming payment status, please wait...',
  '备件详情': 'Part Detail',
  '交付方式': 'Delivery',
  '仓库发货 / 运单录入': 'Warehouse shipping / Tracking entry',
  '售后支持': 'Aftersales',
  '维保工单联动': 'Linked service orders',
  '采购价': 'Purchase Price',
  '可用库存': 'Available Stock',
  '累计采购': 'Total Sales',
  '适配建议': 'Fitment Advice',
  '采购数量': 'Purchase Quantity',
  '立即采购': 'Buy Now',
  '加入采购车': 'Add to Cart',
  '叉车 AI 助手': 'Forklift AI Assistant',
  '基于知识库与社区内容的智能问答（RAG）': 'RAG Q&A based on knowledge base and community content.',
  '社区': 'Community',
  '你好，我是新能源叉车 AI 助手': 'Hi, I am your EV forklift AI assistant',
  '我可以帮你检索知识库文档和社区讨论，回答电池、维修、保养等问题。': 'I can search documents and discussions to answer battery, repair, and maintenance questions.',
  '参考来源': 'Sources',
  '需解锁': 'Locked',
  '正在检索资料并生成回答...': 'Searching and generating an answer...',
  '发送': 'Send',
  '维修服务': 'Repair Service',
  '预约维修': 'Book Service',
  '我的订单': 'My Orders',
  '服务介绍': 'Service Intro',
  '工单号': 'Work Order No.',
  '服务类型': 'Service Type',
  '问题描述': 'Issue Description',
  '创建时间': 'Created At',
  '查看': 'View',
  '暂无订单': 'No orders',
  '故障诊断': 'Fault Diagnosis',
  '专业技师上门诊断叉车故障，快速定位问题': 'Technicians diagnose faults onsite and locate issues quickly.',
  '维修保养': 'Repair & Maintenance',
  '定期保养维护，延长叉车使用寿命': 'Regular maintenance to extend forklift service life.',
  '紧急救援': 'Emergency Support',
  '24小时紧急救援服务，快速响应': '24-hour emergency response.',
  '联系电话': 'Contact Phone',
  '地址': 'Address',
  '欢迎回来': 'Welcome Back',
  '验证码登录': 'SMS Login',
  '密码登录': 'Password Login',
  '获取验证码': 'Get Code',
  '还没有账号？': 'No account?',
  '立即注册': 'Sign up now',
  '创建账号': 'Create Account',
  '用户名': 'Username',
  '密码': 'Password',
  '昵称': 'Nickname',
  '已有账号？': 'Already have an account?',
  '立即登录': 'Log in now',
  '发布帖子': 'Posts',
  '发表评论': 'Comments',
  '可用积分': 'Available Points',
  '总积分': 'Total Points',
  '我的帖子': 'My Posts',
  '我的评论': 'My Comments',
  '购买积分': 'Buy Points',
  '积分兑换': 'Redeem Points',
  '兑换记录': 'Redeem History',
  '当前可用积分': 'Available Points',
  '模拟支付，购买后积分立即到账': 'Demo payment. Points arrive immediately.',
  '购买': 'Buy',
  '兑换': 'Redeem',
  '物品名称': 'Item',
  '消耗积分': 'Points Cost',
  '兑换时间': 'Redeem Time',
  '待处理': 'Pending',
  '已接单': 'Accepted',
  '服务中': 'In Service',
  '已完成': 'Completed',
  '已取消': 'Cancelled',
  '待发货': 'Pending Shipment'
}

const attrNames = ['placeholder', 'title', 'aria-label']
let observer
let applying = false

const originalText = new WeakMap()
const originalAttrs = new WeakMap()
const hasCjk = (text) => /[\u4e00-\u9fff]/.test(text || '')

const replaceText = (text) => {
  if (!text || !text.trim()) return text
  let next = text
  Object.keys(zhToEn)
    .sort((a, b) => b.length - a.length)
    .forEach((zh) => {
      next = next.split(zh).join(zhToEn[zh])
    })
  return next
}

const walk = (node, locale) => {
  if (!node) return
  if (node.nodeType === Node.TEXT_NODE) {
    if (locale === 'en') {
      if (!originalText.has(node) && hasCjk(node.nodeValue)) originalText.set(node, node.nodeValue)
      if (!originalText.has(node)) return
      node.nodeValue = replaceText(originalText.get(node))
    } else if (originalText.has(node)) {
      node.nodeValue = originalText.get(node)
    }
    return
  }

  if (node.nodeType !== Node.ELEMENT_NODE) return
  const element = node
  if (element.closest?.('[data-i18n-skip]')) return

  attrNames.forEach((name) => {
    if (!element.hasAttribute(name)) return
    let attrs = originalAttrs.get(element)
    if (!attrs) {
      attrs = {}
      originalAttrs.set(element, attrs)
    }
    if (attrs[name] == null && hasCjk(element.getAttribute(name))) attrs[name] = element.getAttribute(name)
    if (attrs[name] == null) return
    element.setAttribute(name, locale === 'en' ? replaceText(attrs[name]) : attrs[name])
  })

  element.childNodes.forEach((child) => walk(child, locale))
}

export const installDomI18n = (localeRef) => {
  const apply = async () => {
    if (applying) return
    applying = true
    await nextTick()
    walk(document.body, localeRef.value)
    applying = false
  }

  watch(localeRef, apply, { immediate: true })

  observer = new MutationObserver(() => {
    if (!applying) apply()
  })
  observer.observe(document.body, {
    childList: true,
    subtree: true,
    characterData: true,
    attributes: true,
    attributeFilter: attrNames
  })
}

export const uninstallDomI18n = () => {
  observer?.disconnect()
}
