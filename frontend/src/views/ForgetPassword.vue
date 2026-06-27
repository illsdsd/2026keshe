<template>
  <div class="auth-page">
    <div class="auth-card">
      <div class="brand">
        <el-icon :size="28"><Key /></el-icon>
        <h1>找回密码</h1>
      </div>
      <p class="subtitle">通过绑定邮箱接收验证码</p>
      <el-form :model="form" :rules="rules" ref="formRef" size="large">
        <el-form-item prop="email">
          <el-input v-model="form.email" placeholder="注册邮箱" :prefix-icon="Message" />
        </el-form-item>
        <el-form-item prop="code">
          <div class="code-row">
            <el-input v-model="form.code" placeholder="6 位验证码" maxlength="6" />
            <el-button type="primary" :disabled="cooldown > 0" @click="sendCode">
              {{ cooldown > 0 ? `${cooldown}s` : '获取验证码' }}
            </el-button>
          </div>
        </el-form-item>
        <el-form-item prop="newPassword">
          <el-input v-model="form.newPassword" type="password" placeholder="新密码（≥ 6 位）" show-password :prefix-icon="Lock" />
        </el-form-item>
        <el-button type="primary" class="submit-btn" :loading="loading" @click="onSubmit">重置密码</el-button>
      </el-form>
      <div class="auth-footer">
        <router-link to="/login">返回登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Key, Message, Lock } from '@element-plus/icons-vue'
import { sendForgetCode, resetPassword } from '@/api/auth'

const router = useRouter()
const formRef = ref()
const loading = ref(false)
const cooldown = ref(0)
let timer = null

const form = reactive({ email: '', code: '', newPassword: '' })
const rules = {
  email: [{ required: true, message: '请输入邮箱', trigger: 'blur', type: 'email' }],
  code: [{ required: true, min: 6, max: 6, message: '请输入 6 位验证码', trigger: 'blur' }],
  newPassword: [{ required: true, min: 6, message: '密码至少 6 位', trigger: 'blur' }]
}

async function sendCode() {
  if (!form.email) { ElMessage.warning('请先填写邮箱'); return }
  try {
    await sendForgetCode({ email: form.email })
    ElMessage.success('验证码已发送（控制台可见）')
    cooldown.value = 60
    timer = setInterval(() => {
      cooldown.value--
      if (cooldown.value <= 0) clearInterval(timer)
    }, 1000)
  } catch (e) { /* 拦截器已提示 */ }
}

async function onSubmit() {
  await formRef.value.validate()
  loading.value = true
  try {
    await resetPassword(form)
    ElMessage.success('密码已重置，请重新登录')
    router.push('/login')
  } finally {
    loading.value = false
  }
}

onBeforeUnmount(() => { if (timer) clearInterval(timer) })
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
  display: flex; align-items: center; justify-content: center; gap: 10px;
  color: var(--cl-primary);
}
.brand h1 { font-size: 26px; margin: 0; font-weight: 800; }
.subtitle { text-align: center; color: var(--cl-text-secondary); margin: 10px 0 28px; }
.code-row { display: flex; gap: 10px; }
.code-row .el-input { flex: 1; }
.submit-btn { width: 100%; background: var(--cl-gradient-hero) !important; border: none !important; height: 44px; font-weight: 600; }
.auth-footer { text-align: center; margin-top: 18px; font-size: 13px; }
.auth-footer a { color: var(--cl-primary); }
</style>
