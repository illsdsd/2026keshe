<template>
  <div>
    <h2 class="page-title">数据大盘</h2>

    <div class="stat-grid">
      <div class="stat-card hero">
        <div class="stat-card__label">用户数</div>
        <div class="stat-card__value">{{ data.userCount || 0 }}</div>
        <el-icon class="stat-card__icon"><User /></el-icon>
      </div>
      <div class="stat-card mint">
        <div class="stat-card__label">队伍数</div>
        <div class="stat-card__value">{{ data.teamCount || 0 }}</div>
        <el-icon class="stat-card__icon"><UserFilled /></el-icon>
      </div>
      <div class="stat-card sky">
        <div class="stat-card__label">竞赛数</div>
        <div class="stat-card__value">{{ data.competitionCount || 0 }}</div>
        <el-icon class="stat-card__icon"><Trophy /></el-icon>
      </div>
      <div class="stat-card amber">
        <div class="stat-card__label">待处理举报</div>
        <div class="stat-card__value">{{ data.reportPending || 0 }}</div>
        <el-icon class="stat-card__icon"><Warning /></el-icon>
      </div>
    </div>

    <div class="row">
      <el-card class="row-card">
        <template #header><strong>近 7 天注册趋势</strong></template>
        <EChart v-if="trendOption" :option="trendOption" height="280px" />
      </el-card>
      <el-card class="row-card">
        <template #header><strong>业务量分布</strong></template>
        <EChart v-if="distOption" :option="distOption" height="280px" />
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import EChart from '@/components/EChart.vue'
import { adminDashboard } from '@/api/admin'

const data = ref({})

const trendOption = computed(() => data.value.registerTrend && ({
  tooltip: { trigger: 'axis' },
  xAxis: { type: 'category', data: data.value.registerTrend.map(p => p.date) },
  yAxis: { type: 'value' },
  series: [{
    type: 'line', smooth: true, areaStyle: { color: 'rgba(79,70,229,0.18)' },
    lineStyle: { color: '#4f46e5' }, itemStyle: { color: '#4f46e5' },
    data: data.value.registerTrend.map(p => p.count)
  }]
}))

const distOption = computed(() => data.value.userCount != null && ({
  tooltip: { trigger: 'item' },
  legend: { bottom: 0 },
  series: [{
    type: 'pie', radius: ['40%', '70%'],
    data: [
      { value: data.value.userCount || 0, name: '用户', itemStyle: { color: '#4f46e5' } },
      { value: data.value.teamCount || 0, name: '队伍', itemStyle: { color: '#10b981' } },
      { value: data.value.taskCount || 0, name: '任务', itemStyle: { color: '#0ea5e9' } },
      { value: data.value.applyCount || 0, name: '申请', itemStyle: { color: '#f59e0b' } }
    ]
  }]
}))

onMounted(async () => { data.value = await adminDashboard() })
</script>

<style scoped>
.row { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
@media (max-width: 1000px) { .row { grid-template-columns: 1fr; } }
</style>
