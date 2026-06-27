<template>
  <div class="page-container">
    <div class="cl-hero">
      <h1>登录日志</h1>
      <p>展示近期登录记录与异常登录提示</p>
    </div>
    <el-card>
      <el-table :data="logs" stripe>
        <el-table-column label="时间" prop="loginTime" min-width="180" />
        <el-table-column label="账号" prop="username" />
        <el-table-column label="IP" prop="ip" />
        <el-table-column label="设备" prop="device" show-overflow-tooltip />
        <el-table-column label="结果" width="120">
          <template #default="{ row }">
            <span :class="row.success ? 'cl-tag success' : 'cl-tag danger'">
              {{ row.success ? '成功' : '失败' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="失败原因" prop="failReason" min-width="160" />
      </el-table>
      <div class="pager">
        <el-pagination
          :current-page="page.current"
          :page-size="page.size"
          :total="page.total"
          @current-change="(p) => loadData(p)"
          layout="prev, pager, next, total"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { getLoginLogs } from '@/api/user'

const logs = ref([])
const page = reactive({ current: 1, size: 10, total: 0 })

async function loadData(p = 1) {
  const data = await getLoginLogs({ current: p, size: page.size })
  logs.value = data.records
  page.current = data.current
  page.total = data.total
}

onMounted(loadData)
</script>

<style scoped>
.pager { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
