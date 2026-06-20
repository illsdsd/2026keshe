import request from './request'

export const pageCompetitions = (params) =>
  request.get('/competition', { params })

export const getCompetition = (id) => request.get(`/competition/${id}`)

export const createCompetition = (data) => request.post('/competition', data)

export const updateCompetition = (id, data) => request.put(`/competition/${id}`, data)

export const deleteCompetition = (id) => request.delete(`/competition/${id}`)
