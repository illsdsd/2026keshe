<template>
  <div class="page-container">
    <el-page-header @back="$router.back()" title="返回">
      <template #content>工时填报 #{{ teamId }}</template>
    </el-page-header>

    <el-card style="margin-top: 16px">
      <template #header><strong>填报工时</strong></template>
      <el-form :model="form" :inline="true">
        <el-form-item label="任务 id"><el-input v-model="form.taskId" /></el-form-item>
        <el-form-item label="日期"><el-date-picker v-model="form.workDate" /></el-form-item>
        <el-form-item label="工时"><el-input-number v-model="form.hours" :step="0.5" :min="0.5" :max="24" /></el-form-item>
        <el-form-item label="内容"><el-input v-model="form.content" /></el-form-item>
        <el-button type="primary" @click="submit">保存</el-button>
      </el-form>
    </el-card>

    <el-card style="margin-top: 16px">
      <template #header>
        <strong>队伍工时分布</strong>
        <el-button text type="primary" style="float: right" @click="exportXlsx">导出 Excel</el-button>
      </template>
      <EChart v-if="option" :option="option" height="320px" />
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import EChart from '@/components/EChart.vue'
import { logWork, teamWorklogStat } from '@/api/taskExtra'
import { useUserStore } from '@/store/user'

const route = useRoute()
const userStore = useUserStore()
const teamId = route.params.id
const form = reactive({ taskId: '', workDate: new Date().toISOString().slice(0, 10), hours: 1, content: '' })
const stat = ref([])

const option = computed(() => stat.value.length && ({
  tooltip: {},
  xAxis: { type: 'category', data: stat.value.map(s => '用户 ' + s.userId) },
  yAxis: { type: 'value', name: '小时' },
  series: [{
    type: 'bar', data: stat.value.map(s => Number(s.hours)),
    itemStyle: { color: '#4f46e5', borderRadius: [6, 6, 0, 0] }
  }]
}))

async function submit() {
  await logWork({ ...form, workDate: form.workDate?.toISOString?.().slice(0, 10) || form.workDate })
  ElMessage.success('已填报')
  await loadStat()
}

async function loadStat() { stat.value = await teamWorklogStat(teamId) }

async function exportXlsx() {
  const res = await fetch(`/api/worklog/team/${teamId}/export`, {
    headers: { Authorization: `Bearer ${userStore.token}` }
  })
  const blob = await res.blob()
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a'); a.href = url; a.download = `worklog-${teamId}.xlsx`; a.click()
  URL.revokeObjectURL(url)
}

onMounted(loadStat)
</script>
