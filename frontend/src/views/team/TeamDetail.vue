<template>
  <div class="page-container" v-loading="loading">
    <el-page-header content="队伍详情" @back="$router.back()" />

    <template v-if="detail">
      <el-card class="detail-card">
        <div class="detail-head">
          <div>
            <div class="title-line">
              <span class="team-name">{{ team.name }}</span>
              <el-tag :type="TEAM_STATUS_TAG[team.status]">{{ TEAM_STATUS_MAP[team.status] }}</el-tag>
            </div>
            <div class="text-muted meta-line">
              <span v-if="detail.competitionName">
                <el-icon><Trophy /></el-icon> {{ detail.competitionName }}
              </span>
              <span><el-icon><User /></el-icon> 队长：{{ detail.leaderName }}</span>
              <span v-if="team.college"><el-icon><School /></el-icon> {{ team.college }}</span>
              <span><el-icon><UserFilled /></el-icon> {{ team.currentSize }}/{{ team.totalSize }} 人</span>
            </div>
          </div>
          <div class="head-ops">
            <el-button v-if="isMember || isLeader" @click="$router.push(`/teams/${team.id}/board`)">
              <el-icon><Grid /></el-icon> 任务看板
            </el-button>
            <template v-if="isLeader">
              <el-button type="primary" @click="$router.push(`/teams/${team.id}/applies`)">
                申请管理
              </el-button>
              <el-button type="primary" plain @click="openEdit">编辑</el-button>
              <el-button type="danger" plain @click="onDissolve">解散</el-button>
            </template>
            <el-button v-else-if="!isMember && team.status === 'RECRUITING'" type="primary" @click="onApply">
              申请加入
            </el-button>
            <el-tag v-else-if="isMember" type="success" size="large">已加入</el-tag>
          </div>
        </div>

        <el-divider content-position="left">队伍简介</el-divider>
        <p class="intro">{{ team.intro || '暂无简介' }}</p>

        <el-divider content-position="left">招募岗位</el-divider>
        <div v-if="detail.recruits?.length" class="recruits">
          <el-tag v-for="r in detail.recruits" :key="r.id"
                  :type="r.filled >= r.count ? 'info' : 'warning'" effect="plain" size="large">
            {{ r.position }} {{ r.filled }}/{{ r.count }}
          </el-tag>
        </div>
        <span v-else class="text-muted">未设置招募岗位</span>

        <el-divider content-position="left">队伍成员</el-divider>
        <div class="members">
          <div v-for="m in detail.members" :key="m.userId" class="member"
               @click="$router.push(`/user/${m.userId}`)">
            <el-avatar :size="44" :src="m.avatar">{{ m.nickname?.charAt(0) }}</el-avatar>
            <div class="member-info">
              <span class="member-name">
                {{ m.nickname }}
                <el-tag v-if="m.role === 'LEADER'" type="danger" size="small">队长</el-tag>
              </span>
              <span class="text-muted">{{ m.major || '—' }} · {{ m.grade ? m.grade + '级' : '—' }}</span>
            </div>
          </div>
        </div>
      </el-card>
    </template>

    <!-- 编辑队伍 -->
    <el-dialog v-model="editDialog" title="编辑队伍" width="520px">
      <el-form :model="editForm" label-width="90px">
        <el-form-item label="队伍名称"><el-input v-model="editForm.name" /></el-form-item>
        <el-form-item label="关联竞赛">
          <el-select v-model="editForm.competitionId" clearable filterable style="width: 100%">
            <el-option v-for="c in competitions" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="所属学院"><el-input v-model="editForm.college" /></el-form-item>
        <el-form-item label="总人数"><el-input-number v-model="editForm.totalSize" :min="1" :max="20" /></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="editForm.status" style="width: 100%">
            <el-option v-for="s in TEAM_STATUS" :key="s.value" :label="s.label" :value="s.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="简介"><el-input v-model="editForm.intro" type="textarea" :rows="4" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveEdit">保存</el-button>
      </template>
    </el-dialog>

    <!-- 申请加入 -->
    <el-dialog v-model="applyDialog" title="申请加入队伍" width="480px">
      <el-form :model="applyForm" label-position="top">
        <el-form-item label="自我介绍">
          <el-input v-model="applyForm.selfIntro" type="textarea" :rows="3"
                    placeholder="简单介绍一下你自己" />
        </el-form-item>
        <el-form-item label="技能说明">
          <el-input v-model="applyForm.skillDesc" type="textarea" :rows="3"
                    placeholder="你能为队伍带来什么" />
        </el-form-item>
        <el-form-item label="个人主页链接">
          <el-input v-model="applyForm.profileLink" placeholder="可选，如 GitHub / 作品集" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="applyDialog = false">取消</el-button>
        <el-button type="primary" :loading="applying" @click="doApply">提交申请</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getTeam, updateTeam, deleteTeam } from '@/api/team'
