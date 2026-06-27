<template>
  <div class="page-container">
    <div class="cl-hero">
      <h1>消息中心</h1>
      <p>申请、审核、任务、公告统一收纳</p>
    </div>

    <div class="filter-bar">
      <el-select v-model="filters.type" placeholder="全部分类" clearable style="width: 160px">
        <el-option v-for="t in types" :key="t.value" :label="t.label" :value="t.value" />
      </el-select>
      <el-select v-model="filters.isRead" placeholder="全部状态" clearable style="width: 140px">
        <el-option label="未读" :value="0" />
        <el-option label="已读" :value="1" />
      </el-select>
      <el-input v-model="search" placeholder="关键词" style="width: 200px" clearable @keyup.enter="loadData(1)" />
      <el-button type="primary" @click="loadData(1)">检索</el-button>
      <el-button @click="markAll">全部已读</el-button>
      <el-button type="danger" plain :disabled="selected.length === 0" @click="batchRemove">批量删除</el-button>
    </div>

    <el-card>
      <el-table :data="messages" @selection-change="(rows) => selected = rows.map(r => r.id)">
        <el-table-column type="selection" width="50" />
        <el-table-column label="类型" width="100">
          <template #default="{ row }">
            <span class="cl-tag" :class="badge(row.type)">{{ row.type }}</span>
          </template>
        </el-table-column>
        <el-table-column label="内容" prop="content" show-overflow-tooltip />
        <el-table-column label="时间" prop="createTime" width="170" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <span :class="row.isRead ? 'cl-tag info' : 'cl-tag danger'">
              {{ row.isRead ? '已读' : '未读' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button v-if="!row.isRead" text type="primary" @click="onRead(row)">标记已读</el-button>
          </template>
        </el-table-column>
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
import { onMounted, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { listMessages, markRead, markAllRead, batchDeleteMsg, searchMessage } from '@/api/messageExtra'

const filters = reactive({ type: '', isRead: null })
const search = ref('')
const messages = ref([])
const selected = ref([])
const page = reactive({ current: 1, size: 10, total: 0 })

const types = [
  { label: '申请', value: 'APPLY' },
  { label: '审核', value: 'AUDIT' },
  { label: '任务', value: 'TASK' },
  { label: '公告', value: 'NOTICE' },
  { label: '摘要', value: 'DIGEST' }
]

function badge(type) {
  return { APPLY: 'info', AUDIT: 'success', TASK: 'warn', NOTICE: '', DIGEST: '' }[type] || ''
}

async function loadData(p = 1) {
  let data
  if (search.value) {
    data = await searchMessage({ current: p, size: page.size, keyword: search.value })
  } else {
    data = await listMessages({ current: p, size: page.size, type: filters.type || undefined, isRead: filters.isRead == null ? undefined : filters.isRead })
  }
  messages.value = data.records
  page.current = data.current
  page.total = data.total
}

async function onRead(row) {
  await markRead(row.id)
  await loadData(page.current)
}

async function markAll() {
  await markAllRead()
  ElMessage.success('已全部标记为已读')
  await loadData(page.current)
}

async function batchRemove() {
  await batchDeleteMsg(selected.value)
  ElMessage.success('已删除选中消息')
  selected.value = []
  await loadData(page.current)
}

watch(() => [filters.type, filters.isRead], () => loadData(1))
onMounted(() => loadData())
</script>

<style scoped>
.pager { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
