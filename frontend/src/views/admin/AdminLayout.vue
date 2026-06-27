<template>
  <div class="admin-layout">
    <aside class="side">
      <div class="side-title">管理后台</div>
      <el-menu :default-active="$route.path" router class="side-menu">
        <el-menu-item index="/admin/dashboard"><el-icon><DataLine /></el-icon> 数据大盘</el-menu-item>
        <el-menu-item index="/admin/users"><el-icon><User /></el-icon> 用户管理</el-menu-item>
        <el-menu-item index="/admin/teams"><el-icon><UserFilled /></el-icon> 队伍管理</el-menu-item>
        <el-menu-item index="/admin/reports"><el-icon><Warning /></el-icon> 举报处理</el-menu-item>
        <el-menu-item index="/admin/skill"><el-icon><Star /></el-icon> 技能标签</el-menu-item>
        <el-menu-item index="/admin/notice"><el-icon><Bell /></el-icon> 系统公告</el-menu-item>
      </el-menu>
    </aside>
    <section class="main">
      <router-view />
    </section>
  </div>
</template>

<style scoped>
.admin-layout { display: grid; grid-template-columns: 220px 1fr; min-height: calc(100vh - 68px); }
.side { background: var(--cl-bg-card); border-right: 1px solid var(--cl-border); padding: 16px 8px; }
.side-title { padding: 8px 16px; font-weight: 700; color: var(--cl-text); margin-bottom: 8px; font-size: 18px; }
.side-menu { border: none; background: transparent; }
.side-menu :deep(.el-menu-item) { border-radius: 10px; margin: 4px 6px; height: 44px; line-height: 44px; }
.side-menu :deep(.el-menu-item.is-active) { background: var(--cl-primary-soft); color: var(--cl-primary); }
.main { padding: 24px; overflow-y: auto; }
</style>
