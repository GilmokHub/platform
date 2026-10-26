import { api } from './client'

export function getEvents() {
  return api.get('/admin/queues')
}

export function getEvent(eventId) {
  return api.get(`/admin/queues/${eventId}`)
}

export function createQueue(payload) {
  return api.post('/admin/queues', payload)
}
