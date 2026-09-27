import { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import { Link } from 'react-router-dom';
import api from '../../services/api';

export default function Dashboard() {
  const { t } = useTranslation();
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [queueToDelete, setQueueToDelete] = useState(null);
  const [deleteLoading, setDeleteLoading] = useState(false);

  useEffect(() => {
    fetchDashboard();
  }, []);

  const fetchDashboard = async () => {
    try {
      setLoading(true);
      const res = await api.get('/api/admin/analytics/dashboard');
      setData(res.data);
      setError(null);
    } catch (err) {
      setError(err.response?.data?.message || err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleConfirmDelete = async () => {
    if (!queueToDelete) return;
    setDeleteLoading(true);
    try {
      await api.delete(`/api/admin/queues/${queueToDelete.id}`);
      setQueueToDelete(null);
      await fetchDashboard();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to delete queue.');
      setQueueToDelete(null);
    } finally {
      setDeleteLoading(false);
    }
  };

  if (loading) return <div className="py-5 text-center"><div className="spinner-border text-primary" role="status"></div></div>;

  return (
    <div>
      {/* Header */}
      <div className="d-flex justify-content-between align-items-center mb-4">
        <div>
          <h1 style={{ fontSize: '1.5rem', fontWeight: 700, margin: 0, letterSpacing: '-0.025em' }}>
            {t('admin.dashboard.title', 'Admin Overview')}
          </h1>
          <p style={{ fontSize: '0.875rem', color: 'var(--color-text-muted)', margin: '0.25rem 0 0 0' }}>
            Live status metrics and queue operations control
          </p>
        </div>
        <Link to="/admin/create-queue" className="btn btn-primary">
          <i className="bi bi-plus-circle me-1"></i> {t('admin.dashboard.createQueue', 'Create Queue')}
        </Link>
      </div>

      {error && (
        <div style={{ padding: '0.75rem 1rem', background: '#FEF2F2', border: '1px solid #FECACA', borderRadius: '6px', color: '#991B1B', fontSize: '0.875rem', marginBottom: '1.5rem' }}>
          {error}
        </div>
      )}

      {/* Metrics Row */}
      <div className="row g-3 mb-4">
        <div className="col-sm-6 col-lg-3">
          <div className="stat-card">
            <div className="stat-card-label">{t('admin.dashboard.activeQueues', 'Active Queues')}</div>
            <div className="stat-card-value">{data?.activeQueues ?? 0}</div>
          </div>
        </div>
        <div className="col-sm-6 col-lg-3">
          <div className="stat-card">
            <div className="stat-card-label">{t('admin.dashboard.customersWaiting', 'Total Waiting')}</div>
            <div className="stat-card-value">{data?.customersWaiting ?? 0}</div>
          </div>
        </div>
        <div className="col-sm-6 col-lg-3">
          <div className="stat-card">
            <div className="stat-card-label">{t('admin.dashboard.currentToken', 'Serving Now')}</div>
            <div className="stat-card-value" style={{ color: 'var(--color-primary)' }}>{data?.currentToken || '-'}</div>
          </div>
        </div>
        <div className="col-sm-6 col-lg-3">
          <div className="stat-card">
            <div className="stat-card-label">{t('admin.dashboard.avgWaitTime', 'Avg Wait Time')}</div>
            <div className="stat-card-value">{data?.avgWaitTime || '0 min'}</div>
          </div>
        </div>
      </div>

      {/* Queue List Panel */}
      <div className="card">
        <div className="card-header">
          <h2 className="card-title">{t('admin.dashboard.recentQueues', 'Managed Queues')}</h2>
        </div>
        
        <div className="table-responsive">
          <table className="table">
            <thead>
              <tr>
                <th>Queue Name</th>
                <th>Queue ID</th>
                <th>Service Type</th>
                <th>Status</th>
                <th>Waiting</th>
                <th style={{ textAlign: 'right' }}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {data?.recentQueues && data.recentQueues.length > 0 ? (
                data.recentQueues.map(queue => (
                  <tr key={queue.id}>
                    <td style={{ fontWeight: 600, color: 'var(--color-text-main)' }}>{queue.name}</td>
                    <td style={{ fontSize: '0.8125rem', color: 'var(--color-text-muted)' }}>{queue.queueId || queue.id}</td>
                    <td>{queue.serviceType || 'General'}</td>
                    <td>
                      <span className={`status-badge status-${queue.status?.toLowerCase()}`}>
                        {queue.status}
                      </span>
                    </td>
                    <td style={{ fontWeight: 600 }}>{queue.waitingCount ?? 0}</td>
                    <td style={{ textAlign: 'right' }}>
                      <Link to={`/admin/queues/${queue.id}/edit`} className="btn btn-sm btn-outline-secondary me-2">
                        <i className="bi bi-pencil"></i> Edit
                      </Link>
                      <Link to={`/admin/queues/${queue.id}/manage`} className="btn btn-sm btn-outline-primary me-2">
                        Manage Queue &rarr;
                      </Link>
                      <button 
                        type="button" 
                        className="btn btn-sm btn-outline-danger" 
                        onClick={() => setQueueToDelete(queue)}
                        title="Delete Queue"
                      >
                        <i className="bi bi-trash"></i>
                      </button>
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan="6" style={{ textAlign: 'center', padding: '2.5rem 1rem', color: 'var(--color-text-muted)' }}>
                    No queues created yet. Click "Create Queue" to start.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Delete Confirmation Modal */}
      {queueToDelete && (
        <div className="modal d-block modal-backdrop" tabIndex="-1">
          <div className="modal-dialog modal-dialog-centered" style={{ maxWidth: '440px' }}>
            <div className="modal-content">
              <div className="modal-header">
                <h5 className="modal-title" style={{ fontSize: '1rem', fontWeight: 600 }}>Delete Queue</h5>
                <button type="button" className="btn-close" onClick={() => setQueueToDelete(null)}></button>
              </div>
              <div className="modal-body">
                <p style={{ margin: 0, fontSize: '0.875rem', color: 'var(--color-text-body)' }}>
                  Are you sure you want to delete <strong>{queueToDelete.name}</strong> ({queueToDelete.queueId})?
                </p>
                <p style={{ marginTop: '0.5rem', marginBottom: 0, fontSize: '0.8125rem', color: '#B91C1C' }}>
                  This will permanently remove the queue, its configuration, all active tokens, history, and associated notifications. This action cannot be undone.
                </p>
              </div>
              <div className="modal-footer">
                <button 
                  type="button" 
                  className="btn btn-secondary btn-sm" 
                  onClick={() => setQueueToDelete(null)} 
                  disabled={deleteLoading}
                >
                  Cancel
                </button>
                <button 
                  type="button" 
                  className="btn btn-danger btn-sm"
                  onClick={handleConfirmDelete}
                  disabled={deleteLoading}
                >
                  {deleteLoading ? <span className="spinner-border spinner-border-sm me-1" role="status" aria-hidden="true"></span> : <i className="bi bi-trash me-1"></i>}
                  Delete Permanently
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
