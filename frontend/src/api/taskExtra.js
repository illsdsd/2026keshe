import request from './request'

// 任务优先级 / 标签 / 评论
export const setPriority = (id, data) => request.put(`/task/${id}/priority`, data)
export const addTag = (id, data) => request.post(`/task/${id}/tag`, data)
export const commentTask = (id, data) => request.post(`/task/${id}/comment`, data)
export const listTaskComments = (id) => request.get(`/task/${id}/comments`)

// 子任务
export const listSubtasks = (parentId) => request.get(`/subtask/task/${parentId}`)
export const createSubtask = (data) => request.post('/subtask', data)
export const updateSubtaskStatus = (id, data) => request.put(`/subtask/${id}`, data)

// 工时
export const logWork = (data) => request.post('/worklog', data)
export const teamWorklogStat = (teamId) => request.get(`/worklog/team/${teamId}/stat`)
export const exportWorklogUrl = (teamId) => `/api/worklog/team/${teamId}/export`

// 模板
export const listTemplates = () => request.get('/task/template')
export const saveTemplate = (data) => request.post('/task/template', data)
export const deleteTemplate = (id) => request.delete(`/task/template/${id}`)

// v2 申请
export const batchApply = (list) => request.post('/apply/batch', list)
export const cancelApply = (id) => request.put(`/apply/${id}/cancel`)
export const applyStat = () => request.get('/apply/stat')
