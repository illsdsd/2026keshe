import request from './request'

export const userStat = (id) => id ? request.get(`/stat/user/${id}`) : request.get('/stat/user/me')
export const userRadar = () => request.get('/stat/user/me/radar')
export const teamStat = (id) => request.get(`/stat/team/${id}`)
export const exportTeamStatUrl = (id) => `/api/stat/team/${id}/export`
export const competitionStat = (id) => request.get(`/stat/competition/${id}`)
export const platformTrends = () => request.get('/stat/platform/trends')
