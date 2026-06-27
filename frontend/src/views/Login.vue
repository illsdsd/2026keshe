<template>
  <div class="auth-page">
    <div class="auth-card">
      <div class="brand">
        <el-icon :size="28"><Connection /></el-icon>
        <h1>CampusLink</h1>
      </div>
      <p class="subtitle">校园竞赛组队与协作平台</p>
      <el-form ref="formRef" :model="form" :rules="rules" size="large" @submit.prevent>
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="账号" :prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="密码"
            :prefix-icon="Lock"
            show-password
            @keyup.enter="onSubmit"
          />
        </el-form-item>
        <el-button type="primary" :loading="loading" class="submit-btn" @click="onSubmit">
          登录
        </el-button>
      </el-form>
      <div class="auth-footer">
        <router-link to="/forget-password" class="link-secondary">忘记密码？</router-link>
        <span>·</span>
        <router-link to="/register">去注册</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { User, Lock } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { login } from '@/api/auth'
import { useUserStore } from '@/store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const formRef = ref()
const loading = ref(false)
const form = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function onSubmit() {
  await formRef.value.validate()
  loading.value = true
  try {
    const data = await login(form)
    userStore.setAuth(data.token, data.user)
    ElMessage.success('登录成功')
    router.push(route.query.redirect || '/teams')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background:
    radial-gradient(circle at 20% 30%, rgba(255, 255, 255, 0.18), transparent 40%),
    radial-gradient(circle at 80% 70%, rgba(255, 255, 255, 0.18), transparent 40%),
    var(--cl-gradient-hero);
}
.auth-card {
  width: 400px;
  background: var(--cl-bg-glass);
  backdrop-filter: blur(20px);
  border-radius: var(--cl-radius-lg);
  padding: 44px 36px 32px;
  box-shadow: var(--cl-shadow-xl);
  border: 1px solid rgba(255, 255, 255, 0.4);
}
.brand {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  color: var(--cl-primary);
}
.brand h1 {
  font-size: 28px;
  margin: 0;
  font-weight: 800;
  letter-spacing: -0.5px;
}
.subtitle {
  text-align: center;
  color: var(--cl-text-secondary);
  margin: 10px 0 28px;
}
.submit-btn {
  width: 100%;
  background: var(--cl-gradient-hero) !important;
  border: none !important;
  height: 44px;
  font-weight: 600;
}
.auth-footer {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 10px;
  margin-top: 18px;
  font-size: 13px;
  color: var(--cl-text-secondary);
}
.auth-footer a {
  color: var(--cl-primary);
  font-weight: 500;
}
.link-secondary {
  color: var(--cl-text-secondary) !important;
}
</style>
