import { api } from './client'

export function getEvents() {
  return api.get('/gilmok-platform/admin/queues')
}

export function getEvent(eventId) {
  return api.get(`/gilmok-platform/admin/queues/${eventId}`)
}

export function createQueue(payload) {
  return api.post('/gilmok-platform/admin/queues', payload)
}
