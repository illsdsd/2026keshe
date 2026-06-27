<template>
  <div class="page-container">
    <div class="cl-hero">
      <h1>归档队伍</h1>
      <p>已完结/归档队伍，可查看不可修改</p>
    </div>

    <el-card>
      <el-table :data="teams">
        <el-table-column label="ID" prop="id" width="80" />
        <el-table-column label="队伍名" prop="name" />
        <el-table-column label="队长" prop="leaderId" width="100" />
        <el-table-column label="归档时间" prop="archivedTime" width="180" />
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <router-link :to="`/teams/${row.id}`">
              <el-button text type="primary">查看</el-button>
            </router-link>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { pageTeams } from '@/api/team'

const teams = ref([])

onMounted(async () => {
  const data = await pageTeams({ status: 'ARCHIVED', current: 1, size: 50 })
  teams.value = data.records || []
})
</script>
