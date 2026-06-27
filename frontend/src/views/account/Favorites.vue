<template>
  <div class="page-container">
    <div class="cl-hero">
      <h1>我的收藏</h1>
      <p>收藏的竞赛与意向队伍统一管理</p>
    </div>

    <el-tabs v-model="tab" type="border-card">
      <el-tab-pane label="全部" name="" />
      <el-tab-pane label="竞赛" name="COMPETITION" />
      <el-tab-pane label="队伍" name="TEAM" />
    </el-tabs>

    <div v-if="favorites.length === 0" class="empty">还没有收藏哦~</div>
    <el-card v-for="f in favorites" :key="f.id" class="fav-card">
      <div class="fav-row">
        <div>
          <div class="fav-title">
            <span class="cl-tag" :class="f.refType === 'COMPETITION' ? 'info' : 'success'">
              {{ f.refType === 'COMPETITION' ? '竞赛' : '队伍' }}
            </span>
            #{{ f.refId }}
          </div>
          <div class="text-muted">备注：{{ f.note || '—' }} · 收藏于 {{ f.createTime }}</div>
        </div>
        <el-button type="danger" plain @click="remove(f.id)">取消收藏</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { listFavorites, removeFavorite } from '@/api/user'

const tab = ref('')
const favorites = ref([])

async function loadData() {
  favorites.value = await listFavorites(tab.value || undefined)
}

async function remove(id) {
  await removeFavorite(id)
  ElMessage.success('已取消收藏')
  await loadData()
}

watch(tab, loadData)
onMounted(loadData)
</script>

<style scoped>
.fav-card { margin-bottom: 12px; }
.fav-row { display: flex; justify-content: space-between; align-items: center; }
.fav-title { font-weight: 600; display: flex; gap: 8px; align-items: center; }
.empty { padding: 60px; text-align: center; color: var(--cl-text-muted); }
</style>
