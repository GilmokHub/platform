import { useNavigate, useParams } from 'react-router-dom'

export default function Monitoring() {
  const navigate = useNavigate()
  const { eventId } = useParams()
  const GRAFANA_BASE_URL = (import.meta.env.VITE_GRAFANA_BASE_URL)
      .trim()
      .replace(/\/+$/, '')

  const DASH_UID = (import.meta.env.VITE_GRAFANA_DASHBOARD_UID).trim()

  return (
    <div className="d-flex flex-column gap-4">
      <div className="d-flex justify-content-between align-items-start flex-wrap gap-2 mb-0">
        <div>
          <h2 className="h5 fw-semibold mb-1">모니터링</h2>
          <p className="text-muted mb-0">대기열의 실시간 RPS, 에러율, 대기열 길이 등을 모니터링합니다.</p>
        </div>
        {eventId && (
          <button
            type="button"
            className="btn btn-primary btn-sm"
            onClick={() => navigate(`/events/${eventId}/ai-recommendation`)}
          >
            AI 트래픽 분석 및 정책 추천
          </button>
        )}
      </div>

      {/* Grafana 메트릭 카드 */}
      <div className="card">
        <div className="card-body">
          <div className="d-flex justify-content-between align-items-center mb-3">
            <div>
              <h2 className="h5 fw-semibold mb-1">시스템 메트릭 (Grafana & Loki)</h2>
              <p className="text-muted small mb-0">
                주요 트래픽, 에러율, 지연 시간 및 비동기 수집 로그를 실시간으로 모니터링합니다.
              </p>
            </div>
          </div>
          <div className="border rounded overflow-hidden">
            {GRAFANA_BASE_URL && DASH_UID ? (
                <iframe
                    src={`${GRAFANA_BASE_URL}/d/${DASH_UID}?orgId=1&kiosk&refresh=10s`}
                    width="100%"
                    height="720"
                    frameBorder="0"
                    title="Grafana Dashboard"
                />
            ) : (
                <div className="text-center text-muted py-5">
                  Grafana URL/UID가 설정되지 않았습니다.
                </div>
            )}
          </div>
        </div>
      </div>
    </div>
  )
}