import { pageCompetitions } from '@/api/competition'
import { submitApply } from '@/api/apply'
import { useUserStore } from '@/store/user'
import { TEAM_STATUS, TEAM_STATUS_MAP, TEAM_STATUS_TAG } from '@/constants'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const detail = ref(null)
const team = computed(() => detail.value?.team || {})
const isLeader = computed(() => team.value.leaderId === userStore.user?.id)
const isMember = computed(() =>
  (detail.value?.members || []).some((m) => m.userId === userStore.user?.id)
)

const applyDialog = ref(false)
const applying = ref(false)
const applyForm = reactive({ selfIntro: '', skillDesc: '', profileLink: '' })

const competitions = ref([])
const editDialog = ref(false)
const saving = ref(false)
const editForm = reactive({ name: '', competitionId: null, college: '', totalSize: 1, status: '', intro: '' })

async function load() {
  loading.value = true
  try {
    detail.value = await getTeam(route.params.id)
  } finally {
    loading.value = false
  }
}

async function openEdit() {
  if (!competitions.value.length) {
    const data = await pageCompetitions({ current: 1, size: 100 })
    competitions.value = data.records
  }
  Object.assign(editForm, {
    name: team.value.name,
    competitionId: team.value.competitionId,
    college: team.value.college,
    totalSize: team.value.totalSize,
    status: team.value.status,
    intro: team.value.intro
  })
  editDialog.value = true
}

async function saveEdit() {
  saving.value = true
  try {
    await updateTeam(team.value.id, editForm)
    ElMessage.success('已保存')
    editDialog.value = false
    load()
  } finally {
    saving.value = false
  }
}

async function onDissolve() {
  await ElMessageBox.confirm('确定解散该队伍？此操作不可恢复', '警告', { type: 'warning' })
  await deleteTeam(team.value.id)
  ElMessage.success('队伍已解散')
  router.push('/teams')
}

function onApply() {
  Object.assign(applyForm, { selfIntro: '', skillDesc: '', profileLink: '' })
  applyDialog.value = true
}

async function doApply() {
  applying.value = true
  try {
    await submitApply({ teamId: team.value.id, ...applyForm })
    ElMessage.success('申请已提交，请等待队长审核')
    applyDialog.value = false
  } finally {
    applying.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.detail-card {
  margin-top: 16px;
}
.detail-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}
.title-line {
  display: flex;
  align-items: center;
  gap: 12px;
}
.team-name {
  font-size: 22px;
  font-weight: 600;
}
.meta-line {
  display: flex;
  flex-wrap: wrap;
  gap: 18px;
  margin-top: 10px;
}
.meta-line span {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}
.intro {
  white-space: pre-wrap;
  line-height: 1.7;
  color: #606266;
}
.recruits {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}
.members {
  display: flex;
  flex-wrap: wrap;
  gap: 18px;
}
.member {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
}
.member-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.member-name {
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 500;
}
</style>
