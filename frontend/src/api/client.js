const API_BASE = import.meta.env.VITE_API_BASE_URL || ''
export const PASSKEY_STORAGE_KEY = 'gilmok_admin_passkey'
export const AUTH_FAILED_EVENT = 'gilmok-admin-auth-failed'

/**
 * 길목 대기열 SaaS 플랫폼 공통 API 클라이언트
 * - JSON 자동 직렬화/역직렬화 및 data 필드 언래핑
 * - sessionStorage 기반 B2B 관리자 PassKey(X-Platform-Admin-Key) 주입
 * - 401 Unauthorized 수신 시 자동 세션 만료 및 재인증 이벤트 발생
 */
async function request(path, options = {}) {
    const url = `${API_BASE}${path}`
    const passkey = sessionStorage.getItem(PASSKEY_STORAGE_KEY) || ''

    const fetchOptions = {
        ...options,
        headers: {
            'Content-Type': 'application/json',
            'X-Platform-Admin-Key': passkey,
            ...options.headers,
        },
    }

    const res = await fetch(url, fetchOptions)

    if (res.status === 401) {
        sessionStorage.removeItem(PASSKEY_STORAGE_KEY)
        window.dispatchEvent(new Event(AUTH_FAILED_EVENT))
    }

    const json = await res.json().catch(() => ({}))

    if (!res.ok) {
        const err = new Error(json.message || res.statusText || '요청 실패')
        err.status = res.status
        err.code = json.code
        throw err
    }

    return json.data !== undefined ? json.data : json
}

export const api = {
    get: (path, headers, opts) => request(path, { method: 'GET', headers, ...opts }),
    post: (path, body, opts) => request(path, {
        method: 'POST',
        body: body !== undefined ? JSON.stringify(body) : undefined,
        ...opts,
    }),
    put: (path, body, opts) => request(path, {
        method: 'PUT',
        body: body !== undefined ? JSON.stringify(body) : undefined,
        ...opts,
    }),
    delete: (path, opts) => request(path, { method: 'DELETE', ...opts }),
}