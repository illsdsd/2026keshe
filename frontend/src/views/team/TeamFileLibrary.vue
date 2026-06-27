<template>
  <div class="page-container">
    <el-page-header @back="$router.back()" title="返回">
      <template #content>队伍文件库 #{{ teamId }}</template>
    </el-page-header>

    <div class="action-bar">
      <el-upload :http-request="onUpload" :show-file-list="false">
        <el-button class="cl-btn-hero"><el-icon><UploadFilled /></el-icon>上传文件</el-button>
      </el-upload>
      <el-input v-model="folder" placeholder="文件夹（默认 /）" style="width: 200px; margin-left: 12px" />
    </div>

    <el-card>
      <el-table :data="files">
        <el-table-column label="文件夹" prop="folder" width="120" />
        <el-table-column label="文件名" prop="name" />
        <el-table-column label="上传人" prop="uploaderId" width="100" />
        <el-table-column label="上传时间" prop="createTime" width="180" />
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button text type="primary" @click="download(row)">下载</el-button>
            <el-button text type="danger" @click="del(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { uploadFile } from '@/api/file'
import { listTeamFiles, addTeamFile, deleteTeamFile } from '@/api/teamExtra'
import { useUserStore } from '@/store/user'

const route = useRoute()
const userStore = useUserStore()
const teamId = route.params.id
const files = ref([])
const folder = ref('/')

async function loadData() {
  files.value = await listTeamFiles(teamId)
}

async function onUpload({ file }) {
  const form = new FormData()
  form.append('file', file)
  const uploaded = await uploadFile(form, { scope: 'TEAM', businessId: teamId })
  await addTeamFile(teamId, { fileId: uploaded.fileId, folder: folder.value || '/', name: file.name })
  ElMessage.success('已上传到文件库')
  await loadData()
}

async function download(row) {
  const res = await fetch(`/api/file/${row.fileId}/download`, {
    headers: { Authorization: `Bearer ${userStore.token}` }
  })
  const blob = await res.blob()
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a'); a.href = url; a.download = row.name; a.click()
  URL.revokeObjectURL(url)
}

async function del(row) {
  await deleteTeamFile(row.id)
  ElMessage.success('已删除')
  await loadData()
}

onMounted(loadData)
</script>

<style scoped>
.action-bar { display: flex; gap: 12px; align-items: center; margin: 16px 0; }
</style>
