import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { getEvents, createQueue } from '../../api/events.js'

const statusLabel = { ACTIVE: 'ACTIVE', OPEN: 'OPEN', DRAFT: 'DRAFT', CLOSED: 'CLOSED', PAUSED: 'PAUSED' }
const statusClass = { ACTIVE: 'open', OPEN: 'open', DRAFT: 'draft', CLOSED: 'closed', PAUSED: 'closed' }

function toDateStr(ldt) {
  if (!ldt) return ''
  const d = new Date(ldt)
  return d.toISOString().slice(0, 10)
}

export default function EventManagement() {
  const [events, setEvents] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  const [showCreateModal, setShowCreateModal] = useState(false)
  const [newQueueName, setNewQueueName] = useState('')
  const [creating, setCreating] = useState(false)
  const [copiedCode, setCopiedCode] = useState(null)

  const loadEvents = async () => {
    setLoading(true)
    setError(null)
    try {
      const data = await getEvents()
      setEvents(Array.isArray(data) ? data : [])
    } catch (e) {
      setError(e.message || '목록을 불러오지 못했습니다.')
      setEvents([])
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadEvents()
  }, [])

  const handleCreateQueue = async (e) => {
    e.preventDefault()
    if (!newQueueName.trim()) return
    setCreating(true)
    setError(null)
    try {
      await createQueue({
        name: newQueueName.trim(),
      })
      setNewQueueName('')
      setShowCreateModal(false)
      await loadEvents()
    } catch (err) {
      setError(err.message || '대기열 생성에 실패했습니다.')
    } finally {
      setCreating(false)
    }
  }

  const handleCopyCode = async (code) => {
    if (!code) return
    try {
      await navigator.clipboard.writeText(code)
      setCopiedCode(code)
      setTimeout(() => setCopiedCode(null), 2000)
    } catch {
      // 클립보드 권한 실패 시 무시
    }
  }

  return (
    <>
      <div className="mb-4">
        <h1 className="h3 mb-1 fw-bold">이벤트 및 정책 관리</h1>
        <p className="text-muted mb-0">
          등록된 대기열(`queue_code`)을 확인하고, 각 대기열의 트래픽 정책 및 실시간 모니터링을 설정합니다.
        </p>
      </div>

      {error && (
        <div className="alert alert-danger alert-dismissible fade show" role="alert">
          {error}
          <button type="button" className="btn-close" onClick={() => setError(null)} aria-label="닫기" />
        </div>
      )}

      {showCreateModal && (
        <div className="card border-primary rounded-3 mb-4">
          <div className="card-body">
            <h2 className="h6 fw-bold mb-1">새 대기열 생성</h2>
            <p className="text-muted small mb-3">
              대기열 이름을 입력하면 고객사 이벤트에 연동할 고유 대기열 키(<code>q_...</code>)가 자동 발급됩니다.
            </p>
            <form onSubmit={handleCreateQueue} className="row g-3 align-items-end">
              <div className="col-md-9">
                <label className="form-label small fw-semibold" htmlFor="queue-name-input">대기열 이름</label>
                <input
                  id="queue-name-input"
                  type="text"
                  className="form-control form-control-sm"
                  placeholder="예: 싸이 흠뻑쇼 2026 대기열"
                  value={newQueueName}
                  onChange={(e) => setNewQueueName(e.target.value)}
                  required
                />
              </div>
              <div className="col-md-3 d-flex gap-2">
                <button type="submit" className="btn btn-primary btn-sm flex-grow-1" disabled={creating}>
                  {creating ? '생성 중...' : '생성 및 키 발급'}
                </button>
                <button
                  type="button"
                  className="btn btn-outline-secondary btn-sm"
                  onClick={() => setShowCreateModal(false)}
                  disabled={creating}
                >
                  취소
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      <div className="card border rounded-3">
        <div className="card-body">
          <div className="d-flex align-items-center justify-content-between mb-3">
            <div>
              <h2 className="h5 fw-semibold mb-1">등록된 대기열 목록</h2>
              <p className="text-muted small mb-0">발급된 대기열 코드(queue_code)를 복사하여 고객사 이벤트 생성 시 입력하세요.</p>
            </div>
            <div className="d-flex gap-2">
              <button
                type="button"
                className="btn btn-primary btn-sm"
                onClick={() => setShowCreateModal((prev) => !prev)}
              >
                새 대기열 생성
              </button>
              <button
                type="button"
                className="btn btn-outline-secondary btn-sm"
                onClick={loadEvents}
                disabled={loading}
              >
                새로고침
              </button>
            </div>
          </div>

          {loading ? (
            <p className="text-muted mb-0 py-3 text-center">불러오는 중...</p>
          ) : (
            <div className="table-responsive">
              <table className="table table-hover align-middle mb-0">
                <thead>
                  <tr>
                    <th>대기열 ID</th>
                    <th>이름</th>
                    <th>대기열 키 (queue_code)</th>
                    <th>상태</th>
                    <th>생성일</th>
                    <th className="text-end" style={{ width: 140 }}>대기열 설정</th>
                  </tr>
                </thead>
                <tbody>
                  {events.length === 0 ? (
                    <tr>
                      <td colSpan={6} className="text-muted text-center py-4">
                        등록된 대기열이 없습니다.
                      </td>
                    </tr>
                  ) : (
                    events.map((evt) => {
                      const id = evt.queueId ?? evt.eventId
                      return (
                        <tr key={id}>
                          <td className="fw-medium">{id}</td>
                          <td>{evt.name}</td>
                          <td>
                            <div className="d-inline-flex align-items-center gap-2">
                              <code className="text-primary bg-light px-2 py-1 rounded border">{evt.queueCode || '-'}</code>
                              {evt.queueCode && (
                                <button
                                  type="button"
                                  className="btn btn-outline-secondary btn-sm py-0 px-2"
                                  style={{ fontSize: '0.75rem' }}
                                  onClick={() => handleCopyCode(evt.queueCode)}
                                >
                                  {copiedCode === evt.queueCode ? '복사됨 ✓' : '복사'}
                                </button>
                              )}
                            </div>
                          </td>
                          <td>
                            <span className={`badge badge-status ${statusClass[evt.status] || 'draft'}`}>
                              {statusLabel[evt.status] ?? evt.status}
                            </span>
                          </td>
                          <td>{toDateStr(evt.createdAt)}</td>
                          <td className="text-end">
                            <Link
                              to={`/events/${id}`}
                              className="btn btn-sm btn-outline-primary"
                            >
                              콘솔 상세 →
                            </Link>
                          </td>
                        </tr>
                      )
                    })
                  )}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>
    </>
  )
}
