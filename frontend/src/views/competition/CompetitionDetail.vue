<template>
  <div class="page-container">
    <el-page-header @back="$router.back()" title="返回">
      <template #content>竞赛详情 #{{ id }}</template>
    </el-page-header>

    <el-card style="margin-top: 16px">
      <h2>{{ comp.name }}</h2>
      <p class="text-muted">类型：{{ comp.type }} · 报名截止：{{ comp.deadline }}</p>
      <p>{{ comp.intro }}</p>
      <el-button class="cl-btn-hero" @click="openRegister">报名参赛（队伍）</el-button>
    </el-card>

    <el-tabs v-model="tab" type="border-card" style="margin-top: 16px">
      <el-tab-pane label="赛事资讯" name="news">
        <el-button v-if="isAdmin" type="primary" @click="newsDialog = true" style="margin-bottom: 10px">发布资讯</el-button>
        <el-card v-for="n in news" :key="n.id" style="margin-bottom: 10px">
          <strong>{{ n.title }}</strong>
          <p class="text-muted">{{ n.createTime }}</p>
          <p>{{ n.content }}</p>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="赛事附件" name="attach">
        <el-table :data="attachments">
          <el-table-column label="名称" prop="name" />
          <el-table-column label="分类" prop="category" />
          <el-table-column label="操作">
            <template #default="{ row }">
              <el-button text type="primary" @click="downloadFile(row.fileId, row.name)">下载</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="报名列表" name="register">
        <el-table :data="registers">
          <el-table-column label="ID" prop="id" width="80" />
          <el-table-column label="队伍 id" prop="teamId" />
          <el-table-column label="状态" prop="status" />
          <el-table-column label="备注" prop="remark" />
          <el-table-column label="操作">
            <template #default="{ row }">
              <template v-if="isAdmin && row.status === 'PENDING'">
                <el-button text type="success" @click="audit(row, true)">通过</el-button>
                <el-button text type="danger" @click="audit(row, false)">拒绝</el-button>
              </template>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="registerDialog" title="报名" width="420px">
      <el-form :model="regForm" label-width="80px">
        <el-form-item label="队伍 id"><el-input v-model="regForm.teamId" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="regForm.remark" type="textarea" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="registerDialog = false">取消</el-button>
        <el-button type="primary" @click="submitRegister">提交</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="newsDialog" title="发布资讯" width="540px">
      <el-form :model="newsForm" label-width="80px">
        <el-form-item label="标题"><el-input v-model="newsForm.title" /></el-form-item>
        <el-form-item label="内容"><el-input v-model="newsForm.content" type="textarea" :rows="6" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="newsDialog = false">取消</el-button>
        <el-button type="primary" @click="submitNews">发布</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getCompetition } from '@/api/competition'
import { registerCompetition, auditRegister, listRegisters, listAttachments, listNews, publishNews } from '@/api/competitionExtra'
import { useUserStore } from '@/store/user'

const route = useRoute()
const userStore = useUserStore()
const id = route.params.id
const comp = ref({})
const tab = ref('news')
const news = ref([])
const attachments = ref([])
const registers = ref([])
const isAdmin = computed(() => userStore.user?.role === 'ADMIN')

const registerDialog = ref(false)
const regForm = reactive({ teamId: '', remark: '' })

const newsDialog = ref(false)
const newsForm = reactive({ title: '', content: '' })

function openRegister() { registerDialog.value = true }

async function submitRegister() {
  await registerCompetition(id, regForm)
  ElMessage.success('报名已提交')
  registerDialog.value = false
}

async function submitNews() {
  await publishNews(id, newsForm)
  ElMessage.success('已发布')
  newsDialog.value = false
  news.value = await listNews(id)
}

async function audit(row, approved) {
  await auditRegister(row.id, { approved, reason: approved ? '' : '不符合条件' })
  ElMessage.success('审核完成')
  registers.value = (await listRegisters(id, { current: 1, size: 100 })).records || []
}

async function downloadFile(fileId, name) {
  const res = await fetch(`/api/file/${fileId}/download`, {
    headers: { Authorization: `Bearer ${userStore.token}` }
  })
  const blob = await res.blob()
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a'); a.href = url; a.download = name; a.click()
  URL.revokeObjectURL(url)
}

import { computed } from 'vue'

onMounted(async () => {
  comp.value = await getCompetition(id)
  news.value = await listNews(id)
  attachments.value = await listAttachments(id)
  registers.value = (await listRegisters(id, { current: 1, size: 100 })).records || []
})
</script>
