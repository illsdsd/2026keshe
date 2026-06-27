import request from './request'

// 队伍文件库
export const listTeamFiles = (id, folder) => request.get(`/team/${id}/files`, { params: { folder } })
export const addTeamFile = (id, data) => request.post(`/team/${id}/files`, data)
export const deleteTeamFile = (id) => request.delete(`/team-file/${id}`)

// 动态墙
export const pageTeamPosts = (id, params) => request.get(`/team/${id}/posts`, { params })
export const publishTeamPost = (id, data) => request.post(`/team/${id}/posts`, data)
export const commentPost = (id, data) => request.post(`/team-post/${id}/comment`, data)
export const likePost = (id) => request.post(`/team-post/${id}/like`)

// 副队长 / 归档 / 招募置顶
export const promoteDeputy = (memberId) => request.put(`/team-member/${memberId}/deputy`)
export const archiveTeam = (id) => request.put(`/team/${id}/archive`)
export const topRecruit = (id) => request.put(`/team-recruit/${id}/top`)

// 黑名单
export const addBlacklist = (data) => request.post('/team-blacklist', data)
export const listBlacklist = (id) => request.get(`/team/${id}/blacklist`)
export const removeBlacklist = (id) => request.delete(`/team-blacklist/${id}`)
