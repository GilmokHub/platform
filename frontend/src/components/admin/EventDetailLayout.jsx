import { Outlet, useParams, NavLink, useLocation, Link } from 'react-router-dom'
import { useEffect, useMemo, useState } from 'react'
import { getEvent } from '../../api/events.js'

const eventTabs = [
  { to: 'policy', label: '대기열 정책 설정' },
  { to: 'monitoring', label: '실시간 모니터링' },
  { to: 'ai-recommendation', label: 'AI 부하 분석 & 추천' },
]

export default function EventDetailLayout() {
  const { eventId } = useParams()
  const location = useLocation()
  const [eventName, setEventName] = useState('')

  useEffect(() => {
    if (!eventId) return
    let cancelled = false
    setEventName('')
    getEvent(eventId)
      .then((res) => {
        if (cancelled) return
        const ev = res?.data ?? res
        setEventName(ev?.name ?? '')
      })
      .catch(() => {
        if (!cancelled) setEventName('')
      })
    return () => {
      cancelled = true
    }
  }, [eventId])

  const currentMenuLabel = useMemo(() => {
    if (!eventId) return ''
    const prefix = `/events/${eventId}/`
    const rest = location.pathname.startsWith(prefix) ? location.pathname.slice(prefix.length) : ''
    const segment = (rest.split('/')[0] || '').trim()
    const found = eventTabs.find((t) => t.to === segment)
    return found?.label ?? ''
  }, [eventId, location.pathname])

  return (
    <>
      <div className="d-flex align-items-start justify-content-between flex-wrap gap-2 mb-3">
        <div>
          <h1 className="h4 fw-bold mb-0">{eventName || '-'}</h1>
        </div>
        <div className="d-flex align-items-center gap-2">
          {currentMenuLabel && (
            <Link to={`/events/${eventId}`} className="btn btn-outline-secondary btn-sm">
              돌아가기
            </Link>
          )}
        </div>
      </div>
      <nav className="nav nav-tabs mb-3 flex-nowrap overflow-auto">
        {eventTabs.map(({ to, label }) => (
          <NavLink
            key={to}
            to={`/events/${eventId}/${to}`}
            className={({ isActive }) =>
              `nav-link ${isActive ? 'active' : ''} text-nowrap`
            }
          >
            {label}
          </NavLink>
        ))}
      </nav>
      <Outlet />
    </>
  )
}
