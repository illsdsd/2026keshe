import request from './request'

export const register = (data) => request.post('/auth/register', data)

export const login = (data) => request.post('/auth/login', data)

export const logout = () => request.post('/auth/logout')

// v2 找回密码
export const sendForgetCode = (data) => request.post('/auth/password/forget', data)
export const resetPassword = (data) => request.post('/auth/password/reset', data)

