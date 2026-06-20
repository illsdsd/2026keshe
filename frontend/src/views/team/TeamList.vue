<template>
  <div class="page-container">
    <div class="header-row">
      <h2 class="page-title">找队伍</h2>
      <el-button type="primary" @click="$router.push('/teams/create')">
        <el-icon><Plus /></el-icon> 创建队伍
      </el-button>
    </div>

    <div class="filter-bar">
      <el-select v-model="query.competitionId" placeholder="全部竞赛" clearable filterable
                 style="width: 200px" @change="reload">
        <el-option v-for="c in competitions" :key="c.id" :label="c.name" :value="c.id" />
      </el-select>
      <el-input v-model="query.college" placeholder="学院" clearable style="width: 140px"
                @keyup.enter="reload" @clear="reload" />
      <el-input v-model.number="query.grade" placeholder="队长年级" clearable style="width: 120px"
                @keyup.enter="reload" @clear="reload" />
      <el-input v-model="query.skill" placeholder="招募岗位/技能" clearable style="width: 150px"
                @keyup.enter="reload" @clear="reload" />
      <el-select v-model="query.status" placeholder="全部状态" clearable style="width: 130px" @change="reload">
        <el-option v-for="s in TEAM_STATUS" :key="s.value" :label="s.label" :value="s.value" />
      </el-select>
      <el-input v-model="query.keyword" placeholder="队伍名称/简介" clearable style="width: 180px"
                @keyup.enter="reload" @clear="reload" />
      <el-button type="primary" @click="reload">搜索</el-button>
      <el-button @click="resetFilter">重置</el-button>
    </div>

    <div v-loading="loading">
      <el-card v-for="t in list" :key="t.id" class="team-card" shadow="hover"
               @click="$router.push(`/teams/${t.id}`)">
        <div class="team-top">
          <span class="team-name">{{ t.name }}</span>
          <el-tag :type="TEAM_STATUS_TAG[t.status]" size="small">{{ TEAM_STATUS_MAP[t.status] }}</el-tag>
          <el-tag v-if="t.competitionName" type="info" effect="plain" size="small">
            {{ t.competitionName }}
          </el-tag>
          <span class="size-info">{{ t.currentSize }}/{{ t.totalSize }} 人</span>
        </div>
        <p class="team-intro">{{ t.intro || '暂无简介' }}</p>
        <div class="team-foot">
          <span class="text-muted">
            队长：{{ t.leaderName || '—' }}<template v-if="t.college"> · {{ t.college }}</template>
          </span>
          <span v-if="t.vacancies" class="vacancies">缺口：{{ t.vacancies }}</span>
          <span v-else class="text-muted">暂无缺口</span>
        </div>
      </el-card>
      <el-empty v-if="!loading && !list.length" description="没有符合条件的队伍" />
    </div>

    <el-pagination
      v-if="total > 0"
      class="pager"
      layout="prev, pager, next, total"
      :total="total"
      :current-page="query.current"
      :page-size="query.size"
      @current-change="onPage"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { pageTeams } from '@/api/team'
import { pageCompetitions } from '@/api/competition'
import { TEAM_STATUS, TEAM_STATUS_MAP, TEAM_STATUS_TAG } from '@/constants'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const competitions = ref([])
const query = reactive({
  current: 1,
  size: 10,
  competitionId: null,
  college: '',
  grade: null,
  skill: '',
  status: '',
  keyword: ''
})

async function load() {
  loading.value = true
  try {
    const data = await pageTeams(query)
    list.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

function reload() {
  query.current = 1
  load()
}

function resetFilter() {
  Object.assign(query, {
    current: 1, competitionId: null, college: '', grade: null, skill: '', status: '', keyword: ''
  })
  load()
}

function onPage(p) {
  query.current = p
  load()
}

onMounted(async () => {
  const data = await pageCompetitions({ current: 1, size: 100 })
  competitions.value = data.records
  await load()
})
</script>

<style scoped>
.header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.team-card {
  margin-bottom: 12px;
  cursor: pointer;
}
.team-top {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}
.team-name {
  font-size: 16px;
  font-weight: 600;
}
.size-info {
  margin-left: auto;
  font-size: 13px;
  color: #909399;
}
.team-intro {
  color: #606266;
  font-size: 13px;
  line-height: 1.6;
  margin: 0 0 10px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.team-foot {
  display: flex;
  gap: 20px;
  align-items: center;
  font-size: 13px;
}
.vacancies {
  color: #e6a23c;
}
.pager {
  margin-top: 16px;
  justify-content: center;
}
</style>
