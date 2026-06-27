<template>
  <div class="page-container">
    <el-page-header @back="$router.back()" title="返回">
      <template #content>队伍统计 #{{ teamId }}</template>
    </el-page-header>

    <div class="stat-grid">
      <div class="stat-card hero">
        <div class="stat-card__label">任务总数</div>
        <div class="stat-card__value">{{ stat.total || 0 }}</div>
      </div>
      <div class="stat-card mint">
        <div class="stat-card__label">已完成</div>
        <div class="stat-card__value">{{ stat.done || 0 }}</div>
      </div>
      <div class="stat-card sky">
        <div class="stat-card__label">进行中</div>
        <div class="stat-card__value">{{ stat.doing || 0 }}</div>
      </div>
      <div class="stat-card amber">
        <div class="stat-card__label">累计工时</div>
        <div class="stat-card__value">{{ stat.totalHours || 0 }}</div>
      </div>
    </div>

    <el-card>
      <template #header>
        <strong>完成率</strong>
        <el-button text type="primary" style="float: right" @click="exportXlsx">导出 Excel</el-button>
      </template>
      <EChart v-if="option" :option="option" height="320px" />
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import EChart from '@/components/EChart.vue'
import { teamStat } from '@/api/stat'
import { useUserStore } from '@/store/user'

const route = useRoute()
const userStore = useUserStore()
const teamId = route.params.id
const stat = ref({})

const option = computed(() => stat.value.total != null && ({
  tooltip: { trigger: 'item' },
  legend: { bottom: 0 },
  series: [{
    type: 'pie', radius: ['45%', '70%'],
    data: [
      { value: stat.value.todo || 0, name: '待办', itemStyle: { color: '#94a3b8' } },
      { value: stat.value.doing || 0, name: '进行中', itemStyle: { color: '#0ea5e9' } },
      { value: stat.value.done || 0, name: '已完成', itemStyle: { color: '#10b981' } }
    ]
  }]
}))

async function exportXlsx() {
  const res = await fetch(`/api/stat/team/${teamId}/export`, {
    headers: { Authorization: `Bearer ${userStore.token}` }
  })
  const blob = await res.blob()
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a'); a.href = url; a.download = `team-stat-${teamId}.xlsx`; a.click()
  URL.revokeObjectURL(url)
}

onMounted(async () => { stat.value = await teamStat(teamId) })
</script>
