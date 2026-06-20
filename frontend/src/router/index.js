import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/user'

const routes = [
  { path: '/login', name: 'Login', component: () => import('@/views/Login.vue'), meta: { public: true } },
  { path: '/register', name: 'Register', component: () => import('@/views/Register.vue'), meta: { public: true } },
  {
    path: '/',
    component: () => import('@/layout/MainLayout.vue'),
    redirect: '/teams',
    children: [
      { path: 'teams', name: 'TeamList', component: () => import('@/views/team/TeamList.vue'), meta: { title: '找队伍' } },
      { path: 'teams/create', name: 'TeamCreate', component: () => import('@/views/team/TeamCreate.vue'), meta: { title: '创建队伍' } },
      { path: 'teams/:id', name: 'TeamDetail', component: () => import('@/views/team/TeamDetail.vue'), meta: { title: '队伍详情' } },
      { path: 'competitions', name: 'CompetitionList', component: () => import('@/views/competition/CompetitionList.vue'), meta: { title: '竞赛' } },
      { path: 'mine', name: 'MyTeams', component: () => import('@/views/team/MyTeams.vue'), meta: { title: '我的队伍' } },
      { path: 'profile', name: 'Profile', component: () => import('@/views/Profile.vue'), meta: { title: '个人主页' } },
      { path: 'user/:id', name: 'UserProfile', component: () => import('@/views/Profile.vue'), meta: { title: '用户主页' } }
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: '/' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 全局前置守卫：未登录跳转登录页
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
