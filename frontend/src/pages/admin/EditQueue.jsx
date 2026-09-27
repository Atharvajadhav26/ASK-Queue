import { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import { useParams, useNavigate } from 'react-router-dom';
import api from '../../services/api';

export default function EditQueue() {
  const { t } = useTranslation();
  const { id } = useParams();
  const navigate = useNavigate();

  const [formData, setFormData] = useState({
    name: '',
    serviceType: '',
    description: '',
    maxCapacity: 50,
    avgTimePerPerson: 5,
    holdTimeMinutes: 10,
    workingHoursStart: '09:00',
    workingHoursEnd: '17:00'
  });
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(false);
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [deleteLoading, setDeleteLoading] = useState(false);

  useEffect(() => {
    const fetchQueue = async () => {
      try {
        const res = await api.get(`/api/admin/queues/${id}/tokens`);
        const q = res.data;
        // Also fetch detail for settings
        setFormData(prev => ({
          ...prev,
          name: q.queueName || '',
          serviceType: q.serviceType || prev.serviceType,
        }));
        // Fetch full queue detail for settings
        const detailRes = await api.get(`/api/queues/${q.queueId}`);
        const detail = detailRes.data;
        setFormData({
          name: detail.name || '',
          serviceType: detail.serviceType || '',
          description: detail.description || '',
          maxCapacity: detail.maxCapacity || 50,
          avgTimePerPerson: detail.avgTimePerPerson || 5,
          holdTimeMinutes: detail.holdTimeMinutes || 10,
          workingHoursStart: detail.workingHoursStart || '09:00',
          workingHoursEnd: detail.workingHoursEnd || '17:00'
        });
      } catch (err) {
        setError('Failed to load queue details.');
      } finally {
        setLoading(false);
      }
    };
    fetchQueue();
  }, [id]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    setError(null);
    setSuccess(false);
    try {
      await api.put(`/api/admin/queues/${id}`, formData);
      setSuccess(true);
      setTimeout(() => navigate('/admin/dashboard'), 1500);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to update queue.');
    } finally {
      setSaving(false);
    }
  };

  const handleDeleteQueue = async () => {
    setDeleteLoading(true);
    setError(null);
    try {
      await api.delete(`/api/admin/queues/${id}`);
      navigate('/admin/dashboard');
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to delete queue.');
      setShowDeleteModal(false);
    } finally {
      setDeleteLoading(false);
    }
  };

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
            {t('admin.editQueue.title', 'Edit Queue')}
          </h1>
          <p style={{ fontSize: '0.875rem', color: 'var(--color-text-muted)', margin: '0.25rem 0 0 0' }}>
            Update queue settings and configuration
          </p>
        </div>
        <button 
          type="button" 
          className="btn btn-outline-danger btn-sm"
          onClick={() => setShowDeleteModal(true)}
        >
          <i className="bi bi-trash me-1"></i> Delete Queue
        </button>
      </div>

      {error && (
        <div style={{ padding: '0.75rem 1rem', background: '#FEF2F2', border: '1px solid #FECACA', borderRadius: '6px', color: '#991B1B', fontSize: '0.875rem', marginBottom: '1.5rem' }}>
          {error}
        </div>
      )}

      {success && (
        <div style={{ padding: '0.75rem 1rem', background: '#F0FDF4', border: '1px solid #BBF7D0', borderRadius: '6px', color: '#166534', fontSize: '0.875rem', marginBottom: '1.5rem' }}>
          Queue updated successfully. Redirecting...
        </div>
      )}

      <div className="card">
        <div className="card-body">
          <form onSubmit={handleSubmit}>
            <div className="row g-3 mb-3">
              <div className="col-md-6">
                <label className="form-label">Queue Name</label>
                <input type="text" name="name" className="form-control" value={formData.name} onChange={handleChange} required />
              </div>
              <div className="col-md-6">
                <label className="form-label">Service Type</label>
                <select name="serviceType" className="form-select" value={formData.serviceType} onChange={handleChange} required>
                  <option value="Clinic">Clinic</option>
                  <option value="Bank">Bank</option>
                  <option value="Government">Government</option>
                  <option value="Restaurant">Restaurant</option>
                  <option value="Retail">Retail</option>
                  <option value="Other">Other</option>
                </select>
              </div>
            </div>

            <div className="mb-3">
              <label className="form-label">Description</label>
              <textarea name="description" className="form-control" rows="2" value={formData.description} onChange={handleChange} />
            </div>

            <div className="row g-3 mb-3">
              <div className="col-md-4">
                <label className="form-label">Max Capacity</label>
                <input type="number" name="maxCapacity" className="form-control" value={formData.maxCapacity} onChange={handleChange} min="1" required />
              </div>
              <div className="col-md-4">
                <label className="form-label">Avg Time (min)</label>
                <input type="number" name="avgTimePerPerson" className="form-control" value={formData.avgTimePerPerson} onChange={handleChange} min="1" required />
              </div>
              <div className="col-md-4">
                <label className="form-label">Hold Limit (min)</label>
                <input type="number" name="holdTimeMinutes" className="form-control" value={formData.holdTimeMinutes} onChange={handleChange} min="1" required />
              </div>
            </div>

            <div className="row g-3 mb-4">
              <div className="col-md-6">
                <label className="form-label">Start Time</label>
                <input type="time" name="workingHoursStart" className="form-control" value={formData.workingHoursStart} onChange={handleChange} />
              </div>
              <div className="col-md-6">
                <label className="form-label">End Time</label>
                <input type="time" name="workingHoursEnd" className="form-control" value={formData.workingHoursEnd} onChange={handleChange} />
              </div>
            </div>

            <div className="d-flex justify-content-between align-items-center">
              <div className="d-flex gap-2">
                <button type="submit" className="btn btn-primary" disabled={saving}>
                  {saving ? (
                    <span className="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                  ) : null}
                  Save Changes
                </button>
                <button type="button" className="btn btn-secondary" onClick={() => navigate('/admin/dashboard')}>
                  Cancel
                </button>
              </div>
              <button 
                type="button" 
                className="btn btn-outline-danger"
                onClick={() => setShowDeleteModal(true)}
              >
                <i className="bi bi-trash me-1"></i> Delete
              </button>
            </div>
          </form>
        </div>
      </div>

      {/* Delete Confirmation Modal */}
      {showDeleteModal && (
        <div className="modal d-block modal-backdrop" tabIndex="-1">
          <div className="modal-dialog modal-dialog-centered" style={{ maxWidth: '440px' }}>
            <div className="modal-content">
              <div className="modal-header">
                <h5 className="modal-title" style={{ fontSize: '1rem', fontWeight: 600 }}>Delete Queue</h5>
                <button type="button" className="btn-close" onClick={() => setShowDeleteModal(false)}></button>
              </div>
              <div className="modal-body">
                <p style={{ margin: 0, fontSize: '0.875rem', color: 'var(--color-text-body)' }}>
                  Are you sure you want to delete <strong>{formData.name || 'this queue'}</strong>?
                </p>
                <p style={{ marginTop: '0.5rem', marginBottom: 0, fontSize: '0.8125rem', color: '#B91C1C' }}>
                  This will permanently remove the queue, its configuration, all active tokens, history, and associated notifications. This action cannot be undone.
                </p>
              </div>
              <div className="modal-footer">
                <button 
                  type="button" 
                  className="btn btn-secondary btn-sm" 
                  onClick={() => setShowDeleteModal(false)} 
                  disabled={deleteLoading}
                >
                  Cancel
                </button>
                <button 
                  type="button" 
                  className="btn btn-danger btn-sm"
                  onClick={handleDeleteQueue}
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
