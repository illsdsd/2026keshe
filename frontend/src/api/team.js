import request from './request'

export const pageTeams = (params) => request.get('/team', { params })

export const getTeam = (id) => request.get(`/team/${id}`)

export const createTeam = (data) => request.post('/team', data)

export const updateTeam = (id, data) => request.put(`/team/${id}`, data)

export const deleteTeam = (id) => request.delete(`/team/${id}`)

export const myTeams = () => request.get('/team/mine')
