import { api } from './client'

export function getPolicy(eventId) {
  return api.get(`/gilmok-platform/admin/events/${eventId}/policy`)
}

export function updatePolicy(eventId, body) {
  return api.put(`/gilmok-platform/admin/events/${eventId}/policy`, body)
}

export function getPolicyHistories(eventId, page = 0, size = 10) {
  return api.get(`/gilmok-platform/admin/events/${eventId}/policy/histories?page=${page}&size=${size}`)
}

export function rollbackPolicy(eventId, historyId) {
  return api.post(`/gilmok-platform/admin/events/${eventId}/policy/rollback/${historyId}`)
}
