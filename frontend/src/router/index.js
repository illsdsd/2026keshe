import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/user'

const routes = [
  { path: '/login', name: 'Login', component: () => import('@/views/Login.vue'), meta: { public: true } },
  { path: '/register', name: 'Register', component: () => import('@/views/Register.vue'), meta: { public: true } },
  { path: '/forget-password', name: 'ForgetPassword', component: () => import('@/views/ForgetPassword.vue'), meta: { public: true } },
  {
    path: '/',
    component: () => import('@/layout/MainLayout.vue'),
    redirect: '/teams',
    children: [
      { path: 'teams', name: 'TeamList', component: () => import('@/views/team/TeamList.vue'), meta: { title: '找队伍' } },
      { path: 'teams/create', name: 'TeamCreate', component: () => import('@/views/team/TeamCreate.vue'), meta: { title: '创建队伍' } },
      { path: 'teams/archive', name: 'TeamArchiveList', component: () => import('@/views/team/TeamArchiveList.vue'), meta: { title: '归档队伍' } },
      { path: 'teams/:id', name: 'TeamDetail', component: () => import('@/views/team/TeamDetail.vue'), meta: { title: '队伍详情' } },
      { path: 'teams/:id/applies', name: 'TeamApplies', component: () => import('@/views/team/TeamApplies.vue'), meta: { title: '申请管理' } },
      { path: 'teams/:id/board', name: 'TeamBoard', component: () => import('@/views/team/TeamBoard.vue'), meta: { title: '任务看板' } },
      { path: 'teams/:id/notices', name: 'TeamNotices', component: () => import('@/views/team/TeamNotices.vue'), meta: { title: '队伍公告' } },
      { path: 'teams/:id/files', name: 'TeamFileLibrary', component: () => import('@/views/team/TeamFileLibrary.vue'), meta: { title: '队伍文件库' } },
      { path: 'teams/:id/posts', name: 'TeamPostWall', component: () => import('@/views/team/TeamPostWall.vue'), meta: { title: '动态墙' } },
      { path: 'teams/:id/blacklist', name: 'TeamBlacklist', component: () => import('@/views/team/TeamBlacklist.vue'), meta: { title: '队伍黑名单' } },
      { path: 'teams/:id/gantt', name: 'TeamGantt', component: () => import('@/views/team/TeamGantt.vue'), meta: { title: '甘特图' } },
      { path: 'teams/:id/worklog', name: 'TeamWorklog', component: () => import('@/views/team/TeamWorklog.vue'), meta: { title: '工时填报' } },
      { path: 'teams/:id/stat', name: 'TeamStat', component: () => import('@/views/team/TeamStat.vue'), meta: { title: '队伍统计' } },
      { path: 'competitions', name: 'CompetitionList', component: () => import('@/views/competition/CompetitionList.vue'), meta: { title: '竞赛' } },
      { path: 'competitions/ranking', name: 'CompetitionRanking', component: () => import('@/views/competition/CompetitionRanking.vue'), meta: { title: '赛事排行' } },
      { path: 'competitions/:id', name: 'CompetitionDetail', component: () => import('@/views/competition/CompetitionDetail.vue'), meta: { title: '竞赛详情' } },
      { path: 'mine', name: 'MyTeams', component: () => import('@/views/team/MyTeams.vue'), meta: { title: '我的队伍' } },
      { path: 'my-applications', name: 'MyApplications', component: () => import('@/views/MyApplications.vue'), meta: { title: '我的申请' } },
      { path: 'profile', name: 'Profile', component: () => import('@/views/Profile.vue'), meta: { title: '个人主页' } },
      { path: 'user/:id', name: 'UserProfile', component: () => import('@/views/Profile.vue'), meta: { title: '用户主页' } },
      { path: 'security', name: 'SecuritySettings', component: () => import('@/views/account/SecuritySettings.vue'), meta: { title: '账号安全' } },
      { path: 'login-log', name: 'LoginLog', component: () => import('@/views/account/LoginLog.vue'), meta: { title: '登录日志' } },
      { path: 'projects', name: 'ProjectGallery', component: () => import('@/views/account/ProjectGallery.vue'), meta: { title: '作品集' } },
      { path: 'favorites', name: 'Favorites', component: () => import('@/views/account/Favorites.vue'), meta: { title: '我的收藏' } },
      { path: 'reputation', name: 'ReputationDetail', component: () => import('@/views/account/ReputationDetail.vue'), meta: { title: '信誉分明细' } },
      { path: 'reputation/ranking', name: 'ReputationRanking', component: () => import('@/views/account/ReputationRanking.vue'), meta: { title: '信誉分榜单' } },
      { path: 'messages', name: 'MessageCenter', component: () => import('@/views/message/MessageCenter.vue'), meta: { title: '消息中心' } },
      { path: 'file-center', name: 'FileCenter', component: () => import('@/views/file/FileCenter.vue'), meta: { title: '文件中心' } },
      { path: 'user-stat', name: 'UserStat', component: () => import('@/views/stat/UserStat.vue'), meta: { title: '我的数据' } },
      { path: 'admin', name: 'AdminLayout', component: () => import('@/views/admin/AdminLayout.vue'), redirect: '/admin/dashboard',
        children: [
          { path: 'dashboard', name: 'AdminDashboard', component: () => import('@/views/admin/AdminDashboard.vue'), meta: { title: '数据大盘' } },
          { path: 'users', name: 'AdminUsers', component: () => import('@/views/admin/AdminUsers.vue'), meta: { title: '用户管理' } },
          { path: 'teams', name: 'AdminTeams', component: () => import('@/views/admin/AdminTeams.vue'), meta: { title: '队伍管理' } },
          { path: 'reports', name: 'AdminReports', component: () => import('@/views/admin/AdminReports.vue'), meta: { title: '举报处理' } },
          { path: 'skill', name: 'AdminSkill', component: () => import('@/views/admin/AdminSkill.vue'), meta: { title: '技能标签' } },
          { path: 'notice', name: 'AdminNotice', component: () => import('@/views/admin/AdminNotice.vue'), meta: { title: '系统公告' } }
        ]
      }
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: '/' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  if (to.meta.public) {
    next()
  } else if (!userStore.isLogin) {
    next({ path: '/login', query: { redirect: to.fullPath } })
  } else {
    next()
  }
})

export default router
