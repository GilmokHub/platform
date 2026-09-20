import { Outlet, Link, useLocation } from 'react-router-dom'
import PasskeyModal from './PasskeyModal'
import { PASSKEY_STORAGE_KEY } from '../../api/client'

export default function Layout() {
  const location = useLocation()

  const handleLogout = () => {
    if (window.confirm('PassKey 인증을 해제하고 콘솔을 잠그시겠습니까?')) {
      sessionStorage.removeItem(PASSKEY_STORAGE_KEY)
      window.location.reload()
    }
  }

  return (
    <div className="admin-layout">
      <PasskeyModal />
      <div className="admin-main">
        <header className="admin-header d-flex align-items-center justify-content-between px-4 py-2 border-bottom bg-white">
          <div className="d-flex align-items-center gap-4">
            <Link to="/monitoring" className="d-flex align-items-center gap-2 text-decoration-none text-dark fw-semibold">
              <img src="/logo.png" alt="gilmok" width={28} height={28} />
              <span>Gilmok SaaS Console</span>
            </Link>
            <nav className="d-flex gap-2">
              <Link
                to="/monitoring"
                className={`btn btn-sm ${location.pathname.startsWith('/monitoring') ? 'btn-primary' : 'btn-outline-secondary'}`}
              >
                실시간 모니터링
              </Link>
              <Link
                to="/events"
                className={`btn btn-sm ${location.pathname.startsWith('/events') ? 'btn-primary' : 'btn-outline-secondary'}`}
              >
                이벤트 및 정책 설정
              </Link>
            </nav>
          </div>
          <div className="d-flex align-items-center gap-3">
            <span className="badge bg-success">Platform Connected (:8082)</span>
            <a
              href="http://localhost:3030"
              className="btn btn-outline-secondary btn-sm d-flex align-items-center gap-1"
            >
              <span>고객사 데모 (3030)</span>
              <span className="small">→</span>
            </a>
            <button
              type="button"
              onClick={handleLogout}
              className="btn btn-outline-danger btn-sm d-flex align-items-center gap-1"
              title="PassKey 삭제 및 콘솔 잠금"
            >
              <span>인증 해제</span>
            </button>
          </div>
        </header>
        <main className="admin-content p-4">
          <Outlet />
        </main>
        <footer className="admin-footer text-center py-3 border-top text-muted small">
          <span>© 2026 Gilmok Queue SaaS Platform. All rights reserved.</span>
        </footer>
      </div>
    </div>
  )
}
