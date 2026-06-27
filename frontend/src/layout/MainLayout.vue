<template>
  <div class="layout">
    <header class="navbar">
      <div class="navbar-inner">
        <div class="logo" @click="$router.push('/teams')">
          <el-icon><Connection /></el-icon>
          <span>CampusLink</span>
        </div>
        <el-menu
          class="nav-menu"
          mode="horizontal"
          :default-active="activeMenu"
          :ellipsis="false"
          router
        >
          <el-menu-item index="/teams">
            <el-icon><UserFilled /></el-icon>找队伍
          </el-menu-item>
          <el-menu-item index="/competitions">
            <el-icon><Trophy /></el-icon>竞赛
          </el-menu-item>
          <el-menu-item index="/mine">
            <el-icon><Coordinate /></el-icon>我的队伍
          </el-menu-item>
          <el-menu-item index="/file-center">
            <el-icon><Files /></el-icon>文件中心
          </el-menu-item>
          <el-menu-item index="/user-stat">
            <el-icon><DataLine /></el-icon>数据
          </el-menu-item>
          <el-menu-item v-if="isAdmin" index="/admin">
            <el-icon><Setting /></el-icon>管理后台
          </el-menu-item>
        </el-menu>
        <div class="navbar-right">
          <el-button class="cl-btn-hero" @click="$router.push('/teams/create')">
            <el-icon><Plus /></el-icon> 创建队伍
          </el-button>

          <el-badge :value="unread" :hidden="!unread" class="msg-badge">
            <el-button text @click="$router.push('/messages')">
              <el-icon size="20"><Bell /></el-icon>
            </el-button>
          </el-badge>

          <el-dropdown @command="onCommand">
            <span class="user-trigger">
              <el-avatar :size="34" :src="user?.avatar" class="cl-avatar">
                {{ user?.nickname?.charAt(0) }}
              </el-avatar>
              <div class="user-meta">
                <span class="nickname">{{ user?.nickname }}</span>
                <span class="reputation">信誉分 {{ user?.reputation }}</span>
              </div>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile" :icon="User">个人主页</el-dropdown-item>
                <el-dropdown-item command="security" :icon="Lock">账号安全</el-dropdown-item>
                <el-dropdown-item command="applications" :icon="Document">我的申请</el-dropdown-item>
                <el-dropdown-item command="favorites" :icon="Star">我的收藏</el-dropdown-item>
                <el-dropdown-item command="messages" :icon="Bell">消息中心</el-dropdown-item>
                <el-dropdown-item divided command="logout" :icon="SwitchButton">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </header>
    <main class="content cl-animate">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { unreadCount } from '@/api/messageExtra'
import { User, Lock, Document, Star, Bell, SwitchButton } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const user = computed(() => userStore.user)
const activeMenu = computed(() => '/' + (route.path.split('/')[1] || 'teams'))
const isAdmin = computed(() => user.value?.role === 'ADMIN')

const unread = ref(0)
let timer = null

async function refreshUnread() {
  if (!userStore.isLogin) return
  try {
    const data = await unreadCount()
    unread.value = data?.count || 0
  } catch (e) {
    // 忽略
  }
}

onMounted(() => {
  refreshUnread()
  timer = setInterval(refreshUnread, 60_000)
})

onBeforeUnmount(() => {
  if (timer) clearInterval(timer)
})

function onCommand(command) {
  const map = {
    profile: '/profile',
    security: '/security',
    applications: '/my-applications',
    favorites: '/favorites',
    messages: '/messages'
  }
  if (command === 'logout') {
    userStore.logout()
    router.push('/login')
    return
  }
  if (map[command]) router.push(map[command])
}
</script>

<style scoped>
.navbar {
  background: var(--cl-bg-glass);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid var(--cl-border);
  position: sticky;
  top: 0;
  z-index: 100;
  box-shadow: var(--cl-shadow-sm);
}
.navbar-inner {
  max-width: 1320px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  height: 68px;
  padding: 0 24px;
  gap: 24px;
}
.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 22px;
  font-weight: 800;
  letter-spacing: -0.2px;
  color: var(--cl-primary);
  cursor: pointer;
}
.logo .el-icon {
  font-size: 24px;
  background: var(--cl-gradient-hero);
  color: #fff;
  padding: 6px;
  border-radius: 10px;
}
.nav-menu {
  flex: 1;
  border-bottom: none !important;
  background: transparent;
}
.nav-menu :deep(.el-menu-item) {
  border-bottom: 0 !important;
  font-weight: 500;
  height: 68px;
  line-height: 68px;
  padding: 0 16px;
}
.nav-menu :deep(.el-menu-item.is-active) {
  color: var(--cl-primary) !important;
  background: var(--cl-primary-soft);
  border-radius: 12px;
  margin: 12px 4px;
  height: 44px;
  line-height: 44px;
}
.navbar-right {
  display: flex;
  align-items: center;
  gap: 14px;
}
.msg-badge :deep(.el-badge__content) {
  background: var(--cl-danger);
}
.user-trigger {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 6px 10px;
  border-radius: 12px;
  transition: background 0.2s ease;
}
.user-trigger:hover {
  background: var(--cl-primary-soft);
}
.cl-avatar {
  background: var(--cl-gradient-hero);
  color: #fff;
  font-weight: 700;
}
.user-meta {
  display: flex;
  flex-direction: column;
  line-height: 1.1;
}
.nickname {
  font-size: 14px;
  font-weight: 600;
}
.reputation {
  font-size: 11px;
  color: var(--cl-text-muted);
}
.content {
  min-height: calc(100vh - 68px);
}
</style>
