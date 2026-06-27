<template>
  <div>
    <h2 class="page-title">举报处理</h2>
    <div class="filter-bar">
      <el-select v-model="status" placeholder="状态" clearable style="width: 160px">
        <el-option label="待处理" value="PENDING" />
        <el-option label="已处理" value="HANDLED" />
        <el-option label="已驳回" value="REJECTED" />
      </el-select>
      <el-button type="primary" @click="loadData(1)">查询</el-button>
    </div>
    <el-card>
      <el-table :data="reports" stripe>
        <el-table-column label="ID" prop="id" width="80" />
        <el-table-column label="举报对象">
          <template #default="{ row }">{{ row.targetType }} #{{ row.targetId }}</template>
        </el-table-column>
        <el-table-column label="原因" prop="reason" show-overflow-tooltip />
        <el-table-column label="举报人" prop="reporterId" width="100" />
        <el-table-column label="状态" prop="status" width="100" />
        <el-table-column label="举报时间" prop="createTime" width="170" />
        <el-table-column label="操作" width="220">
          <template #default="{ row }">
            <template v-if="row.status === 'PENDING'">
              <el-button text type="success" @click="handle(row, true)">处理</el-button>
              <el-button text type="danger" @click="handle(row, false)">驳回</el-button>
            </template>
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
import { adminReports, adminHandleReport } from '@/api/admin'

const status = ref('')
const reports = ref([])
const page = reactive({ current: 1, size: 10, total: 0 })

async function loadData(p = 1) {
  const data = await adminReports({ status: status.value || undefined, current: p, size: page.size })
  reports.value = data.records
  page.current = data.current
  page.total = data.total
}

async function handle(row, handled) {
  const { value } = await ElMessageBox.prompt(handled ? '处理备注' : '驳回理由', '处理举报', { inputValue: '' })
  await adminHandleReport(row.id, { handled, remark: value })
  ElMessage.success('已处理')
  await loadData(page.current)
}

onMounted(loadData)
</script>

<style scoped>
.pager { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
