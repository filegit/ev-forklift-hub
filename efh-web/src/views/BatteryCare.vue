<template>
  <div class="battery-care efh-page">
    <EfhBackBar title="电池保养专区" subtitle="铅酸电池与锂电池分场景维护" fallback="/" />

    <el-card class="care-shell">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="铅酸电池保养" name="lead">
          <div class="care-grid">
            <article v-for="item in leadAcidItems" :key="item.title" class="care-card">
              <img :src="item.image" :alt="item.title" />
              <div>
                <h3>{{ item.title }}</h3>
                <p>{{ item.desc }}</p>
                <strong>{{ item.tip }}</strong>
              </div>
            </article>
          </div>
        </el-tab-pane>

        <el-tab-pane label="锂电池保养" name="lithium">
          <el-alert
            class="lithium-tip"
            type="warning"
            show-icon
            :closable="false"
            title="锂电池保养重点：不要私自拆包，不要在高温、低温或异常告警状态下强行充放电。"
          />
          <div class="care-grid">
            <article v-for="item in lithiumItems" :key="item.title" class="care-card">
              <img :src="item.image" :alt="item.title" />
              <div>
                <h3>{{ item.title }}</h3>
                <p>{{ item.desc }}</p>
                <strong>{{ item.tip }}</strong>
              </div>
            </article>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import EfhBackBar from '@/components/EfhBackBar.vue'

const activeTab = ref('lead')
const leadAcidItems = [
  {
    title: '补水',
    image: 'https://images.unsplash.com/photo-1621905251189-08b45d6a269e?auto=format&fit=crop&w=800&q=80',
    desc: '定期检查电解液液位，使用蒸馏水补充，避免极板长期暴露。',
    tip: '重点：补水应在充电完成后冷却状态进行。'
  },
  {
    title: '均衡充电',
    image: 'https://images.unsplash.com/photo-1581092921461-eab62e97a780?auto=format&fit=crop&w=800&q=80',
    desc: '按周期执行均衡充电，降低单体电压差，减少容量衰减。',
    tip: '重点：均衡期间保持现场通风并监控温升。'
  },
  {
    title: '通风存放',
    image: 'https://images.unsplash.com/photo-1565608438257-fac3c27beb36?auto=format&fit=crop&w=800&q=80',
    desc: '存放环境保持干燥通风，远离明火和高温区域。',
    tip: '重点：长期停放需定期补电，避免深度亏电。'
  }
]

const lithiumItems = [
  {
    title: 'BMS 告警查看',
    image: 'https://images.unsplash.com/photo-1581092160607-ee22621dd758?auto=format&fit=crop&w=800&q=80',
    desc: '班前检查 BMS 告警码、电压差、SOC 和历史异常记录。',
    tip: '重点：出现绝缘、过温、压差异常时先停用排查。'
  },
  {
    title: '温度管控',
    image: 'https://images.unsplash.com/photo-1581092335878-2d9ff86ca2bf?auto=format&fit=crop&w=800&q=80',
    desc: '避免高温暴晒和低温大倍率充电，必要时启用预热或冷却策略。',
    tip: '重点：温度异常比单次续航下降更需要优先处理。'
  },
  {
    title: '规范充放电',
    image: 'https://images.unsplash.com/photo-1593941707882-a5bba14938c7?auto=format&fit=crop&w=800&q=80',
    desc: '使用匹配充电机，避免长期满电闲置和频繁深度放电。',
    tip: '重点：充电插头、线束和接口温升需要巡检。'
  }
]
</script>

<style scoped>
.care-shell {
  overflow: hidden;
}

.lithium-tip {
  margin-bottom: 16px;
}

.care-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.care-card {
  border: 1px solid var(--efh-border-light);
  border-radius: 8px;
  overflow: hidden;
  background: #fff;
}

.care-card img {
  width: 100%;
  aspect-ratio: 16 / 9;
  object-fit: cover;
}

.care-card div {
  padding: 14px;
}

.care-card h3 {
  margin: 0 0 8px;
}

.care-card p {
  color: var(--efh-text-secondary);
  line-height: 1.7;
}

.care-card strong {
  display: block;
  margin-top: 10px;
  color: #b45309;
}

@media (max-width: 900px) {
  .care-grid {
    grid-template-columns: 1fr;
  }
}
</style>
