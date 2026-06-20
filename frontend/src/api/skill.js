import request from './request'

export const listSkills = (category) =>
  request.get('/skill', { params: { category } })
