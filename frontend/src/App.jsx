import { Routes, Route, Navigate } from 'react-router-dom'
import Layout from './components/common/Layout.jsx'
import EventManagement from './pages/admin/EventManagement.jsx'
import EventDetail from './pages/admin/EventDetail.jsx'
import EventDetailLayout from './components/admin/EventDetailLayout.jsx'
import PolicySettings from './pages/admin/PolicySettings.jsx'
import Monitoring from './pages/admin/Monitoring.jsx'
import AIRecommendation from './pages/admin/AIRecommendation.jsx'
export default function App() {
  return (
    <Routes>
      <Route element={<Layout />}>
        {/* 길목 대기열 플랫폼 콘솔의 기본 홈은 실시간 모니터링입니다 */}
        <Route index element={<Navigate to="/monitoring" replace />} />
        <Route path="/monitoring" element={<Monitoring />} />
        
        {/* 이벤트별 대기열 정책 및 모니터링 설정 */}
        <Route path="/events" element={<EventManagement />} />
        <Route path="/events/:eventId" element={<EventDetailLayout />}>
          <Route index element={<EventDetail />} />
          <Route path="policy" element={<PolicySettings />} />
          <Route path="monitoring" element={<Monitoring />} />
          <Route path="ai-recommendation" element={<AIRecommendation />} />
        </Route>
      </Route>
      <Route path="*" element={<Navigate to="/monitoring" replace />} />
    </Routes>
  )
}
