<template>
  <div class="page-container">
    <div class="cl-hero">
      <h1>信誉分明细</h1>
      <p>每一次评价的加权来源都在这里</p>
    </div>

    <div class="stat-grid">
      <div class="stat-card hero">
        <div class="stat-card__label">评价次数</div>
        <div class="stat-card__value">{{ detail.total || 0 }}</div>
      </div>
      <div class="stat-card mint">
        <div class="stat-card__label">责任心均值</div>
        <div class="stat-card__value">{{ detail.avgResponsibility }}</div>
      </div>
      <div class="stat-card sky">
        <div class="stat-card__label">技术能力均值</div>
        <div class="stat-card__value">{{ detail.avgTech }}</div>
      </div>
      <div class="stat-card amber">
        <div class="stat-card__label">沟通能力均值</div>
        <div class="stat-card__value">{{ detail.avgCommunication }}</div>
      </div>
    </div>

    <el-card>
      <template #header><strong>评价明细</strong></template>
      <el-table :data="detail.items || []" stripe>
        <el-table-column label="队伍 id" prop="teamId" width="100" />
        <el-table-column label="评价人 id" prop="fromUserId" width="100" />
        <el-table-column label="责任心" prop="responsibility" width="100" />
        <el-table-column label="技术" prop="tech" width="100" />
        <el-table-column label="沟通" prop="communication" width="100" />
        <el-table-column label="时间" prop="createTime" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { getReputationDetail } from '@/api/user'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const detail = ref({})

onMounted(async () => {
  detail.value = await getReputationDetail(userStore.user.id)
})
</script>
