import React from 'react';

export function GilmokWaitingModal({ isWaiting, queueStatus, onCancel }) {
  if (!isWaiting) return null;

  const rank = queueStatus?.rank ?? queueStatus?.position ?? '--';
  const eta = queueStatus?.expectedWaitSeconds ?? queueStatus?.etaSeconds;
  const etaText = eta !== undefined && eta > 0 ? `약 ${eta}초` : '곧 입장';

  return (
    <div style={styles.overlay}>
      <div style={styles.modal}>
        <div style={styles.badge}>🚦 Gilmok Traffic Guard</div>
        <h3 style={styles.title}>접속 대기열 안내</h3>
        <p style={styles.subtitle}>현재 접속자가 많아 순서대로 입장 처리를 진행하고 있습니다.</p>

        <div style={styles.statusBox}>
          <div style={styles.statusItem}>
            <span style={styles.label}>내 대기 순번</span>
            <strong style={styles.valueHighlight}>{typeof rank === 'number' ? `${rank.toLocaleString()}번째` : rank}</strong>
          </div>
          <div style={styles.statusItem}>
            <span style={styles.label}>예상 대기 시간</span>
            <strong style={styles.value}>{etaText}</strong>
          </div>
          {queueStatus?.total > 0 && (
            <div style={styles.statusItem}>
              <span style={styles.label}>전체 대기 인원</span>
              <span style={styles.valueSmall}>{queueStatus.total.toLocaleString()}명</span>
            </div>
          )}
        </div>

        <div style={styles.spinnerContainer}>
          <div style={styles.spinner}></div>
        </div>

        <p style={styles.notice}>
          새로고침을 하거나 페이지를 벗어나면 대기 순번이 초기화될 수 있습니다.
        </p>

        {onCancel && (
          <button style={styles.cancelBtn} onClick={onCancel}>
            대기 취소
          </button>
        )}
      </div>
    </div>
  );
}

const styles = {
  overlay: {
    position: 'fixed',
    top: 0, left: 0, right: 0, bottom: 0,
    backgroundColor: 'rgba(15, 23, 42, 0.75)',
    backdropFilter: 'blur(4px)',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    zIndex: 10000,
  },
  modal: {
    backgroundColor: '#ffffff',
    borderRadius: '16px',
    padding: '36px 32px',
    width: '420px',
    maxWidth: '90vw',
    textAlign: 'center',
    boxShadow: '0 25px 50px -12px rgba(0, 0, 0, 0.25)',
    fontFamily: '-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif',
  },
  badge: {
    display: 'inline-block',
    padding: '4px 12px',
    backgroundColor: '#EEF2FF',
    color: '#4F46E5',
    borderRadius: '9999px',
    fontSize: '12px',
    fontWeight: '600',
    marginBottom: '12px',
  },
  title: {
    margin: '0 0 8px 0',
    fontSize: '22px',
    fontWeight: '700',
    color: '#1E293B',
  },
  subtitle: {
    margin: '0 0 20px 0',
    fontSize: '14px',
    color: '#64748B',
    lineHeight: '1.5',
  },
  statusBox: {
    backgroundColor: '#F8FAFC',
    borderRadius: '12px',
    padding: '16px 20px',
    marginBottom: '20px',
    border: '1px solid #E2E8F0',
  },
  statusItem: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    padding: '6px 0',
  },
  label: {
    fontSize: '14px',
    color: '#475569',
  },
  valueHighlight: {
    fontSize: '18px',
    fontWeight: '700',
    color: '#4F46E5',
  },
  value: {
    fontSize: '16px',
    fontWeight: '600',
    color: '#1E293B',
  },
  valueSmall: {
    fontSize: '13px',
    color: '#64748B',
  },
  spinnerContainer: {
    display: 'flex',
    justifyContent: 'center',
    marginBottom: '16px',
  },
  spinner: {
    width: '28px',
    height: '28px',
    border: '3px solid #E2E8F0',
    borderTop: '3px solid #4F46E5',
    borderRadius: '50%',
    animation: 'spin 1s linear infinite',
  },
  notice: {
    margin: '0',
    fontSize: '12px',
    color: '#94A3B8',
    lineHeight: '1.4',
  },
  cancelBtn: {
    marginTop: '16px',
    padding: '8px 16px',
    border: '1px solid #CBD5E1',
    backgroundColor: '#ffffff',
    color: '#64748B',
    borderRadius: '8px',
    fontSize: '13px',
    cursor: 'pointer',
  }
};
