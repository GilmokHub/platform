import { useState, useEffect, useCallback, useRef } from 'react';

/**
 * Gilmok Queue SaaS React Hook
 * @param {string|object} optionsOrClientKey - clientKey 문자열 또는 옵션 객체
 * @param {string} [legacyQueueUrl] - 이전 버전 호환용 queueUrl
 */
export function useGilmokQueue(optionsOrClientKey, legacyQueueUrl) {
  const options = typeof optionsOrClientKey === 'object' && optionsOrClientKey !== null
    ? optionsOrClientKey
    : { clientKey: optionsOrClientKey, queueUrl: legacyQueueUrl };

  const {
    clientKey = 'demo-client',
    queueUrl = 'http://localhost:8082',
    onAdmitted = null,
    fallbackToDirect = true // 플랫폼 장애 시 고객사 서비스 무중단을 위한 Fail-Open
  } = options;

  const onAdmittedRef = useRef(onAdmitted);
  useEffect(() => {
    onAdmittedRef.current = onAdmitted;
  }, [onAdmitted]);

  const [isWaiting, setIsWaiting] = useState(false);
  const [queueStatus, setQueueStatus] = useState(null); // { rank, expectedWaitSeconds, status, total }
  const [admissionToken, setAdmissionToken] = useState(null);
  const [queueKey, setQueueKey] = useState(null);
  const [currentEventId, setCurrentEventId] = useState(null);
  const [currentUserId, setCurrentUserId] = useState(null);
  const [error, setError] = useState(null);

  const pollIntervalRef = useRef(3000);
  const pollTimerRef = useRef(null);

  // 대기열 상태 폴링
  useEffect(() => {
    if (!isWaiting || admissionToken || !queueKey || !currentEventId) {
      if (pollTimerRef.current) clearTimeout(pollTimerRef.current);
      return;
    }

    let isSubscribed = true;

    const pollStatus = async () => {
      try {
        const queryParams = new URLSearchParams({
          clientKey,
          eventId: String(currentEventId),
          queueKey,
          userId: String(currentUserId ?? 0)
        });

        const res = await fetch(`${queueUrl}/gilmok-platform/queue/status?${queryParams.toString()}`, {
          credentials: 'omit'
        });

        if (!res.ok) {
          throw new Error(`Queue status error: ${res.status}`);
        }

        const json = await res.json();
        const data = json.data !== undefined ? json.data : json;
        console.log("[GilmokQueue] poll status:", data);

        if (!isSubscribed) return;

        const isAdmitted = data.status === 'ADMITTABLE' || data.status === 'ADMITTED';
        if (isAdmitted) {
          const admittedToken = data.admissionToken || data.token;
          setAdmissionToken(admittedToken);
          setIsWaiting(false);
          if (onAdmittedRef.current) {
            onAdmittedRef.current({ admissionToken: admittedToken, eventId: currentEventId, queueKey });
          }
          return;
        }

        // 대기 상태 갱신
        setQueueStatus({
          rank: data.rank ?? data.position ?? 0,
          expectedWaitSeconds: data.expectedWaitSeconds ?? data.etaSeconds ?? 0,
          total: data.total ?? 0,
          status: data.status
        });

        if (data.pollAfterMs && data.pollAfterMs > 0) {
          pollIntervalRef.current = data.pollAfterMs;
        }

        // 다음 폴링 스케줄링
        pollTimerRef.current = setTimeout(pollStatus, pollIntervalRef.current);

      } catch (e) {
        console.warn("[GilmokQueue] Status polling issue:", e);
        if (fallbackToDirect) {
          console.warn("[GilmokQueue] Fail-Open activated: bypassing queue on poll error");
          setIsWaiting(false);
          const mockToken = "FAIL_OPEN_DIRECT_TOKEN";
          setAdmissionToken(mockToken);
          if (onAdmittedRef.current) {
            onAdmittedRef.current({ admissionToken: mockToken, eventId: currentEventId, queueKey });
          }
        } else {
          pollTimerRef.current = setTimeout(pollStatus, pollIntervalRef.current);
        }
      }
    };

    pollTimerRef.current = setTimeout(pollStatus, 1000);

    return () => {
      isSubscribed = false;
      if (pollTimerRef.current) clearTimeout(pollTimerRef.current);
    };
  }, [isWaiting, admissionToken, queueKey, currentEventId, currentUserId, clientKey, queueUrl, fallbackToDirect]);

  /**
   * 대기열 진입
   * - 평상시(ROUTING_DISABLED): 대기 모달 없이 즉시 onAdmitted 호출 및 완료
   * - 트래픽 집중 시(ROUTING_ENABLED): isWaiting = true, 모달 표시 및 순번 대기
   */
  const enterQueue = useCallback(async (eventId, userId) => {
    setError(null);
    setCurrentEventId(eventId);
    setCurrentUserId(userId);

    try {
      const res = await fetch(`${queueUrl}/gilmok-platform/queue/enter`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          clientKey,
          eventId: String(eventId),
          userId: userId ?? 0
        })
      });

      if (!res.ok) {
        throw new Error(`Queue enter failed with status ${res.status}`);
      }

      const json = await res.json();
      const data = json.data !== undefined ? json.data : json;

      // 1. 평상시: 대기열이 필요 없는 경우(directPass 또는 즉시 통과)
      if (data.directPass || data.status === 'ADMITTABLE') {
        const admittedToken = data.admissionToken || data.token;
        setAdmissionToken(admittedToken);
        setIsWaiting(false);
        if (onAdmitted) {
          onAdmitted({ admissionToken: admittedToken, eventId, queueKey: data.queueKey });
        }
        return { directPass: true, admissionToken: admittedToken };
      }

      // 2. 트래픽 집중 시: 대기열 진입
      setQueueKey(data.queueKey);
      setQueueStatus({
        rank: data.rank ?? data.position ?? 1,
        expectedWaitSeconds: data.expectedWaitSeconds ?? data.etaSeconds ?? 0,
        status: data.status
      });
      setIsWaiting(true);
      return { directPass: false, queueKey: data.queueKey };

    } catch (e) {
      console.error("[GilmokQueue] Failed to enter queue:", e);
      if (fallbackToDirect) {
        console.warn("[GilmokQueue] Fail-Open: bypassing queue due to connection failure");
        const mockToken = "FAIL_OPEN_DIRECT_TOKEN";
        setAdmissionToken(mockToken);
        setIsWaiting(false);
        if (onAdmitted) {
          onAdmitted({ admissionToken: mockToken, eventId, queueKey: "FAIL_OPEN" });
        }
        return { directPass: true, admissionToken: mockToken };
      } else {
        setError("대기열 서비스에 연결할 수 없습니다.");
        return { error: e };
      }
    }
  }, [clientKey, queueUrl, onAdmitted, fallbackToDirect]);

  return {
    isWaiting,
    queueStatus,
    admissionToken,
    queueKey,
    error,
    enterQueue
  };
}
