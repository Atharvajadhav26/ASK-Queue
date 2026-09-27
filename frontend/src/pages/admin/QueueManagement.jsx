import { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import { useParams, Link } from 'react-router-dom';
import api from '../../services/api';
import { useAdminWebSocket } from '../../hooks/useWebSocket';

export default function QueueManagement() {
  const { id } = useParams();
  const { t } = useTranslation();
  
  const [queueData, setQueueData] = useState(null);
  const [tokens, setTokens] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [selectedToken, setSelectedToken] = useState(null);
  const [actionLoading, setActionLoading] = useState(false);

  // Action Modals State
  const [showSkipModal, setShowSkipModal] = useState(false);
  const [showCancelModal, setShowCancelModal] = useState(false);
  const [showPriorityModal, setShowPriorityModal] = useState(false);
  const [cancelReason, setCancelReason] = useState('');

  const fetchQueueData = async () => {
    try {
      const res = await api.get(`/api/admin/queues/${id}/tokens`);
      setQueueData(res.data);
      setTokens(res.data.tokens || []);
      
      if (selectedToken) {
        const updatedSelected = res.data.tokens?.find(tok => (tok.id || tok.tokenId) === (selectedToken.id || selectedToken.tokenId));
        setSelectedToken(updatedSelected || null);
      }
      setError(null);
    } catch (err) {
      setError(err.response?.data?.message || err.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchQueueData();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  useAdminWebSocket(id, () => {
    fetchQueueData();
  });

  const handleAction = async (actionStr, tokenId = null, payload = {}) => {
    setActionLoading(true);
    try {
      if (tokenId) {
        await api.post(`/api/admin/tokens/${tokenId}/${actionStr}`, payload);
      } else {
        await api.post(`/api/admin/queues/${id}/${actionStr}`, payload);
      }
      setShowSkipModal(false);
      setShowCancelModal(false);
      setShowPriorityModal(false);
      setCancelReason('');
      await fetchQueueData();
    } catch (err) {
      setError('Action failed: ' + (err.response?.data?.message || err.message));
    } finally {
      setActionLoading(false);
    }
  };

  if (loading) return <div className="py-5 text-center"><div className="spinner-border text-primary" role="status"></div></div>;

  const queueName = queueData?.queueName || 'Queue Management';
  const queueStatus = queueData?.status || 'OPEN';
  const isQueueOpen = queueStatus === 'OPEN';

  return (
    <div>
      {error && (
        <div style={{ padding: '0.75rem 1rem', background: '#FEF2F2', border: '1px solid #FECACA', borderRadius: '6px', color: '#991B1B', fontSize: '0.875rem', marginBottom: '1.25rem' }}>
          {error}
        </div>
      )}

      {/* Queue Header & Control Bar */}
      <div className="card mb-4">
        <div className="card-body d-flex justify-content-between align-items-center flex-wrap gap-3">
          <div>
            <div className="d-flex align-items-center gap-2 mb-1">
              <h1 style={{ fontSize: '1.375rem', fontWeight: 700, margin: 0 }}>{queueName}</h1>
              <span className={`status-badge status-${queueStatus.toLowerCase()}`}>
                {queueStatus}
              </span>
            </div>
            <span style={{ fontSize: '0.8125rem', color: 'var(--color-text-muted)' }}>
              Queue ID: {queueData?.queueId || id}
            </span>
          </div>

          <div className="d-flex gap-2 flex-wrap">
            <Link to={`/admin/queues/${id}/edit`} className="btn btn-secondary">
              <i className="bi bi-pencil me-1"></i> Edit
            </Link>
            <button className="btn btn-secondary" onClick={() => handleAction('previous')} disabled={actionLoading}>
              <i className="bi bi-arrow-left me-1"></i> {t('admin.manage.previous', 'Previous Token')}
            </button>
            <button className="btn btn-primary" onClick={() => handleAction('next')} disabled={actionLoading}>
              {t('admin.manage.next', 'Call Next Token')} <i className="bi bi-arrow-right ms-1"></i>
            </button>
            <button 
              className={`btn ${isQueueOpen ? 'btn-danger' : 'btn-primary'}`}
              onClick={() => handleAction(isQueueOpen ? 'close' : 'open')}
              disabled={actionLoading}
            >
              {isQueueOpen ? t('admin.manage.closeQueue', 'Close Queue') : t('admin.manage.openQueue', 'Open Queue')}
            </button>
          </div>
        </div>

        {/* Stats Summary Bar */}
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', borderTop: '1px solid var(--color-border)', background: 'var(--color-bg)' }}>
          <div style={{ padding: '0.75rem 1.25rem', textAlign: 'center', borderRight: '1px solid var(--color-border)' }}>
            <div style={{ fontSize: '0.6875rem', fontWeight: 600, textTransform: 'uppercase', color: 'var(--color-text-muted)', letterSpacing: '0.05em' }}>Serving Now</div>
            <div style={{ fontSize: '1.25rem', fontWeight: 700, color: 'var(--color-primary)' }}>{queueData?.currentServingToken || '-'}</div>
          </div>
          <div style={{ padding: '0.75rem 1.25rem', textAlign: 'center', borderRight: '1px solid var(--color-border)' }}>
            <div style={{ fontSize: '0.6875rem', fontWeight: 600, textTransform: 'uppercase', color: 'var(--color-text-muted)', letterSpacing: '0.05em' }}>Waiting</div>
            <div style={{ fontSize: '1.25rem', fontWeight: 700, color: 'var(--color-text-main)' }}>{queueData?.totalWaiting ?? 0}</div>
          </div>
          <div style={{ padding: '0.75rem 1.25rem', textAlign: 'center', borderRight: '1px solid var(--color-border)' }}>
            <div style={{ fontSize: '0.6875rem', fontWeight: 600, textTransform: 'uppercase', color: 'var(--color-text-muted)', letterSpacing: '0.05em' }}>Completed</div>
            <div style={{ fontSize: '1.25rem', fontWeight: 700, color: 'var(--color-text-main)' }}>{queueData?.totalServed ?? 0}</div>
          </div>
          <div style={{ padding: '0.75rem 1.25rem', textAlign: 'center' }}>
            <div style={{ fontSize: '0.6875rem', fontWeight: 600, textTransform: 'uppercase', color: 'var(--color-text-muted)', letterSpacing: '0.05em' }}>Cancelled</div>
            <div style={{ fontSize: '1.25rem', fontWeight: 700, color: 'var(--color-text-main)' }}>{queueData?.totalCancelled ?? 0}</div>
          </div>
        </div>
      </div>

      {/* Main Grid: Token Table & Token Action Panel */}
      <div className="row g-4">
        {/* Token Table */}
        <div className="col-lg-8">
          <div className="card">
            <div className="card-header">
              <h2 className="card-title">{t('admin.manage.tokenList', 'Live Token Roster')}</h2>
              <span style={{ fontSize: '0.75rem', color: 'var(--color-text-muted)' }}>
                {tokens.length} Total Tokens
              </span>
            </div>
            <div className="table-responsive">
              <table className="table">
                <thead>
                  <tr>
                    <th>Token #</th>
                    <th>Customer</th>
                    <th>Status</th>
                    <th>Priority</th>
                    <th>Join Time</th>
                  </tr>
                </thead>
                <tbody>
                  {tokens.length > 0 ? tokens.map(token => {
                    const tokenId = token.id || token.tokenId;
                    const isSelected = (selectedToken?.id || selectedToken?.tokenId) === tokenId;
                    return (
                      <tr 
                        key={tokenId} 
                        onClick={() => setSelectedToken(token)}
                        style={{ cursor: 'pointer', backgroundColor: isSelected ? 'var(--color-primary-subtle)' : undefined }}
                      >
                        <td style={{ fontWeight: 700, color: 'var(--color-primary)' }}>{token.tokenNumber}</td>
                        <td style={{ fontWeight: 500 }}>{token.userName || token.customerName || 'Customer'}</td>
                        <td>
                          <span className={`status-badge status-${token.status?.toLowerCase()}`}>
                            {token.status}
                          </span>
                        </td>
                        <td>
                          <span className={`status-badge status-${(token.priority || 'NORMAL').toLowerCase()}`}>
                            {token.priority || 'NORMAL'}
                          </span>
                        </td>
                        <td style={{ fontSize: '0.8125rem', color: 'var(--color-text-muted)' }}>
                          {token.joinTime ? new Date(token.joinTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : '-'}
                        </td>
                      </tr>
                    );
                  }) : (
                    <tr>
                      <td colSpan="5" style={{ textAlign: 'center', padding: '3rem 1rem', color: 'var(--color-text-muted)' }}>
                        <i className="bi bi-inbox fs-3 d-block mb-2"></i>
                        No tokens in this queue yet
                      </td>
                    </tr>
                  )}
                </tbody>
              </table>
            </div>
          </div>
        </div>

        {/* Selected Token Action Side Panel */}
        <div className="col-lg-4">
          <div className="card">
            <div className="card-header">
              <h2 className="card-title">{t('admin.manage.details', 'Token Inspector')}</h2>
            </div>
            <div className="card-body">
              {selectedToken ? (
                <div>
                  <div style={{ marginBottom: '1.25rem', paddingBottom: '1rem', borderBottom: '1px solid var(--color-border)' }}>
                    <div style={{ fontSize: '0.75rem', textTransform: 'uppercase', color: 'var(--color-text-muted)', fontWeight: 600, letterSpacing: '0.05em' }}>Selected Token</div>
                    <div style={{ fontSize: '1.75rem', fontWeight: 800, color: 'var(--color-primary)', marginTop: '0.125rem' }}>{selectedToken.tokenNumber}</div>
                    <div style={{ fontSize: '0.9375rem', fontWeight: 600, color: 'var(--color-text-main)' }}>{selectedToken.userName || selectedToken.customerName || 'Customer'}</div>
                  </div>

                  <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.75rem', marginBottom: '1.25rem', fontSize: '0.8125rem' }}>
                    <div>
                      <span style={{ color: 'var(--color-text-muted)' }}>Status:</span>
                      <div>
                        <span className={`status-badge status-${selectedToken.status?.toLowerCase()}`}>
                          {selectedToken.status}
                        </span>
                      </div>
                    </div>
                    <div>
                      <span style={{ color: 'var(--color-text-muted)' }}>Priority:</span>
                      <div>
                        <span className={`status-badge status-${(selectedToken.priority || 'NORMAL').toLowerCase()}`}>
                          {selectedToken.priority || 'NORMAL'}
                        </span>
                      </div>
                    </div>
                    <div>
                      <span style={{ color: 'var(--color-text-muted)' }}>Position in Line:</span>
                      <div style={{ fontWeight: 700 }}>#{selectedToken.queuePosition || 1}</div>
                    </div>
                    <div>
                      <span style={{ color: 'var(--color-text-muted)' }}>People Ahead:</span>
                      <div style={{ fontWeight: 700 }}>{selectedToken.peopleAhead ?? Math.max(0, (selectedToken.queuePosition || 1) - 1)}</div>
                    </div>
                  </div>

                  <div style={{ paddingTop: '1rem', borderTop: '1px solid var(--color-border)' }}>
                    <div style={{ fontSize: '0.75rem', textTransform: 'uppercase', color: 'var(--color-text-muted)', fontWeight: 600, letterSpacing: '0.05em', marginBottom: '0.75rem' }}>
                      Admin Actions
                    </div>

                    <div className="d-grid gap-2">
                      {selectedToken.status === 'WAITING' && (
                        <>
                          <button 
                            className="btn btn-secondary btn-sm" 
                            onClick={() => handleAction('hold', selectedToken.id || selectedToken.tokenId)}
                            disabled={actionLoading}
                          >
                            <i className="bi bi-pause-circle me-1"></i> Hold Token
                          </button>
                          <button 
                            className="btn btn-secondary btn-sm" 
                            onClick={() => setShowSkipModal(true)}
                            disabled={actionLoading}
                          >
                            <i className="bi bi-skip-forward me-1"></i> Skip Positions
                          </button>
                          <button 
                            className="btn btn-outline-primary btn-sm" 
                            onClick={() => setShowPriorityModal(true)}
                            disabled={actionLoading}
                          >
                            <i className="bi bi-star me-1"></i> Set Priority (VIP / Emergency)
                          </button>
                          <button 
                            className="btn btn-danger btn-sm" 
                            onClick={() => setShowCancelModal(true)}
                            disabled={actionLoading}
                          >
                            <i className="bi bi-x-circle me-1"></i> Cancel Token
                          </button>
                        </>
                      )}

                      {selectedToken.status === 'HELD' && (
                        <>
                          <button 
                            className="btn btn-primary btn-sm" 
                            onClick={() => handleAction('resume', selectedToken.id || selectedToken.tokenId)}
                            disabled={actionLoading}
                          >
                            <i className="bi bi-play-circle me-1"></i> Resume Token
                          </button>
                          <button 
                            className="btn btn-danger btn-sm" 
                            onClick={() => setShowCancelModal(true)}
                            disabled={actionLoading}
                          >
                            <i className="bi bi-x-circle me-1"></i> Cancel Token
                          </button>
                        </>
                      )}

                      {selectedToken.status === 'SERVING' && (
                        <>
                          <button 
                            className="btn btn-primary btn-sm" 
                            onClick={() => handleAction('complete', selectedToken.id || selectedToken.tokenId)}
                            disabled={actionLoading}
                          >
                            <i className="bi bi-check-circle me-1"></i> Complete Service
                          </button>
                          <button 
                            className="btn btn-danger btn-sm" 
                            onClick={() => setShowCancelModal(true)}
                            disabled={actionLoading}
                          >
                            <i className="bi bi-x-circle me-1"></i> Cancel Token
                          </button>
                        </>
                      )}
                    </div>
                  </div>
                </div>
              ) : (
                <div style={{ textAlign: 'center', padding: '3rem 1rem', color: 'var(--color-text-muted)' }}>
                  <i className="bi bi-cursor-fill fs-3 d-block mb-2"></i>
                  Select a token from the roster to inspect details and execute operations
                </div>
              )}
            </div>
          </div>
        </div>
      </div>

      {/* Skip Modal */}
      {showSkipModal && selectedToken && (
        <div className="modal d-block modal-backdrop" tabIndex="-1">
          <div className="modal-dialog modal-dialog-centered" style={{ maxWidth: '400px' }}>
            <div className="modal-content">
              <div className="modal-header">
                <h5 className="modal-title" style={{ fontSize: '1rem', fontWeight: 600 }}>Skip Token {selectedToken.tokenNumber}</h5>
                <button type="button" className="btn-close" onClick={() => setShowSkipModal(false)}></button>
              </div>
              <div className="modal-body">
                <p style={{ fontSize: '0.875rem', color: 'var(--color-text-muted)', marginBottom: '1rem' }}>
                  Select how many positions backward to move this token:
                </p>
                <div className="d-flex gap-2 justify-content-center">
                  {[1, 2, 5, 10].map(pos => (
                    <button 
                      key={pos} 
                      className="btn btn-outline-primary flex-fill"
                      onClick={() => handleAction('skip', selectedToken.id || selectedToken.tokenId, { positions: pos })}
                      disabled={actionLoading}
                    >
                      +{pos} Pos
                    </button>
                  ))}
                </div>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Priority Modal */}
      {showPriorityModal && selectedToken && (
        <div className="modal d-block modal-backdrop" tabIndex="-1">
          <div className="modal-dialog modal-dialog-centered" style={{ maxWidth: '400px' }}>
            <div className="modal-content">
              <div className="modal-header">
                <h5 className="modal-title" style={{ fontSize: '1rem', fontWeight: 600 }}>Set Priority for {selectedToken.tokenNumber}</h5>
                <button type="button" className="btn-close" onClick={() => setShowPriorityModal(false)}></button>
              </div>
              <div className="modal-body">
                <p style={{ fontSize: '0.875rem', color: 'var(--color-text-muted)', marginBottom: '1rem' }}>
                  Promote this token to a higher priority class:
                </p>
                <div className="d-grid gap-2">
                  <button 
                    className="btn btn-outline-primary"
                    onClick={() => handleAction('priority', selectedToken.id || selectedToken.tokenId, { priority: 'VIP', reason: 'Admin designated VIP' })}
                    disabled={actionLoading}
                  >
                    <i className="bi bi-star-fill me-1" style={{ color: '#B45309' }}></i> Set VIP Priority
                  </button>
                  <button 
                    className="btn btn-danger"
                    onClick={() => handleAction('priority', selectedToken.id || selectedToken.tokenId, { priority: 'EMERGENCY', reason: 'Emergency priority override' })}
                    disabled={actionLoading}
                  >
                    <i className="bi bi-exclamation-triangle-fill me-1"></i> Set EMERGENCY Priority
                  </button>
                  <button 
                    className="btn btn-secondary"
                    onClick={() => handleAction('priority', selectedToken.id || selectedToken.tokenId, { priority: 'NORMAL', reason: 'Reverted to Normal' })}
                    disabled={actionLoading}
                  >
                    Reset to Normal Priority
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Cancel Modal */}
      {showCancelModal && selectedToken && (
        <div className="modal d-block modal-backdrop" tabIndex="-1">
          <div className="modal-dialog modal-dialog-centered" style={{ maxWidth: '400px' }}>
            <div className="modal-content">
              <div className="modal-header">
                <h5 className="modal-title" style={{ fontSize: '1rem', fontWeight: 600 }}>Cancel Token {selectedToken.tokenNumber}</h5>
                <button type="button" className="btn-close" onClick={() => setShowCancelModal(false)}></button>
              </div>
              <div className="modal-body">
                <label className="form-label">Cancellation Reason</label>
                <input 
                  type="text" 
                  className="form-control" 
                  value={cancelReason} 
                  onChange={(e) => setCancelReason(e.target.value)}
                  placeholder="e.g. Customer no-show / Admin cancellation"
                />
              </div>
              <div className="modal-footer">
                <button type="button" className="btn btn-secondary btn-sm" onClick={() => setShowCancelModal(false)}>Cancel</button>
                <button 
                  type="button" 
                  className="btn btn-danger btn-sm"
                  onClick={() => handleAction('cancel', selectedToken.id || selectedToken.tokenId, { reason: cancelReason || 'Cancelled by admin' })}
                  disabled={actionLoading}
                >
                  Confirm Cancellation
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
