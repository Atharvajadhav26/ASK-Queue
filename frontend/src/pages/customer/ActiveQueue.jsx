import { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import { Link } from 'react-router-dom';
import api from '../../services/api';
import { useWebSocket } from '../../hooks/useWebSocket';

export default function ActiveQueue() {
  const { t } = useTranslation();
  
  const [token, setToken] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [actionLoading, setActionLoading] = useState(false);
  const [showLeaveModal, setShowLeaveModal] = useState(false);
  const [showSkipModal, setShowSkipModal] = useState(false);

  const fetchToken = async () => {
    try {
      const res = await api.get('/api/tokens/active');
      if (Array.isArray(res.data)) {
        setToken(res.data.length > 0 ? res.data[0] : null);
      } else {
        setToken(res.data);
      }
      setError(null);
    } catch (err) {
      if (err.response?.status === 404) {
        setToken(null);
      } else {
        setError(t('error_fetch_token', 'Failed to load active ticket data.'));
      }
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchToken();
  }, [t]);

  const queueIdForWs = token?.queueId || token?.queue?.queueId || null;
  useWebSocket(queueIdForWs, () => {
    fetchToken();
  });

  const handleAction = async (action, data = {}) => {
    if (!token) return;
    const tokenId = token.tokenId || token.id;
    setActionLoading(true);
    try {
      if (action === 'skip') {
        await api.post(`/api/tokens/${tokenId}/skip`, data);
        setShowSkipModal(false);
      } else if (action === 'leave') {
        await api.post(`/api/tokens/${tokenId}/leave`);
        setToken(null);
        setShowLeaveModal(false);
      } else {
        await api.post(`/api/tokens/${tokenId}/${action}`);
      }
      await fetchToken();
    } catch (err) {
      setError(err.response?.data?.message || t('action_failed', 'Action failed. Please try again.'));
    } finally {
      setActionLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="py-5 text-center">
        <div className="spinner-border text-primary" role="status" style={{ width: '2rem', height: '2rem' }}></div>
      </div>
    );
  }

  if (!token) {
    return (
      <div className="py-5">
        <div className="card text-center p-5" style={{ maxWidth: '460px', margin: '0 auto' }}>
          <div style={{ fontSize: '2.5rem', color: 'var(--color-text-light)', marginBottom: '1rem' }}>
            <i className="bi bi-ticket-perforated"></i>
          </div>
          <h2 style={{ fontSize: '1.25rem', fontWeight: 600, margin: '0 0 0.5rem 0' }}>
            {t('no_active_token', 'No Active Ticket')}
          </h2>
          <p style={{ fontSize: '0.875rem', color: 'var(--color-text-muted)', marginBottom: '1.5rem' }}>
            {t('no_active_token_desc', 'You are not currently waiting in any live queue.')}
          </p>
          <div>
            <Link to="/join-queue" className="btn btn-primary btn-lg">
              <i className="bi bi-plus-circle me-1"></i>
              {t('join_queue', 'Join a Queue')}
            </Link>
          </div>
        </div>
      </div>
    );
  }

  const isServing = token.status === 'SERVING';
  const isHeld = token.status === 'HELD';
  const isWaiting = token.status === 'WAITING';

  return (
    <div style={{ maxWidth: '600px', margin: '0 auto' }}>
      {error && (
        <div style={{ padding: '0.75rem 1rem', background: '#FEF2F2', border: '1px solid #FECACA', borderRadius: '6px', color: '#991B1B', fontSize: '0.875rem', marginBottom: '1.5rem' }}>
          {error}
        </div>
      )}

      {/* Header */}
      <div className="d-flex justify-content-between align-items-center mb-3">
        <div>
          <h1 style={{ fontSize: '1.25rem', fontWeight: 700, margin: 0 }}>
            {token.queueName || token.queue?.name || 'Active Queue'}
          </h1>
          {token.queueDescription && (
            <span style={{ fontSize: '0.8125rem', color: 'var(--color-text-muted)' }}>
              {token.queueDescription}
            </span>
          )}
        </div>
        <button className="btn btn-sm btn-secondary" onClick={fetchToken}>
          <i className="bi bi-arrow-clockwise me-1"></i> {t('refresh', 'Refresh')}
        </button>
      </div>

      {/* Ticket Card */}
      <div className="ticket-card mb-4">
        {isServing && (
          <div style={{ background: '#059669', color: '#ffffff', textAlign: 'center', padding: '0.75rem 1rem', fontWeight: 600, fontSize: '0.9375rem' }}>
            <i className="bi bi-bell-fill me-2"></i>
            {t('your_turn_now', 'IT IS YOUR TURN NOW — PLEASE PROCEED')}
          </div>
        )}
        
        {isHeld && (
          <div style={{ background: '#FFFBEB', color: '#B45309', borderBottom: '1px solid #FDE68A', textAlign: 'center', padding: '0.625rem 1rem', fontWeight: 600, fontSize: '0.875rem' }}>
            <i className="bi bi-pause-circle-fill me-2"></i>
            {t('token_on_hold', 'Your token is currently on hold')}
          </div>
        )}

        <div className="ticket-hero">
          <div style={{ fontSize: '0.75rem', fontWeight: 600, textTransform: 'uppercase', color: 'var(--color-text-muted)', letterSpacing: '0.05em', marginBottom: '0.5rem' }}>
            {t('your_token_number', 'Your Token Number')}
          </div>
          <div className="ticket-number">
            {token.tokenNumber}
          </div>
          <div className="mt-3">
            <span className={`status-badge status-${token.status?.toLowerCase()}`}>
              {t(`status_${token.status?.toLowerCase()}`, token.status)}
            </span>
          </div>
        </div>

        {/* Metrics Grid */}
        <div className="ticket-metrics">
          <div className="ticket-metric-item">
            <div className="ticket-metric-value">{token.queuePosition || 1}</div>
            <div className="ticket-metric-label">{t('position', 'Position')}</div>
          </div>
          <div className="ticket-metric-item">
            <div className="ticket-metric-value">{token.peopleAhead ?? Math.max(0, (token.queuePosition || 1) - 1)}</div>
            <div className="ticket-metric-label">{t('people_ahead', 'Ahead')}</div>
          </div>
          <div className="ticket-metric-item">
            <div className="ticket-metric-value">{token.estimatedWaitMinutes ?? token.estimatedWaitTime ?? 0} <span style={{ fontSize: '0.75rem', fontWeight: 400 }}>min</span></div>
            <div className="ticket-metric-label">{t('est_wait', 'Est. Wait')}</div>
          </div>
          <div className="ticket-metric-item" style={{ background: 'var(--color-bg)' }}>
            <div className="ticket-metric-value" style={{ color: 'var(--color-primary)' }}>{token.currentToken || '-'}</div>
            <div className="ticket-metric-label">{t('current_serving', 'Now Serving')}</div>
          </div>
        </div>

        {/* Actions Panel */}
        <div style={{ padding: '1.25rem', background: 'var(--color-surface)' }}>
          <div style={{ fontSize: '0.75rem', fontWeight: 600, textTransform: 'uppercase', color: 'var(--color-text-muted)', letterSpacing: '0.05em', marginBottom: '0.75rem' }}>
            {t('manage_ticket', 'Ticket Options')}
          </div>
          
          <div className="d-flex gap-2 flex-wrap">
            {isWaiting && (
              <>
                <button 
                  className="btn btn-secondary"
                  onClick={() => handleAction('hold')}
                  disabled={actionLoading}
                >
                  <i className="bi bi-pause-circle me-1"></i> {t('hold_turn', 'Hold Turn')}
                </button>
                <button 
                  className="btn btn-secondary"
                  onClick={() => setShowSkipModal(true)}
                  disabled={actionLoading}
                >
                  <i className="bi bi-skip-forward me-1"></i> {t('skip_turn', 'Skip Turn')}
                </button>
              </>
            )}
            
            {isHeld && (
              <button 
                className="btn btn-primary"
                onClick={() => handleAction('resume')}
                disabled={actionLoading}
              >
                <i className="bi bi-play-circle me-1"></i> {t('resume_turn', 'Resume Turn')}
              </button>
            )}

            {(isWaiting || isHeld) && (
              <button 
                className="btn btn-danger ms-auto"
                onClick={() => setShowLeaveModal(true)}
                disabled={actionLoading}
              >
                <i className="bi bi-box-arrow-right me-1"></i> {t('leave_queue', 'Leave Queue')}
              </button>
            )}
          </div>
        </div>
      </div>

      {/* Leave Confirmation Modal */}
      {showLeaveModal && (
        <div className="modal d-block modal-backdrop" tabIndex="-1">
          <div className="modal-dialog modal-dialog-centered" style={{ maxWidth: '400px' }}>
            <div className="modal-content">
              <div className="modal-header">
                <h5 className="modal-title" style={{ fontSize: '1rem', fontWeight: 600 }}>{t('confirm_leave', 'Leave Queue?')}</h5>
                <button type="button" className="btn-close" onClick={() => setShowLeaveModal(false)}></button>
              </div>
              <div className="modal-body" style={{ fontSize: '0.875rem', color: 'var(--color-text-body)' }}>
                {t('leave_warning', 'Are you sure you want to leave the queue? You will forfeit your position in line.')}
              </div>
              <div className="modal-footer">
                <button type="button" className="btn btn-secondary btn-sm" onClick={() => setShowLeaveModal(false)}>
                  {t('cancel', 'Cancel')}
                </button>
                <button type="button" className="btn btn-danger btn-sm" onClick={() => handleAction('leave')} disabled={actionLoading}>
                  {actionLoading ? <span className="spinner-border spinner-border-sm me-1"></span> : null}
                  {t('yes_leave', 'Confirm Leave')}
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Skip Positions Modal */}
      {showSkipModal && (
        <div className="modal d-block modal-backdrop" tabIndex="-1">
          <div className="modal-dialog modal-dialog-centered" style={{ maxWidth: '420px' }}>
            <div className="modal-content">
              <div className="modal-header">
                <h5 className="modal-title" style={{ fontSize: '1rem', fontWeight: 600 }}>{t('skip_positions', 'Select Positions to Skip')}</h5>
                <button type="button" className="btn-close" onClick={() => setShowSkipModal(false)}></button>
              </div>
              <div className="modal-body">
                <p style={{ fontSize: '0.875rem', color: 'var(--color-text-muted)', marginBottom: '1rem' }}>
                  {t('skip_desc', 'Choose how many positions backward you would like to move:')}
                </p>
                <div className="d-flex gap-2 justify-content-center">
                  {[1, 2, 5, 10].map(pos => (
                    <button 
                      key={pos} 
                      className="btn btn-outline-primary flex-fill"
                      onClick={() => handleAction('skip', { positions: pos })}
                      disabled={actionLoading}
                    >
                      +{pos} {t('positions_btn', 'Pos')}
                    </button>
                  ))}
                </div>
              </div>
              <div className="modal-footer">
                <button type="button" className="btn btn-secondary btn-sm" onClick={() => setShowSkipModal(false)}>
                  {t('cancel', 'Cancel')}
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
