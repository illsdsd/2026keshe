<template>
  <div>
    <h2 class="page-title">技能标签管理</h2>
    <div style="margin-bottom: 16px">
      <el-button type="primary" @click="openCreate">新增技能</el-button>
    </div>
    <el-card>
      <el-table :data="list" stripe>
        <el-table-column label="ID" prop="id" width="80" />
        <el-table-column label="名称" prop="name" />
        <el-table-column label="分类" prop="category" />
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button text type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button text type="danger" @click="del(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑技能' : '新增技能'" width="420px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="分类"><el-input v-model="form.category" placeholder="如：后端 / 前端 / 算法" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminCreateSkill, adminUpdateSkill, adminDeleteSkill } from '@/api/admin'
import { getAll as listAllSkills } from '@/api/skill'

const list = ref([])
const dialogVisible = ref(false)
const form = reactive({ id: null, name: '', category: '' })

async function loadData() { list.value = await listAllSkills() }

function openCreate() { form.id = null; form.name = ''; form.category = ''; dialogVisible.value = true }
function openEdit(row) { form.id = row.id; form.name = row.name; form.category = row.category; dialogVisible.value = true }

async function save() {
  if (form.id) await adminUpdateSkill(form.id, form)
  else await adminCreateSkill(form)
  ElMessage.success('已保存')
  dialogVisible.value = false
  await loadData()
}

async function del(row) {
  await ElMessageBox.confirm(`确认删除技能「${row.name}」？`, '提示', { type: 'warning' })
  await adminDeleteSkill(row.id)
  ElMessage.success('已删除')
  await loadData()
}

onMounted(loadData)
</script>
