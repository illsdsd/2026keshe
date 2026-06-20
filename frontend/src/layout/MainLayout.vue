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
          <el-menu-item index="/teams">找队伍</el-menu-item>
          <el-menu-item index="/competitions">竞赛</el-menu-item>
          <el-menu-item index="/mine">我的队伍</el-menu-item>
        </el-menu>
        <div class="navbar-right">
          <el-button type="primary" plain @click="$router.push('/teams/create')">
            <el-icon><Plus /></el-icon> 创建队伍
          </el-button>
          <el-dropdown @command="onCommand">
            <span class="user-trigger">
              <el-avatar :size="32" :src="user?.avatar">
                {{ user?.nickname?.charAt(0) }}
              </el-avatar>
              <span class="nickname">{{ user?.nickname }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人主页</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </header>
    <main class="content">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const user = computed(() => userStore.user)
const activeMenu = computed(() => '/' + (route.path.split('/')[1] || 'teams'))

function onCommand(command) {
  if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'logout') {
    userStore.logout()
    router.push('/login')
  }
}
</script>

<style scoped>
.navbar {
  background: #fff;
  border-bottom: 1px solid #ebeef5;
  position: sticky;
  top: 0;
  z-index: 100;
}
.navbar-inner {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  height: 60px;
  padding: 0 16px;
  gap: 24px;
}
.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 20px;
  font-weight: 700;
  color: var(--cl-primary);
  cursor: pointer;
}
.nav-menu {
  flex: 1;
  border-bottom: none;
}
.navbar-right {
  display: flex;
  align-items: center;
  gap: 16px;
}
.user-trigger {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  outline: none;
}
.nickname {
  font-size: 14px;
}
.content {
  min-height: calc(100vh - 60px);
}
</style>
