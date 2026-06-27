<template>
  <div class="page-container">
    <div class="cl-hero">
      <h1>信誉分排行榜</h1>
      <p>全校学生信誉分 Top 50</p>
    </div>

    <el-card>
      <el-table :data="ranking" stripe>
        <el-table-column label="名次" width="80">
          <template #default="{ row }">
            <span class="medal" :class="medalClass(row.rank)">{{ row.rank }}</span>
          </template>
        </el-table-column>
        <el-table-column label="昵称" prop="nickname" />
        <el-table-column label="学院" prop="college" />
        <el-table-column label="信誉分">
          <template #default="{ row }">
            <span class="cl-tag" :class="row.reputation > 4.5 ? 'success' : 'info'">
              {{ row.reputation }}
            </span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { reputationRanking } from '@/api/user'

const ranking = ref([])

function medalClass(rank) {
  if (rank === 1) return 'gold'
  if (rank === 2) return 'silver'
  if (rank === 3) return 'bronze'
  return ''
}

onMounted(async () => { ranking.value = await reputationRanking(50) })
</script>

<style scoped>
.medal { display: inline-block; width: 28px; height: 28px; line-height: 28px; text-align: center; border-radius: 50%; background: var(--cl-primary-soft); color: var(--cl-primary); font-weight: 700; }
.medal.gold   { background: linear-gradient(135deg, #fde68a, #f59e0b); color: #fff; }
.medal.silver { background: linear-gradient(135deg, #e2e8f0, #94a3b8); color: #fff; }
.medal.bronze { background: linear-gradient(135deg, #fecaca, #b45309); color: #fff; }
</style>
