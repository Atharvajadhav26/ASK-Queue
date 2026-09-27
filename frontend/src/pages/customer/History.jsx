import { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import api from '../../services/api';

export default function History() {
  const { t } = useTranslation();
  const [history, setHistory] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchHistory = async () => {
      try {
        const response = await api.get('/api/tokens/history');
        setHistory(Array.isArray(response.data) ? response.data : []);
        setError(null);
      } catch (err) {
        setError(t('error_history', 'Failed to load queue history.'));
      } finally {
        setLoading(false);
      }
    };
    fetchHistory();
  }, [t]);

  if (loading) {
    return (
      <div className="py-5 text-center">
        <div className="spinner-border text-primary" role="status"></div>
      </div>
    );
  }

  return (
    <div>
      <div className="mb-4">
        <h1 style={{ fontSize: '1.5rem', fontWeight: 700, margin: 0, letterSpacing: '-0.025em' }}>
          {t('queue_history', 'Queue History')}
        </h1>
        <p style={{ fontSize: '0.875rem', color: 'var(--color-text-muted)', margin: '0.25rem 0 0 0' }}>
          Completed and cancelled ticket history
        </p>
      </div>

      {error && (
        <div style={{ padding: '0.75rem 1rem', background: '#FEF2F2', border: '1px solid #FECACA', borderRadius: '6px', color: '#991B1B', fontSize: '0.875rem', marginBottom: '1.5rem' }}>
          {error}
        </div>
      )}

      <div className="card">
        <div className="table-responsive">
          <table className="table">
            <thead>
              <tr>
                <th>{t('token', 'Token')}</th>
                <th>{t('queue', 'Queue Name')}</th>
                <th>{t('date', 'Date & Time')}</th>
                <th>{t('status', 'Status')}</th>
              </tr>
            </thead>
            <tbody>
              {history.length > 0 ? (
                history.map((item, idx) => {
                  const dateVal = item.joinTime || item.date || item.createdAt;
                  return (
                    <tr key={item.id || idx}>
                      <td style={{ fontWeight: 700, color: 'var(--color-primary)' }}>{item.tokenNumber}</td>
                      <td style={{ fontWeight: 500 }}>{item.queueName || item.queue?.name || 'Service Queue'}</td>
                      <td style={{ fontSize: '0.8125rem', color: 'var(--color-text-muted)' }}>
                        {dateVal ? new Date(dateVal).toLocaleString() : '-'}
                      </td>
                      <td>
                        <span className={`status-badge status-${item.status?.toLowerCase()}`}>
                          {t(`status_${item.status?.toLowerCase()}`, item.status)}
                        </span>
                      </td>
                    </tr>
                  );
                })
              ) : (
                <tr>
                  <td colSpan="4" style={{ textAlign: 'center', padding: '3rem 1rem', color: 'var(--color-text-muted)' }}>
                    <i className="bi bi-clock-history fs-3 d-block mb-2"></i>
                    {t('no_history_yet', 'You have no queue history yet.')}
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
