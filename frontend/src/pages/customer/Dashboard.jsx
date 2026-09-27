import { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import { useAuth } from '../../context/AuthContext';
import { Link } from 'react-router-dom';
import api from '../../services/api';

export default function CustomerDashboard() {
  const { t } = useTranslation();
  const { user } = useAuth();
  
  const [activeToken, setActiveToken] = useState(null);
  const [history, setHistory] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchDashboardData = async () => {
      setLoading(true);
      try {
        const [activeRes, historyRes] = await Promise.all([
          api.get('/api/tokens/active').catch(() => ({ data: null })),
          api.get('/api/tokens/history').catch(() => ({ data: [] }))
        ]);
        
        if (Array.isArray(activeRes.data)) {
          setActiveToken(activeRes.data.length > 0 ? activeRes.data[0] : null);
        } else {
          setActiveToken(activeRes.data);
        }
        setHistory(Array.isArray(historyRes.data) ? historyRes.data.slice(0, 5) : []);
      } catch (err) {
        setError(t('error_fetching_dashboard', 'Failed to load dashboard data.'));
      } finally {
        setLoading(false);
      }
    };
    
    fetchDashboardData();
  }, [t]);

  if (loading) {
    return (
      <div className="py-5 text-center">
        <div className="spinner-border text-primary" role="status" style={{ width: '2rem', height: '2rem' }}></div>
      </div>
    );
  }

  return (
    <div>
      {/* Welcome Header */}
      <div className="d-flex justify-content-between align-items-center mb-4">
        <div>
          <h1 style={{ fontSize: '1.5rem', fontWeight: 700, margin: 0, letterSpacing: '-0.025em' }}>
            {t('welcome_user', 'Welcome, {{name}}!', { name: user?.name || 'User' })}
          </h1>
          <p style={{ fontSize: '0.875rem', color: 'var(--color-text-muted)', margin: '0.25rem 0 0 0' }}>
            {t('dashboard_subtitle', 'Here is your active ticket overview and recent queue history')}
          </p>
        </div>
        <div>
          <Link to="/join-queue" className="btn btn-primary">
            <i className="bi bi-plus-circle me-1"></i> {t('join_queue', 'Join a Queue')}
          </Link>
        </div>
      </div>

      {error && (
        <div style={{ padding: '0.75rem 1rem', background: '#FEF2F2', border: '1px solid #FECACA', borderRadius: '6px', color: '#991B1B', fontSize: '0.875rem', marginBottom: '1.5rem' }}>
          {error}
        </div>
      )}

      {/* Active Queue Summary Section */}
      <div className="mb-4">
        {activeToken ? (
          <div className="card">
            <div className="card-header">
              <div className="d-flex align-items-center gap-2">
                <h2 className="card-title">{activeToken.queueName || activeToken.queue?.name || 'Active Queue'}</h2>
                <span className={`status-badge status-${activeToken.status?.toLowerCase()}`}>
                  {activeToken.status}
                </span>
              </div>
              <Link to="/active-queue" className="btn btn-sm btn-outline-primary">
                Inspect Ticket <i className="bi bi-arrow-right ms-1"></i>
              </Link>
            </div>
            
            <div className="card-body">
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(130px, 1fr))', gap: '1rem' }}>
                <div className="stat-card" style={{ background: 'var(--color-bg)' }}>
                  <div className="stat-card-label">{t('token_number', 'Token Number')}</div>
                  <div className="stat-card-value" style={{ color: 'var(--color-primary)' }}>{activeToken.tokenNumber}</div>
                </div>

                <div className="stat-card" style={{ background: 'var(--color-bg)' }}>
                  <div className="stat-card-label">{t('position', 'Queue Position')}</div>
                  <div className="stat-card-value">#{activeToken.queuePosition || 1}</div>
                </div>

                <div className="stat-card" style={{ background: 'var(--color-bg)' }}>
                  <div className="stat-card-label">{t('people_ahead', 'People Ahead')}</div>
                  <div className="stat-card-value">{activeToken.peopleAhead ?? Math.max(0, (activeToken.queuePosition || 1) - 1)}</div>
                </div>

                <div className="stat-card" style={{ background: 'var(--color-bg)' }}>
                  <div className="stat-card-label">{t('est_time', 'Est. Wait Time')}</div>
                  <div className="stat-card-value">
                    {activeToken.estimatedWaitMinutes ?? activeToken.estimatedWaitTime ?? 0} 
                    <span style={{ fontSize: '0.875rem', fontWeight: 400, marginLeft: '0.25rem' }}>min</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        ) : (
          <div className="card text-center p-5">
            <div style={{ fontSize: '2rem', color: 'var(--color-text-light)', marginBottom: '0.75rem' }}>
              <i className="bi bi-ticket-perforated"></i>
            </div>
            <h3 style={{ fontSize: '1.125rem', fontWeight: 600, margin: '0 0 0.375rem 0' }}>
              {t('no_active_queue', 'No Active Ticket')}
            </h3>
            <p style={{ fontSize: '0.875rem', color: 'var(--color-text-muted)', marginBottom: '1.25rem' }}>
              You are not currently waiting in any live queue.
            </p>
            <div>
              <Link to="/join-queue" className="btn btn-primary">
                <i className="bi bi-plus-circle me-1"></i> {t('join_queue', 'Join a Queue')}
              </Link>
            </div>
          </div>
        )}
      </div>

      {/* Recent History Table */}
      <div className="card">
        <div className="card-header">
          <h2 className="card-title">{t('recent_history', 'Recent Queue History')}</h2>
          <Link to="/history" style={{ fontSize: '0.8125rem', color: 'var(--color-primary)', textDecoration: 'none', fontWeight: 500 }}>
            {t('view_all', 'View All History')} &rarr;
          </Link>
        </div>
        
        <div className="table-responsive">
          <table className="table">
            <thead>
              <tr>
                <th>{t('token', 'Token')}</th>
                <th>{t('queue', 'Queue')}</th>
                <th>{t('date', 'Date')}</th>
                <th>{t('status', 'Status')}</th>
              </tr>
            </thead>
            <tbody>
              {history.length > 0 ? (
                history.map((item, idx) => (
                  <tr key={item.id || idx}>
                    <td style={{ fontWeight: 700, color: 'var(--color-primary)' }}>{item.tokenNumber}</td>
                    <td style={{ fontWeight: 500 }}>{item.queueName || item.queue?.name || '-'}</td>
                    <td style={{ fontSize: '0.8125rem', color: 'var(--color-text-muted)' }}>
                      {item.joinTime || item.date || item.createdAt ? new Date(item.joinTime || item.date || item.createdAt).toLocaleDateString() : '-'}
                    </td>
                    <td>
                      <span className={`status-badge status-${item.status?.toLowerCase()}`}>
                        {t(`status_${item.status?.toLowerCase()}`, item.status)}
                      </span>
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan="4" style={{ textAlign: 'center', padding: '2.5rem 1rem', color: 'var(--color-text-muted)' }}>
                    {t('no_history', 'No past queue history found.')}
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
