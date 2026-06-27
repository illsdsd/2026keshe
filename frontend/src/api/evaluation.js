import request from './request'

export const submit = (data) => request.post('/evaluation', data)
export const listByUser = (id) => request.get(`/evaluation/user/${id}`)
export const reply = (id, data) => request.post(`/evaluation/${id}/reply`, data)
export const listReplies = (id) => request.get(`/evaluation/${id}/replies`)
export const reportEval = (id, data) => request.post(`/evaluation/${id}/report`, data)
