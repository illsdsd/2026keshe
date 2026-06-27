<template>
  <div class="page-container">
    <el-page-header @back="$router.back()" title="返回">
      <template #content>队伍黑名单 #{{ teamId }}</template>
    </el-page-header>

    <el-card style="margin-top: 16px">
      <template #header><strong>新增拉黑</strong></template>
      <el-form :inline="true" :model="form">
        <el-form-item label="用户 id"><el-input v-model="form.userId" /></el-form-item>
        <el-form-item label="原因"><el-input v-model="form.reason" /></el-form-item>
        <el-button type="primary" @click="add">拉黑</el-button>
      </el-form>
    </el-card>

    <el-card style="margin-top: 16px">
      <el-table :data="list">
        <el-table-column label="用户 id" prop="userId" width="120" />
        <el-table-column label="原因" prop="reason" />
        <el-table-column label="操作人" prop="operatorId" width="120" />
        <el-table-column label="时间" prop="createTime" width="180" />
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button text type="primary" @click="remove(row)">移除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { addBlacklist, listBlacklist, removeBlacklist } from '@/api/teamExtra'

const route = useRoute()
const teamId = route.params.id
const list = ref([])
const form = reactive({ userId: '', reason: '' })

async function loadData() { list.value = await listBlacklist(teamId) }
async function add() {
  if (!form.userId) { ElMessage.warning('请填写用户 id'); return }
  await addBlacklist({ teamId, userId: form.userId, reason: form.reason })
  ElMessage.success('已拉黑')
  form.userId = ''; form.reason = ''
  await loadData()
}
async function remove(row) { await removeBlacklist(row.id); ElMessage.success('已移除'); await loadData() }

onMounted(loadData)
</script>
