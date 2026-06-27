import request from './request'

// 通用文件
export const uploadFile = (formData, params) =>
  request.post('/file/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
    params
  })

export const uploadChunk = (formData) =>
  request.post('/file/upload-chunk', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })

export const downloadFile = (id) => `${import.meta.env.BASE_URL || ''}/api/file/${id}/download`

export const deleteFile = (id) => request.delete(`/file/${id}`)

export const myFiles = (params) => request.get('/file/mine', { params })
