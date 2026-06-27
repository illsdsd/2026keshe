import request from './request'

// v2 赛事报名
export const registerCompetition = (id, data) => request.post(`/competition/${id}/register`, data)
export const auditRegister = (registerId, data) => request.put(`/competition/register/${registerId}/audit`, data)
export const listRegisters = (id, params) => request.get(`/competition/${id}/register`, { params })

// v2 赛事附件
export const listAttachments = (id) => request.get(`/competition/${id}/attachments`)
export const addAttachment = (id, data) => request.post(`/competition/${id}/attachments`, data)
export const deleteAttachment = (id) => request.delete(`/competition/attachment/${id}`)

// v2 赛事资讯
export const listNews = (id) => request.get(`/competition/${id}/news`)
export const publishNews = (id, data) => request.post(`/competition/${id}/news`, data)

// v2 赛事排行榜
export const competitionRanking = () => request.get('/competition/ranking')
