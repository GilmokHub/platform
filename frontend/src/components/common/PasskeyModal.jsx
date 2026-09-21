import { useState, useEffect } from 'react'
import { PASSKEY_STORAGE_KEY, AUTH_FAILED_EVENT } from '../../api/client'

export default function PasskeyModal() {
  const [isOpen, setIsOpen] = useState(false)
  const [passkey, setPasskey] = useState('')
  const [errorMessage, setErrorMessage] = useState('')

  useEffect(() => {
    const existingKey = sessionStorage.getItem(PASSKEY_STORAGE_KEY)
    if (!existingKey) {
      setIsOpen(true)
    }

    const handleAuthFailed = () => {
      setErrorMessage('유효하지 않거나 만료된 PassKey입니다. 다시 입력해주세요.')
      setIsOpen(true)
    }

    window.addEventListener(AUTH_FAILED_EVENT, handleAuthFailed)
    return () => window.removeEventListener(AUTH_FAILED_EVENT, handleAuthFailed)
  }, [])

  const handleSubmit = (e) => {
    e?.preventDefault()
    const trimmed = passkey.trim()
    if (!trimmed) {
      setErrorMessage('PassKey를 입력해주세요.')
      return
    }

    sessionStorage.setItem(PASSKEY_STORAGE_KEY, trimmed)
    setErrorMessage('')
    setIsOpen(false)
    window.location.reload()
  }

  if (!isOpen) return null

  return (
    <div
      className="modal show d-block"
      tabIndex="-1"
      style={{ backgroundColor: 'rgba(0, 0, 0, 0.65)', backdropFilter: 'blur(3px)', zIndex: 9999 }}
    >
      <div className="modal-dialog modal-dialog-centered" style={{ maxWidth: '440px' }}>
        <div className="modal-content shadow-lg border-0 rounded-3">
          <div className="modal-header border-bottom-0 pt-4 px-4 pb-1">
            <h5 className="modal-title fw-bold d-flex align-items-center gap-2">
              <span>🔐</span>
              <span>관리자 PassKey 인증</span>
            </h5>
          </div>
          <div className="modal-body px-4 py-3">
            <p className="text-muted small mb-3">
              Gilmok SaaS 대기열 콘솔에 접근하려면 사전에 발급받은 고객사 관리자 전용 PassKey를 입력해야 합니다.
            </p>
            <form onSubmit={handleSubmit}>
              <div className="mb-3">
                <label className="form-label small fw-semibold text-secondary">PassKey</label>
                <input
                  type="password"
                  className="form-control form-control-lg fs-6"
                  placeholder="PassKey를 입력하세요"
                  value={passkey}
                  onChange={(e) => {
                    setPasskey(e.target.value)
                    if (errorMessage) setErrorMessage('')
                  }}
                  autoFocus
                />
                {errorMessage && (
                  <div className="text-danger small mt-2 d-flex align-items-center gap-1">
                    <span>⚠️</span>
                    <span>{errorMessage}</span>
                  </div>
                )}
              </div>
              <button
                type="submit"
                className="btn btn-primary btn-lg w-100 fs-6 fw-semibold py-2 mt-2 shadow-sm"
              >
                콘솔 인증 및 접속
              </button>
            </form>
          </div>
          <div className="modal-footer border-top-0 px-4 pb-4 pt-1 justify-content-center">
            <span className="text-muted small" style={{ fontSize: '0.75rem' }}>
              미승인된 접근은 백엔드 인터셉터에 의해 401로 원천 차단됩니다.
            </span>
          </div>
        </div>
      </div>
    </div>
  )
}
