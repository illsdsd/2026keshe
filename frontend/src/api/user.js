import request from './request'

export const getMe = () => request.get('/user/me')

export const getUser = (id) => request.get(`/user/${id}`)

export const updateProfile = (data) => request.put('/user/me', data)

export const getMySkills = () => request.get('/user/me/skills')

export const getUserSkills = (id) => request.get(`/user/${id}/skills`)

export const addSkill = (skillId) => request.post(`/user/me/skills/${skillId}`)

export const removeSkill = (skillId) => request.delete(`/user/me/skills/${skillId}`)

// v2 账号安全
export const updatePassword = (data) => request.put('/user/me/password', data)
export const updateEmail = (data) => request.put('/user/me/email', data)
export const getPrivacy = () => request.get('/user/me/privacy')
export const updatePrivacy = (data) => request.put('/user/me/privacy', data)
export const getLoginLogs = (params) => request.get('/user/me/login-log', { params })

// v2 作品集
export const listProjects = (id) => id ? request.get(`/user/${id}/projects`) : request.get('/user/me/projects')
export const createProject = (data) => request.post('/user/me/projects', data)
export const updateProject = (id, data) => request.put(`/user/me/projects/${id}`, data)
export const deleteProject = (id) => request.delete(`/user/me/projects/${id}`)

// v2 证书
export const listCertificates = () => request.get('/user/me/certificates')
export const addCertificate = (data) => request.post('/user/me/certificates', data)
export const deleteCertificate = (id) => request.delete(`/user/me/certificates/${id}`)

// v2 收藏
export const listFavorites = (refType) => request.get('/user/me/favorites', { params: { refType } })
export const addFavorite = (data) => request.post('/user/me/favorites', data)
export const removeFavorite = (id) => request.delete(`/user/me/favorites/${id}`)

// v2 信誉分
export const getReputationDetail = (id) => request.get(`/user/${id}/reputation-detail`)
export const reputationRanking = (limit = 50) => request.get('/reputation/ranking', { params: { limit } })
