<template>
  <div class="mobile-entry efh-page">
    <EfhBackBar title="移动端安装入口" subtitle="Android 下载 APK，iOS 添加到桌面快捷方式" fallback="/" />

    <section class="entry-hero">
      <div>
        <span class="efh-kicker">MOBILE ACCESS</span>
        <h1>电动叉车协同平台移动端</h1>
        <p>移动端与网页端共用同一套后台数据。售后工单、备件采购、社区互动和 AI 助手均可联网使用。</p>
      </div>
      <el-button type="primary" size="large" @click="$router.push('/assistant')">打开 AI 助手</el-button>
    </section>

    <div class="entry-grid">
      <el-card class="platform-card">
        <template #header>
          <div class="platform-title">
            <el-icon><Iphone /></el-icon>
            <span>Android 安装包</span>
          </div>
        </template>
        <p>适用于安卓手机、平板和车间移动终端。下载后按系统提示安装。</p>
        <a class="apk-button" href="/downloads/ev-forklift-hub-debug.apk" download>
          下载 Android APK
        </a>
      </el-card>

      <el-card class="platform-card">
        <template #header>
          <div class="platform-title">
            <el-icon><Monitor /></el-icon>
            <span>iOS 桌面快捷方式</span>
          </div>
        </template>
        <p>iPhone 通过 Safari 打开本站后添加到主屏幕，效果接近独立 App，数据与网页端同步。</p>
        <el-button type="primary" plain @click="iosDialog = true">查看 iOS 操作指引</el-button>
      </el-card>
    </div>

    <el-dialog v-model="iosDialog" title="iOS 添加到主屏幕" width="520px">
      <ol class="ios-steps">
        <li>使用 Safari 打开：<strong>http://111.170.36.78:8888/</strong></li>
        <li>点击浏览器底部的分享按钮。</li>
        <li>选择“添加到主屏幕”。</li>
        <li>名称保持“电动叉车协同平台”，点击“添加”。</li>
      </ol>
      <template #footer>
        <el-button type="primary" @click="iosDialog = false">知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { Iphone, Monitor } from '@element-plus/icons-vue'
import EfhBackBar from '@/components/EfhBackBar.vue'

const iosDialog = ref(false)
</script>

<style scoped>
.entry-hero {
  display: flex;
  justify-content: space-between;
  gap: 18px;
  padding: 24px;
  margin-bottom: 16px;
  border: 1px solid var(--efh-border-light);
  border-radius: 8px;
  background: #fff;
}

.entry-hero h1 {
  margin: 0;
  font-size: 28px;
}

.entry-hero p,
.platform-card p {
  color: var(--efh-text-secondary);
  line-height: 1.7;
}

.entry-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.platform-title {
  display: flex;
  align-items: center;
  gap: 8px;
}

.apk-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 40px;
  padding: 0 16px;
  border-radius: 8px;
  background: var(--efh-primary);
  color: #fff;
  text-decoration: none;
  font-weight: 600;
}

.ios-steps {
  padding-left: 20px;
  line-height: 2;
}

@media (max-width: 768px) {
  .entry-hero,
  .entry-grid {
    grid-template-columns: 1fr;
  }

  .entry-hero {
    flex-direction: column;
  }
}
</style>
