<template>
  <div class="page-container">
    <div class="cl-hero">
      <h1>赛事排行榜</h1>
      <p>按报名通过队伍数排序，看哪些赛事最热门</p>
    </div>

    <el-card>
      <el-table :data="ranking" stripe>
        <el-table-column label="名次" width="80">
          <template #default="{ row, $index }">
            <span class="medal" :class="$index < 3 ? ['gold','silver','bronze'][$index] : ''">{{ $index + 1 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="赛事" prop="competitionName" />
        <el-table-column label="类型" prop="type" width="120" />
        <el-table-column label="通过报名队伍数" prop="approvedCount" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { competitionRanking } from '@/api/competitionExtra'

const ranking = ref([])
onMounted(async () => { ranking.value = await competitionRanking() })
</script>

<style scoped>
.medal { display: inline-block; width: 28px; height: 28px; line-height: 28px; text-align: center; border-radius: 50%; background: var(--cl-primary-soft); color: var(--cl-primary); font-weight: 700; }
.medal.gold { background: linear-gradient(135deg, #fde68a, #f59e0b); color: #fff; }
.medal.silver { background: linear-gradient(135deg, #e2e8f0, #94a3b8); color: #fff; }
.medal.bronze { background: linear-gradient(135deg, #fecaca, #b45309); color: #fff; }
</style>
