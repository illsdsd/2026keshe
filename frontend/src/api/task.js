import request from './request'

export const listTeamTasks = (teamId) => request.get(`/task/team/${teamId}`)

export const taskStat = (teamId) => request.get(`/task/team/${teamId}/stat`)

export const createTask = (data) => request.post('/task', data)

export const updateTask = (id, data) => request.put(`/task/${id}`, data)

export const updateTaskStatus = (id, data) => request.put(`/task/${id}/status`, data)

export const deleteTask = (id) => request.delete(`/task/${id}`)
