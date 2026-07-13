<template>
  <div class="home-page">
    <section class="hero-panel">
      <div class="hero-copy">
        <span class="efh-kicker">EV FORKLIFT OPERATIONS</span>
        <h1>新能源叉车业务协同中台</h1>
        <p>面向车企售后场景，把维保工单、备件采购、知识库、运营社区和 AI 助手组织成清晰的业务入口。</p>
        <div class="hero-actions">
          <el-button type="primary" size="large" @click="$router.push('/assistant')">
            <el-icon><ChatDotRound /></el-icon>
            AI 问答
          </el-button>
          <el-button size="large" @click="$router.push('/service')">创建工单</el-button>
        </div>
      </div>
      <div class="hero-metrics">
        <div>
          <strong>4</strong>
          <span>核心业务模块</span>
        </div>
        <div>
          <strong>AI</strong>
          <span>知识检索与业务协同</span>
        </div>
        <div>
          <strong>App</strong>
          <span>移动端独立入口</span>
        </div>
      </div>
    </section>

    <section class="module-grid efh-section">
      <button v-for="item in businessModules" :key="item.path" type="button" @click="$router.push(item.path)">
        <el-icon><component :is="item.icon" /></el-icon>
        <strong>{{ item.title }}</strong>
        <span>{{ item.desc }}</span>
      </button>
    </section>

    <section class="flow-section efh-section">
      <div>
        <span class="efh-kicker">BUSINESS FLOW</span>
        <h2>从问题到闭环</h2>
      </div>
      <div class="flow-grid">
        <div v-for="step in flowSteps" :key="step.title" class="flow-step">
          <el-icon><component :is="step.icon" /></el-icon>
          <strong>{{ step.title }}</strong>
          <span>{{ step.desc }}</span>
        </div>
      </div>
    </section>

    <button type="button" class="assistant-fab" @click="$router.push('/assistant')">
      <el-icon><ChatDotRound /></el-icon>
      AI
    </button>
  </div>
</template>

<script setup>
import { ChatDotRound, Connection, House, Reading, ShoppingBag, Tools, Van, Iphone } from '@element-plus/icons-vue'

const businessModules = [
  { title: '售后工单', desc: '报修、派工、进度跟踪、服务闭环', path: '/service', icon: Tools },
  { title: '备件采购', desc: '选型、下单、支付、物流追踪', path: '/parts', icon: ShoppingBag },
  { title: '运营社区', desc: '技术交流、保养经验、维修案例', path: '/community', icon: House },
  { title: 'AI 助手', desc: 'RAG 检索、业务工具调用、问答辅助', path: '/assistant', icon: ChatDotRound },
  { title: '知识库', desc: '常规维修案例和电池保养资料', path: '/knowledge', icon: Reading },
  { title: '电池保养', desc: '铅酸电池与锂电池专题维护', path: '/battery-care', icon: Van },
  { title: '移动端入口', desc: 'Android APK 与 iOS 桌面快捷方式', path: '/mobile', icon: Iphone }
]

const flowSteps = [
  { title: '提交问题', desc: '售后工单或社区发帖记录现场问题', icon: Tools },
  { title: 'AI 辅助', desc: '检索知识库并调用业务工具给出建议', icon: ChatDotRound },
  { title: '采购备件', desc: '备件商城完成下单、支付和物流跟踪', icon: ShoppingBag },
  { title: '经验沉淀', desc: '把维修案例沉淀到社区和知识库', icon: Connection }
]
</script>

<style scoped>
.home-page {
  max-width: 1120px;
  margin: 0 auto;
}

.hero-panel {
  display: grid;
  grid-template-columns: minmax(0, 1.6fr) minmax(280px, 0.8fr);
  gap: 18px;
  padding: 28px;
  margin-bottom: 16px;
  border: 1px solid var(--efh-border-light);
  border-radius: 8px;
  background: #fff;
}

.hero-copy h1 {
  margin: 0;
  font-size: 32px;
  line-height: 1.2;
}

.hero-copy p {
  max-width: 680px;
  margin: 12px 0 0;
  color: var(--efh-text-secondary);
  line-height: 1.8;
}

.hero-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin-top: 20px;
}

.hero-metrics {
  display: grid;
  gap: 10px;
}

.hero-metrics div {
  padding: 16px;
  border: 1px solid var(--efh-border-light);
  border-radius: 8px;
  background: #f8fafc;
}

.hero-metrics strong {
  display: block;
  font-size: 26px;
  line-height: 1.1;
  color: var(--efh-primary);
}

.hero-metrics span,
.module-grid span,
.flow-step span {
  color: var(--efh-text-secondary);
  font-size: 13px;
}

.module-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.module-grid button {
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-height: 128px;
  padding: 16px;
  border: 1px solid var(--efh-border-light);
  border-radius: 8px;
  background: #fff;
  text-align: left;
  cursor: pointer;
}

.module-grid .el-icon,
.flow-step .el-icon {
  color: var(--efh-primary);
  font-size: 24px;
}

.flow-section {
  padding: 22px;
  border: 1px solid var(--efh-border-light);
  border-radius: 8px;
  background: #fff;
}

.flow-section h2 {
  margin: 0;
}

.flow-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
  margin-top: 16px;
}

.flow-step {
  display: grid;
  gap: 8px;
  padding: 14px;
  border: 1px solid var(--efh-border-light);
  border-radius: 8px;
}

.assistant-fab {
  position: fixed;
  right: 24px;
  bottom: 88px;
  z-index: 800;
  width: 56px;
  height: 56px;
  border: none;
  border-radius: 50%;
  background: var(--efh-primary);
  color: #fff;
  box-shadow: var(--efh-shadow-lg);
  cursor: pointer;
  font-weight: 700;
}

@media (max-width: 900px) {
  .hero-panel,
  .module-grid,
  .flow-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .hero-panel,
  .module-grid,
  .flow-grid {
    grid-template-columns: 1fr;
  }

  .hero-panel {
    padding: 20px;
  }

  .hero-copy h1 {
    font-size: 24px;
  }
}
</style>
