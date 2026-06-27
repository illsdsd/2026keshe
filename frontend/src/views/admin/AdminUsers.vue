<template>
  <div>
    <h2 class="page-title">用户管理</h2>
    <div class="filter-bar">
      <el-input v-model="filters.keyword" placeholder="昵称 / 账号 / 邮箱" style="width: 240px" clearable @keyup.enter="loadData(1)" />
      <el-select v-model="filters.role" placeholder="角色" clearable style="width: 160px">
        <el-option label="学生" value="STUDENT" />
        <el-option label="管理员" value="ADMIN" />
      </el-select>
      <el-button type="primary" @click="loadData(1)">查询</el-button>
      <el-button @click="exportExcel">导出 Excel</el-button>
    </div>

    <el-card>
      <el-table :data="users" stripe>
        <el-table-column label="ID" prop="id" width="80" />
        <el-table-column label="账号" prop="username" />
        <el-table-column label="昵称" prop="nickname" />
        <el-table-column label="邮箱" prop="email" show-overflow-tooltip />
        <el-table-column label="学院" prop="college" />
        <el-table-column label="角色" prop="role" width="100" />
        <el-table-column label="信誉分" prop="reputation" width="100" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <span :class="row.enabled ? 'cl-tag success' : 'cl-tag danger'">
              {{ row.enabled ? '正常' : '禁用' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.enabled" text type="danger" @click="disable(row)">禁用</el-button>
            <el-button v-else text type="success" @click="enable(row)">启用</el-button>
            <el-button text type="primary" @click="reset(row)">重置密码</el-button>
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
import { adminUsers, adminDisableUser, adminEnableUser, adminResetPwd } from '@/api/admin'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const filters = reactive({ keyword: '', role: '' })
const users = ref([])
const page = reactive({ current: 1, size: 10, total: 0 })

async function loadData(p = 1) {
  const data = await adminUsers({ ...filters, current: p, size: page.size })
  users.value = data.records
  page.current = data.current
  page.total = data.total
}

async function disable(row) {
  await ElMessageBox.confirm(`确认禁用用户 ${row.username}？`, '提示', { type: 'warning' })
  await adminDisableUser(row.id)
  ElMessage.success('已禁用')
  await loadData(page.current)
}

async function enable(row) {
  await adminEnableUser(row.id)
  ElMessage.success('已启用')
  await loadData(page.current)
}

async function reset(row) {
  const { value } = await ElMessageBox.prompt('请输入新密码', '重置密码', { inputValue: '123456' })
  await adminResetPwd(row.id, { newPassword: value })
  ElMessage.success('已重置')
}

async function exportExcel() {
  const res = await fetch('/api/admin/users/export', {
    headers: { Authorization: `Bearer ${userStore.token}` }
  })
  const blob = await res.blob()
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url; a.download = 'users.xlsx'; a.click()
  URL.revokeObjectURL(url)
}

onMounted(loadData)
</script>

<style scoped>
.pager { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
