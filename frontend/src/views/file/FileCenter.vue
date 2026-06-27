<template>
  <div class="page-container">
    <div class="cl-hero">
      <h1>文件中心</h1>
      <p>个人空间统一管理 - 上传、下载、清理</p>
    </div>

    <div style="margin-bottom: 16px">
      <el-upload
        :http-request="handleUpload"
        :show-file-list="false"
        accept="*"
      >
        <el-button class="cl-btn-hero">
          <el-icon><UploadFilled /></el-icon> 上传文件
        </el-button>
      </el-upload>
    </div>

    <el-card>
      <el-table :data="files" stripe>
        <el-table-column label="文件名" prop="originalName" min-width="220" />
        <el-table-column label="扩展名" prop="extension" width="100" />
        <el-table-column label="大小" width="120">
          <template #default="{ row }">{{ formatSize(row.sizeBytes) }}</template>
        </el-table-column>
        <el-table-column label="下载次数" prop="downloadCount" width="120" />
        <el-table-column label="上传时间" prop="createTime" width="170" />
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button text type="primary" @click="download(row)">下载</el-button>
            <el-button text type="danger" @click="remove(row)">删除</el-button>
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
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { uploadFile, deleteFile, myFiles } from '@/api/file'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const files = ref([])
const page = reactive({ current: 1, size: 10, total: 0 })

async function loadData(p = 1) {
  const data = await myFiles({ current: p, size: page.size })
  files.value = data.records
  page.current = data.current
  page.total = data.total
}

async function handleUpload({ file }) {
  const form = new FormData()
  form.append('file', file)
  form.append('scope', 'USER')
  try {
    await uploadFile(form)
    ElMessage.success('上传成功')
    await loadData()
  } catch (e) { /* 提示已在 axios */ }
}

async function download(row) {
  // 走带 token 的 fetch，避免直链丢失鉴权
  const res = await fetch(`/api/file/${row.id}/download`, {
    headers: { Authorization: `Bearer ${userStore.token}` }
  })
  if (!res.ok) { ElMessage.error('下载失败'); return }
  const blob = await res.blob()
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url; a.download = row.originalName; a.click()
  URL.revokeObjectURL(url)
}

async function remove(row) {
  await deleteFile(row.id)
  ElMessage.success('已删除')
  await loadData(page.current)
}

function formatSize(bytes) {
  if (!bytes) return '-'
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / 1024 / 1024).toFixed(2) + ' MB'
}

onMounted(loadData)
</script>

<style scoped>
.pager { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
