<template>
  <div class="page-container">
    <div class="cl-hero">
      <h1>我的作品集</h1>
      <p>展示参赛项目、个人作品与分工经历</p>
    </div>

    <div style="margin-bottom: 16px">
      <el-button class="cl-btn-hero" @click="openCreate">
        <el-icon><Plus /></el-icon> 新增作品
      </el-button>
    </div>

    <div class="grid">
      <el-card v-for="item in projects" :key="item.id" class="proj-card">
        <div class="proj-cover">
          <img v-if="item.cover" :src="item.cover" />
          <el-icon v-else size="40"><Picture /></el-icon>
        </div>
        <div class="proj-body">
          <h3>{{ item.title }}</h3>
          <p class="text-muted">{{ item.intro }}</p>
          <div class="proj-meta">
            <span v-if="item.award" class="cl-tag warn">{{ item.award }}</span>
            <span v-if="item.role" class="cl-tag info">{{ item.role }}</span>
          </div>
          <div class="proj-actions">
            <el-button text type="primary" @click="openEdit(item)">编辑</el-button>
            <el-button text type="danger" @click="del(item.id)">删除</el-button>
          </div>
        </div>
      </el-card>
    </div>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑作品' : '新增作品'" width="540px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="封面"><el-input v-model="form.cover" placeholder="图片 URL" /></el-form-item>
        <el-form-item label="链接"><el-input v-model="form.link" /></el-form-item>
        <el-form-item label="奖项"><el-input v-model="form.award" /></el-form-item>
        <el-form-item label="分工"><el-input v-model="form.role" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.intro" type="textarea" :rows="3" /></el-form-item>
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
import { listProjects, createProject, updateProject, deleteProject } from '@/api/user'

const projects = ref([])
const dialogVisible = ref(false)
const form = reactive({ id: null, title: '', cover: '', intro: '', link: '', award: '', role: '' })

async function loadData() {
  projects.value = await listProjects()
}

function openCreate() {
  Object.assign(form, { id: null, title: '', cover: '', intro: '', link: '', award: '', role: '' })
  dialogVisible.value = true
}

function openEdit(item) {
  Object.assign(form, item)
  dialogVisible.value = true
}

async function save() {
  if (form.id) {
    await updateProject(form.id, form)
  } else {
    await createProject(form)
  }
  ElMessage.success('已保存')
  dialogVisible.value = false
  await loadData()
}

async function del(id) {
  await ElMessageBox.confirm('确认删除这个作品？', '提示', { type: 'warning' })
  await deleteProject(id)
  ElMessage.success('已删除')
  await loadData()
}

onMounted(loadData)
</script>

<style scoped>
.grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 16px; }
.proj-card :deep(.el-card__body) { padding: 0; overflow: hidden; }
.proj-cover {
  height: 140px; background: linear-gradient(135deg, var(--cl-primary-light), var(--cl-primary));
  display: flex; align-items: center; justify-content: center; color: rgba(255,255,255,.8);
}
.proj-cover img { width: 100%; height: 100%; object-fit: cover; }
.proj-body { padding: 16px 18px; }
.proj-body h3 { margin: 0 0 6px; }
.proj-meta { display: flex; gap: 6px; flex-wrap: wrap; margin-top: 10px; }
.proj-actions { display: flex; justify-content: flex-end; gap: 6px; margin-top: 10px; }
</style>
