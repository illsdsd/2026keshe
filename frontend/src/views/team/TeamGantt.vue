<template>
  <div class="page-container">
    <el-page-header @back="$router.back()" title="返回">
      <template #content>任务甘特图 #{{ teamId }}</template>
    </el-page-header>

    <el-card style="margin-top: 16px">
      <template #header><strong>任务时间轴</strong></template>
      <EChart v-if="option" :option="option" height="420px" />
      <div v-else class="empty">暂无任务时间数据</div>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import EChart from '@/components/EChart.vue'
import { listTeamTasks } from '@/api/task'

const route = useRoute()
const teamId = route.params.id
const tasks = ref([])

const option = computed(() => {
  if (!tasks.value.length) return null
  const data = tasks.value
    .filter(t => t.deadline)
    .map((t, idx) => ({
      name: t.title,
      value: [idx, +new Date(t.createTime), +new Date(t.deadline), t.status]
    }))
  const colors = { TODO: '#94a3b8', DOING: '#0ea5e9', DONE: '#10b981' }
  return {
    tooltip: {
      formatter: p => `${p.name}<br/>开始: ${new Date(p.value[1]).toLocaleDateString()}<br/>截止: ${new Date(p.value[2]).toLocaleDateString()}<br/>状态: ${p.value[3]}`
    },
    grid: { left: 80, right: 30, top: 30, bottom: 30 },
    xAxis: { type: 'time' },
    yAxis: { type: 'category', data: data.map(d => d.name) },
    series: [{
      type: 'custom',
      renderItem: (params, api) => {
        const start = api.coord([api.value(1), api.value(0)])
        const end = api.coord([api.value(2), api.value(0)])
        const height = 14
        return {
          type: 'rect',
          shape: { x: start[0], y: start[1] - height / 2, width: Math.max(end[0] - start[0], 4), height },
          style: { fill: colors[api.value(3)] || '#4f46e5' }
        }
      },
      encode: { x: [1, 2], y: 0 },
      data: data.map(d => d.value)
    }]
  }
})

onMounted(async () => { tasks.value = await listTeamTasks(teamId) })
</script>

<style scoped>
.empty { padding: 60px; text-align: center; color: var(--cl-text-muted); }
</style>
