<template>
  <div class="page-container">
    <el-page-header content="创建队伍" @back="$router.back()" />
    <el-card class="form-card">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="96px">
        <el-form-item label="队伍名称" prop="name">
          <el-input v-model="form.name" placeholder="给队伍起个名字" />
        </el-form-item>
        <el-form-item label="关联竞赛">
          <el-select v-model="form.competitionId" placeholder="选择竞赛（可选）" clearable filterable
                     style="width: 100%">
            <el-option v-for="c in competitions" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="所属学院">
          <el-input v-model="form.college" placeholder="如 软件学院" />
        </el-form-item>
        <el-form-item label="需要总人数" prop="totalSize">
          <el-input-number v-model="form.totalSize" :min="1" :max="20" />
        </el-form-item>
        <el-form-item label="队伍简介">
          <el-input v-model="form.intro" type="textarea" :rows="4" placeholder="介绍一下队伍目标、要求等" />
        </el-form-item>
        <el-form-item label="招募岗位">
          <div class="recruit-list">
            <div v-for="(r, i) in form.recruits" :key="i" class="recruit-row">
              <el-input v-model="r.position" placeholder="岗位（如 Vue 开发）" style="width: 220px" />
              <el-input-number v-model="r.count" :min="1" :max="10" />
              <el-button link type="danger" @click="form.recruits.splice(i, 1)">
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>
            <el-button type="primary" plain size="small" @click="addRecruit">
              <el-icon><Plus /></el-icon> 添加岗位
            </el-button>
          </div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="onSubmit">创建队伍</el-button>
          <el-button @click="$router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createTeam } from '@/api/team'
import { pageCompetitions } from '@/api/competition'

const router = useRouter()
const formRef = ref()
const saving = ref(false)
const competitions = ref([])
const form = reactive({
  name: '',
  competitionId: null,
  college: '',
  totalSize: 4,
  intro: '',
  recruits: [{ position: '', count: 1 }]
})
const rules = {
  name: [{ required: true, message: '请输入队伍名称', trigger: 'blur' }],
  totalSize: [{ required: true, message: '请填写总人数', trigger: 'blur' }]
}

function addRecruit() {
  form.recruits.push({ position: '', count: 1 })
}

async function onSubmit() {
  await formRef.value.validate()
  saving.value = true
  try {
    const payload = {
      ...form,
      recruits: form.recruits.filter((r) => r.position && r.position.trim())
    }
    const team = await createTeam(payload)
    ElMessage.success('队伍创建成功')
    router.push(`/teams/${team.id}`)
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  const data = await pageCompetitions({ current: 1, size: 100 })
  competitions.value = data.records
})
</script>

<style scoped>
.form-card {
  max-width: 700px;
  margin: 16px auto 0;
}
.recruit-list {
  width: 100%;
}
.recruit-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 10px;
}
</style>
