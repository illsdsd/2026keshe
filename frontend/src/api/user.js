import request from './request'

export const getMe = () => request.get('/user/me')

export const getUser = (id) => request.get(`/user/${id}`)

export const updateProfile = (data) => request.put('/user/me', data)

export const getMySkills = () => request.get('/user/me/skills')

export const getUserSkills = (id) => request.get(`/user/${id}/skills`)

export const addSkill = (skillId) => request.post(`/user/me/skills/${skillId}`)

export const removeSkill = (skillId) => request.delete(`/user/me/skills/${skillId}`)
