<template>
  <div class="page-container">
    <div class="cl-hero">
      <h1>个人数据</h1>
      <p>参赛、任务、信誉与能力雷达一目了然</p>
    </div>

    <div class="stat-grid">
      <div class="stat-card hero">
        <div class="stat-card__label">参赛 / 加入队伍</div>
        <div class="stat-card__value">{{ stat.competitionCount || 0 }}</div>
        <el-icon class="stat-card__icon"><Trophy /></el-icon>
      </div>
      <div class="stat-card mint">
        <div class="stat-card__label">已完成任务</div>
        <div class="stat-card__value">{{ stat.doneTaskCount || 0 }}</div>
        <el-icon class="stat-card__icon"><Check /></el-icon>
      </div>
      <div class="stat-card sky">
        <div class="stat-card__label">信誉分</div>
        <div class="stat-card__value">{{ stat.reputation }}</div>
        <el-icon class="stat-card__icon"><Medal /></el-icon>
      </div>
      <div class="stat-card amber">
        <div class="stat-card__label">申请通过率</div>
        <div class="stat-card__value">{{ (stat.approveRate * 100).toFixed(0) }}%</div>
        <el-icon class="stat-card__icon"><DataLine /></el-icon>
      </div>
    </div>

    <div class="row">
      <el-card class="row-card">
        <template #header><strong>能力雷达图</strong></template>
        <EChart v-if="radarOption" :option="radarOption" height="320px" />
      </el-card>
      <el-card class="row-card">
        <template #header><strong>申请统计</strong></template>
        <EChart v-if="applyOption" :option="applyOption" height="320px" />
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import EChart from '@/components/EChart.vue'
import { userStat, userRadar } from '@/api/stat'
import { applyStat } from '@/api/taskExtra'

const stat = ref({ approveRate: 0 })
const radar = ref(null)
const applyData = ref(null)

const radarOption = computed(() => radar.value && ({
  tooltip: {},
  radar: {
    indicator: radar.value.dimensions.map(name => ({ name, max: 5 })),
    radius: '60%',
    splitArea: { areaStyle: { color: ['rgba(79,70,229,0.04)', 'rgba(79,70,229,0.08)'] } }
  },
  series: [{
    type: 'radar', areaStyle: { color: 'rgba(79,70,229,0.25)' },
    lineStyle: { color: '#4f46e5' }, itemStyle: { color: '#4f46e5' },
    data: [{ value: radar.value.values, name: '能力' }]
  }]
}))

const applyOption = computed(() => applyData.value && ({
  tooltip: { trigger: 'item' },
  legend: { bottom: 0 },
  series: [{
    type: 'pie', radius: ['45%', '70%'],
    data: [
      { value: applyData.value.approved || 0, name: '通过', itemStyle: { color: '#10b981' } },
      { value: applyData.value.rejected || 0, name: '拒绝', itemStyle: { color: '#ef4444' } },
      { value: applyData.value.pending || 0, name: '待审核', itemStyle: { color: '#f59e0b' } }
    ]
  }]
}))

onMounted(async () => {
  stat.value = await userStat()
  radar.value = await userRadar()
  applyData.value = await applyStat()
})
</script>

<style scoped>
.row { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
@media (max-width: 900px) { .row { grid-template-columns: 1fr; } }
</style>
