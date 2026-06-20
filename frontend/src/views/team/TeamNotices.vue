<template>
  <div class="page-container" v-loading="loading">
    <div class="notice-head">
      <el-page-header content="队伍公告" @back="$router.push(`/teams/${teamId}`)" />
      <el-button v-if="isLeader" type="primary" @click="openPublish">
        <el-icon><Plus /></el-icon> 发布公告
      </el-button>
    </div>

    <el-timeline v-if="list.length" class="timeline">
      <el-timeline-item
        v-for="n in list"
        :key="n.id"
        :timestamp="n.createTime"
        placement="top"
        type="primary"
      >
        <el-card>
          <div class="notice-title-row">
            <span class="notice-title">{{ n.title }}</span>
            <el-button v-if="isLeader" link type="danger" size="small" @click="onDelete(n)">
              删除
            </el-button>
          </div>
          <p class="notice-content">{{ n.content }}</p>
          <div class="text-muted">发布者：{{ n.authorName }}</div>
        </el-card>
      </el-timeline-item>
    </el-timeline>
    <el-empty v-else-if="!loading" description="暂无公告" />

    <!-- 发布公告 -->
    <el-dialog v-model="dialog" title="发布公告" width="520px">
      <el-form :model="form" label-position="top">
        <el-form-item label="标题">
          <el-input v-model="form.title" placeholder="公告标题" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="form.content" type="textarea" :rows="5" placeholder="公告内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="publish">发布</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listTeamNotices, publishNotice, deleteNotice } from '@/api/notice'
import { getTeam } from '@/api/team'
import { useUserStore } from '@/store/user'

const route = useRoute()
const teamId = route.params.id
const userStore = useUserStore()

const loading = ref(false)
const list = ref([])
const isLeader = ref(false)

const dialog = ref(false)
const saving = ref(false)
const form = reactive({ title: '', content: '' })

async function load() {
  loading.value = true
  try {
    list.value = await listTeamNotices(teamId)
  } finally {
    loading.value = false
  }
}

function openPublish() {
  Object.assign(form, { title: '', content: '' })
  dialog.value = true
}

async function publish() {
  if (!form.title?.trim() || !form.content?.trim()) {
    ElMessage.warning('请填写标题和内容')
    return
  }
  saving.value = true
  try {
    await publishNotice({ teamId, ...form })
    ElMessage.success('已发布')
    dialog.value = false
    load()
  } finally {
    saving.value = false
  }
}

async function onDelete(n) {
  await ElMessageBox.confirm('确定删除该公告？', '提示', { type: 'warning' })
  await deleteNotice(n.id)
  ElMessage.success('已删除')
  load()
}

onMounted(async () => {
  const detail = await getTeam(teamId)
  isLeader.value = detail.team?.leaderId === userStore.user?.id
  await load()
})
</script>

<style scoped>
.notice-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.timeline {
  margin-top: 16px;
  padding-left: 4px;
}
.notice-title-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.notice-title {
  font-size: 16px;
  font-weight: 600;
}
.notice-content {
  white-space: pre-wrap;
  line-height: 1.7;
  color: #606266;
  margin: 8px 0;
}
</style>
