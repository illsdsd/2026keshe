<template>
  <div class="page-container">
    <el-page-header @back="$router.back()" title="返回">
      <template #content>动态墙 #{{ teamId }}</template>
    </el-page-header>

    <el-card style="margin-top: 16px">
      <template #header><strong>发布动态</strong></template>
      <el-input v-model="form.content" type="textarea" :rows="3" placeholder="说点什么..." />
      <el-input v-model="form.imageUrls" placeholder="图片 URL，多张用逗号分隔" style="margin-top: 10px" />
      <div style="margin-top: 10px; text-align: right">
        <el-button class="cl-btn-hero" @click="publish">发布</el-button>
      </div>
    </el-card>

    <div class="posts">
      <el-card v-for="p in posts" :key="p.id" class="post-card">
        <div class="post-head">
          <el-avatar :size="36" class="cl-avatar">{{ ('U' + p.authorId).slice(-2) }}</el-avatar>
          <div>
            <div><strong>用户 #{{ p.authorId }}</strong></div>
            <div class="text-muted">{{ p.createTime }}</div>
          </div>
        </div>
        <p>{{ p.content }}</p>
        <div v-if="p.imageUrls" class="post-images">
          <img v-for="url in p.imageUrls.split(',')" :key="url" :src="url" />
        </div>
        <div class="post-actions">
          <el-button text @click="like(p)">
            <el-icon><Star /></el-icon> {{ p.likeCount }}
          </el-button>
          <el-button text @click="openComment(p)">
            <el-icon><ChatDotRound /></el-icon> {{ p.commentCount }}
          </el-button>
        </div>
        <div v-if="commentingId === p.id" class="comment-box">
          <el-input v-model="commentContent" placeholder="评论..." />
          <el-button type="primary" @click="submitComment(p)">提交</el-button>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { pageTeamPosts, publishTeamPost, commentPost, likePost } from '@/api/teamExtra'

const route = useRoute()
const teamId = route.params.id
const form = reactive({ content: '', imageUrls: '' })
const posts = ref([])
const commentingId = ref(null)
const commentContent = ref('')

async function loadData() {
  const data = await pageTeamPosts(teamId, { current: 1, size: 30 })
  posts.value = data.records
}

async function publish() {
  if (!form.content) { ElMessage.warning('内容不能为空'); return }
  await publishTeamPost(teamId, form)
  ElMessage.success('已发布')
  form.content = ''; form.imageUrls = ''
  await loadData()
}

async function like(p) {
  const data = await likePost(p.id)
  p.likeCount += data.liked ? 1 : -1
}

function openComment(p) {
  commentingId.value = commentingId.value === p.id ? null : p.id
  commentContent.value = ''
}

async function submitComment(p) {
  if (!commentContent.value) return
  await commentPost(p.id, { content: commentContent.value })
  ElMessage.success('已评论')
  commentContent.value = ''
  commentingId.value = null
  await loadData()
}

onMounted(loadData)
</script>

<style scoped>
.posts { margin-top: 16px; display: flex; flex-direction: column; gap: 12px; }
.post-card { padding: 0; }
.post-head { display: flex; gap: 12px; align-items: center; margin-bottom: 10px; }
.post-images { display: flex; gap: 8px; flex-wrap: wrap; }
.post-images img { width: 120px; height: 90px; object-fit: cover; border-radius: 8px; }
.post-actions { margin-top: 10px; display: flex; gap: 10px; }
.comment-box { display: flex; gap: 8px; margin-top: 10px; }
</style>
