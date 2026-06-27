<template>
  <div class="page-container">
    <div class="cl-hero">
      <h1>账号安全</h1>
      <p>修改密码、更换邮箱、隐私设置一站式管理</p>
    </div>

    <el-tabs v-model="tab" type="border-card">
      <el-tab-pane label="修改密码" name="password">
        <el-form :model="pwd" :rules="pwdRules" ref="pwdRef" label-width="100px">
          <el-form-item label="原密码" prop="oldPassword">
            <el-input v-model="pwd.oldPassword" type="password" show-password />
          </el-form-item>
          <el-form-item label="新密码" prop="newPassword">
            <el-input v-model="pwd.newPassword" type="password" show-password />
          </el-form-item>
          <el-button type="primary" :loading="pwdLoading" @click="submitPwd">保存</el-button>
        </el-form>
      </el-tab-pane>

      <el-tab-pane label="隐私设置" name="privacy">
        <div v-if="privacy" class="privacy-list">
          <div class="privacy-item">
            <span>主页对其他用户公开</span>
            <el-switch v-model="privacy.profilePublic" :active-value="1" :inactive-value="0" />
          </div>
          <div class="privacy-item">
            <span>信誉分对外展示</span>
            <el-switch v-model="privacy.reputationPublic" :active-value="1" :inactive-value="0" />
          </div>
          <el-divider content-position="left">推送通知</el-divider>
          <div class="privacy-item">
            <span>申请通知</span>
            <el-switch v-model="privacy.pushApply" :active-value="1" :inactive-value="0" />
          </div>
          <div class="privacy-item">
            <span>审核通知</span>
            <el-switch v-model="privacy.pushAudit" :active-value="1" :inactive-value="0" />
          </div>
          <div class="privacy-item">
            <span>任务通知</span>
            <el-switch v-model="privacy.pushTask" :active-value="1" :inactive-value="0" />
          </div>
          <div class="privacy-item">
            <span>公告通知</span>
            <el-switch v-model="privacy.pushNotice" :active-value="1" :inactive-value="0" />
          </div>
          <div class="privacy-item">
            <span>评价通知</span>
            <el-switch v-model="privacy.pushEval" :active-value="1" :inactive-value="0" />
          </div>
          <el-button type="primary" @click="savePrivacy">保存隐私设置</el-button>
        </div>
      </el-tab-pane>

      <el-tab-pane label="登录日志" name="log">
        <router-link to="/login-log">
          <el-button type="primary" plain>
            <el-icon><DataLine /></el-icon> 查看完整登录日志
          </el-button>
        </router-link>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { updatePassword, getPrivacy, updatePrivacy } from '@/api/user'

const tab = ref('password')
const pwdRef = ref()
const pwdLoading = ref(false)
const pwd = reactive({ oldPassword: '', newPassword: '' })
const pwdRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [{ required: true, min: 6, message: '至少 6 位', trigger: 'blur' }]
}
const privacy = ref(null)

async function submitPwd() {
  await pwdRef.value.validate()
  pwdLoading.value = true
  try {
    await updatePassword(pwd)
    ElMessage.success('密码已修改')
    pwd.oldPassword = ''
    pwd.newPassword = ''
  } finally { pwdLoading.value = false }
}

async function loadPrivacy() {
  privacy.value = await getPrivacy()
}

async function savePrivacy() {
  await updatePrivacy(privacy.value)
  ElMessage.success('隐私设置已保存')
}

onMounted(loadPrivacy)
</script>

<style scoped>
.privacy-list { display: flex; flex-direction: column; gap: 12px; padding: 8px 0; }
.privacy-item { display: flex; justify-content: space-between; align-items: center; padding: 12px 16px; background: #f8fafc; border-radius: 12px; }
</style>
