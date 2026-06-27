import request from './request'

// 消息中心
export const listMessages = (params) => request.get('/message', { params })
export const unreadCount = () => request.get('/message/unread/count')
export const markRead = (id) => request.put(`/message/${id}/read`)
export const markAllRead = () => request.put('/message/read-all')
export const batchRead = (ids) => request.put('/message/batch-read', { ids })
export const batchDeleteMsg = (ids) => request.delete('/message/batch', { data: { ids } })
export const searchMessage = (params) => request.get('/message/search', { params })

// 定时公告
export const scheduleNotice = (data) => request.post('/notice/schedule', data)
