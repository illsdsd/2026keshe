<template>
  <div>
    <h2 class="page-title">系统公告</h2>
    <el-card style="margin-bottom: 16px">
      <template #header><strong>发布平台公告</strong></template>
      <el-form :model="form" label-width="80px">
        <el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="内容"><el-input v-model="form.content" type="textarea" :rows="6" /></el-form-item>
        <el-button type="primary" @click="publish">发布</el-button>
      </el-form>
    </el-card>

    <el-card>
      <template #header><strong>历史公告</strong></template>
      <el-table :data="list">
        <el-table-column label="ID" prop="id" width="80" />
        <el-table-column label="标题" prop="title" />
        <el-table-column label="发布时间" prop="publishAt" width="180" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { adminPublishNotice, adminListNotices } from '@/api/admin'

const form = reactive({ title: '', content: '' })
const list = ref([])

async function loadData() { list.value = await adminListNotices() }

async function publish() {
  if (!form.title) { ElMessage.warning('请填写标题'); return }
  await adminPublishNotice(form)
  ElMessage.success('已发布')
  form.title = ''; form.content = ''
  await loadData()
}

onMounted(loadData)
</script>
