import request from './request'

export const listTeamNotices = (teamId) => request.get(`/notice/team/${teamId}`)

export const publishNotice = (data) => request.post('/notice', data)

export const deleteNotice = (id) => request.delete(`/notice/${id}`)
