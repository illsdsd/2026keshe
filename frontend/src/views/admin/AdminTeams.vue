<template>
  <div>
    <h2 class="page-title">队伍管理</h2>
    <div class="filter-bar">
      <el-input v-model="filters.keyword" placeholder="队伍名" style="width: 240px" clearable @keyup.enter="loadData(1)" />
      <el-select v-model="filters.status" placeholder="状态" clearable style="width: 160px">
        <el-option v-for="s in statuses" :key="s.value" :label="s.label" :value="s.value" />
      </el-select>
      <el-button type="primary" @click="loadData(1)">查询</el-button>
    </div>
    <el-card>
      <el-table :data="teams" stripe>
        <el-table-column label="ID" prop="id" width="80" />
        <el-table-column label="队伍名" prop="name" />
        <el-table-column label="队长" prop="leaderId" width="80" />
        <el-table-column label="人数">
          <template #default="{ row }">{{ row.currentSize }}/{{ row.totalSize }}</template>
        </el-table-column>
        <el-table-column label="学院" prop="college" />
        <el-table-column label="状态" prop="status" width="120" />
        <el-table-column label="创建时间" prop="createTime" width="170" />
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button text type="danger" @click="disband(row)">强制解散</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pager">
        <el-pagination :current-page="page.current" :page-size="page.size" :total="page.total" @current-change="(p) => loadData(p)" layout="prev, pager, next, total" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminTeams, adminDisbandTeam } from '@/api/admin'

const filters = reactive({ keyword: '', status: '' })
const teams = ref([])
const page = reactive({ current: 1, size: 10, total: 0 })
const statuses = [
  { label: '招募中', value: 'RECRUITING' },
  { label: '已满员', value: 'FULL' },
  { label: '已关闭', value: 'CLOSED' },
  { label: '已归档', value: 'ARCHIVED' }
]

async function loadData(p = 1) {
  const data = await adminTeams({ ...filters, current: p, size: page.size })
  teams.value = data.records
  page.current = data.current
  page.total = data.total
}

async function disband(row) {
  await ElMessageBox.confirm(`确认强制解散队伍「${row.name}」？`, '提示', { type: 'warning' })
  await adminDisbandTeam(row.id)
  ElMessage.success('已解散')
  await loadData(page.current)
}

onMounted(loadData)
</script>

<style scoped>
.pager { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
