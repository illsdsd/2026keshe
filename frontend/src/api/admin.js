import request from './request'

// 用户管理
export const adminUsers = (params) => request.get('/admin/users', { params })
export const adminDisableUser = (id) => request.put(`/admin/users/${id}/disable`)
export const adminEnableUser = (id) => request.put(`/admin/users/${id}/enable`)
export const adminResetPwd = (id, data) => request.put(`/admin/users/${id}/reset-password`, data)
export const adminExportUsersUrl = () => '/api/admin/users/export'

// 队伍管理
export const adminTeams = (params) => request.get('/admin/teams', { params })
export const adminDisbandTeam = (id) => request.delete(`/admin/teams/${id}`)

// 举报
export const adminReports = (params) => request.get('/admin/reports', { params })
export const adminHandleReport = (id, data) => request.put(`/admin/reports/${id}/handle`, data)

// 大盘
export const adminDashboard = () => request.get('/admin/dashboard')

// 平台公告
export const adminPublishNotice = (data) => request.post('/admin/notice', data)
export const adminListNotices = () => request.get('/admin/notice')

// 技能
export const adminCreateSkill = (data) => request.post('/admin/skill', data)
export const adminUpdateSkill = (id, data) => request.put(`/admin/skill/${id}`, data)
export const adminDeleteSkill = (id) => request.delete(`/admin/skill/${id}`)

// 字典
export const dictByType = (type) => request.get(`/dict/${type}`)
export const dictAll = () => request.get('/dict/all')
