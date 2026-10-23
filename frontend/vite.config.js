import {defineConfig, loadEnv} from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig(({mode}) => {
    const env = loadEnv(mode, process.cwd(), '')

    const bypassHtml = (req) => {
        if (req.headers.accept?.includes('text/html')) return '/index.html'
    }

    // 길목 대기열 플랫폼 코어 서버 (backend) - 포트 8082
    const platformServerProxy = {
        target: env.VITE_QUEUE_API_URL || 'http://localhost:8082',
        changeOrigin: true,
        bypass: bypassHtml,
    }

    return {
        plugins: [react()],
        server: {
            port: 3031,
            proxy: {
                // 길목 대기열 플랫폼 전용 엔드포인트 (:8082, context-path: /gilmok-platform)
                '/gilmok-platform': platformServerProxy,
            },
        },
    }
})
