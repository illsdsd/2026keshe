<template>
  <div class="page-container" v-loading="loading">
    <h2 class="page-title">我的队伍</h2>
    <el-row :gutter="16">
      <el-col v-for="t in list" :key="t.id" :xs="24" :sm="12" :lg="8">
        <el-card class="team-card" shadow="hover" @click="$router.push(`/teams/${t.id}`)">
          <div class="team-top">
            <span class="team-name">{{ t.name }}</span>
            <el-tag :type="TEAM_STATUS_TAG[t.status]" size="small">{{ TEAM_STATUS_MAP[t.status] }}</el-tag>
          </div>
          <p class="team-intro">{{ t.intro || '暂无简介' }}</p>
          <div class="text-muted">
            {{ t.currentSize }}/{{ t.totalSize }} 人
            <el-tag v-if="t.leaderId === myId" type="danger" size="small" effect="plain">我是队长</el-tag>
          </div>
        </el-card>
      </el-col>
    </el-row>
    <el-empty v-if="!loading && !list.length" description="你还没有加入任何队伍">
      <el-button type="primary" @click="$router.push('/teams')">去找队伍</el-button>
    </el-empty>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { myTeams } from '@/api/team'
import { useUserStore } from '@/store/user'
import { TEAM_STATUS_MAP, TEAM_STATUS_TAG } from '@/constants'

const userStore = useUserStore()
const myId = computed(() => userStore.user?.id)
const loading = ref(false)
const list = ref([])

onMounted(async () => {
  loading.value = true
  try {
    list.value = await myTeams()
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.team-card {
  margin-bottom: 16px;
  cursor: pointer;
}
.team-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.team-name {
  font-size: 16px;
  font-weight: 600;
}
.team-intro {
  color: #606266;
  font-size: 13px;
  line-height: 1.6;
  min-height: 42px;
  margin: 0 0 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
