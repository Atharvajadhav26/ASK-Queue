import { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import api from '../../services/api';

export default function Notifications() {
  const { t } = useTranslation();
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchNotifications = async () => {
    try {
      const response = await api.get('/api/notifications/');
      setNotifications(Array.isArray(response.data) ? response.data : []);
      setError(null);
    } catch (err) {
      setError(t('error_notifications', 'Failed to load notifications.'));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchNotifications();
  }, [t]);

  const handleMarkAsRead = async (id) => {
    try {
      await api.put(`/api/notifications/${id}/read`);
      setNotifications(notifications.map(n => n.id === id ? { ...n, read: true, isRead: true } : n));
    } catch (err) {
      console.error(err);
    }
  };

  const handleMarkAllAsRead = async () => {
    try {
      await api.put('/api/notifications/read-all');
      setNotifications(notifications.map(n => ({ ...n, read: true, isRead: true })));
    } catch (err) {
      console.error(err);
    }
  };

  const unreadCount = notifications.filter(n => !n.read && !n.isRead).length;

  if (loading) {
    return (
      <div className="py-5 text-center">
        <div className="spinner-border text-primary" role="status"></div>
      </div>
    );
  }

  return (
    <div style={{ maxWidth: '680px', margin: '0 auto' }}>
      <div className="d-flex justify-content-between align-items-center mb-4">
        <div>
          <h1 style={{ fontSize: '1.5rem', fontWeight: 700, margin: 0, letterSpacing: '-0.025em' }}>
            {t('notifications', 'Notifications')}
            {unreadCount > 0 && (
              <span className="badge bg-danger ms-2" style={{ borderRadius: '12px', fontSize: '0.75rem' }}>
                {unreadCount}
              </span>
            )}
          </h1>
          <p style={{ fontSize: '0.875rem', color: 'var(--color-text-muted)', margin: '0.25rem 0 0 0' }}>
            Live status updates and queue turn alerts
          </p>
        </div>
        {unreadCount > 0 && (
          <button onClick={handleMarkAllAsRead} className="btn btn-sm btn-outline-primary">
            {t('mark_all_read', 'Mark all as read')}
          </button>
        )}
      </div>

      {error && (
        <div style={{ padding: '0.75rem 1rem', background: '#FEF2F2', border: '1px solid #FECACA', borderRadius: '6px', color: '#991B1B', fontSize: '0.875rem', marginBottom: '1.5rem' }}>
          {error}
        </div>
      )}

      {notifications.length > 0 ? (
        <div className="card" style={{ padding: 0 }}>
          {notifications.map((n, idx) => {
            const isUnread = !n.read && !n.isRead;
            return (
              <div 
                key={n.id || idx} 
                style={{ 
                  padding: '1rem 1.25rem', 
                  borderBottom: idx < notifications.length - 1 ? '1px solid var(--color-border)' : 'none',
                  background: isUnread ? 'var(--color-primary-subtle)' : 'var(--color-surface)',
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'flex-start',
                  gap: '1rem'
                }}
              >
                <div>
                  <div style={{ fontSize: '0.9375rem', fontWeight: isUnread ? 700 : 600, color: 'var(--color-text-main)', marginBottom: '0.25rem' }}>
                    {n.title}
                  </div>
                  <div style={{ fontSize: '0.875rem', color: 'var(--color-text-body)', marginBottom: '0.375rem' }}>
                    {n.message}
                  </div>
                  <div style={{ fontSize: '0.75rem', color: 'var(--color-text-muted)' }}>
                    {n.createdAt ? new Date(n.createdAt).toLocaleString() : ''}
                  </div>
                </div>

                {isUnread && (
                  <button 
                    onClick={() => handleMarkAsRead(n.id)}
                    className="btn btn-sm btn-secondary"
                    style={{ fontSize: '0.75rem', whiteSpace: 'nowrap' }}
                  >
                    Mark Read
                  </button>
                )}
              </div>
            );
          })}
        </div>
      ) : (
        <div className="card text-center p-5">
          <i className="bi bi-bell-slash fs-2 text-muted mb-2 d-block"></i>
          <h3 style={{ fontSize: '1rem', fontWeight: 600, margin: '0 0 0.25rem 0' }}>No Notifications</h3>
          <p style={{ fontSize: '0.875rem', color: 'var(--color-text-muted)', margin: 0 }}>
            {t('no_notifications', 'You have no queue notifications yet.')}
          </p>
        </div>
      )}
    </div>
  );
}
