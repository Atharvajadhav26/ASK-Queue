import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useNavigate } from 'react-router-dom';
import { QRCodeSVG } from 'qrcode.react';
import api from '../../services/api';

export default function CreateQueue() {
  const { t } = useTranslation();
  const navigate = useNavigate();

  const [formData, setFormData] = useState({
    name: '',
    serviceType: 'Clinic',
    description: '',
    prefix: '',
    maxCapacity: 50,
    avgTimePerPerson: 5,
    holdTimeMinutes: 10,
    workingHoursStart: '09:00',
    workingHoursEnd: '17:00'
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [successData, setSuccessData] = useState(null);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    try {
      const res = await api.post('/api/admin/queues/', formData);
      setSuccessData(res.data);
    } catch (err) {
      setError(err.response?.data?.message || err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleOpenQueue = async () => {
    if (!successData?.id) return;
    try {
      await api.post(`/api/admin/queues/${successData.id}/open`);
      navigate(`/admin/queues/${successData.id}/manage`);
    } catch (err) {
      setError('Error opening queue: ' + (err.response?.data?.message || err.message));
    }
  };

  const handleCopyLink = () => {
    const url = `${window.location.origin}/queue/${successData.queueId || successData.id}`;
    navigator.clipboard.writeText(url);
    alert(t('admin.createQueue.linkCopied', 'Queue link copied to clipboard!'));
  };

  if (successData) {
    const queueLink = `${window.location.origin}/queue/${successData.queueId || successData.id}`;
    return (
      <div style={{ maxWidth: '540px', margin: '0 auto' }}>
        <div className="card text-center p-5">
          <div style={{ fontSize: '2.5rem', color: 'var(--status-emerald-text)', marginBottom: '0.75rem' }}>
            <i className="bi bi-check-circle-fill"></i>
          </div>
          <h2 style={{ fontSize: '1.25rem', fontWeight: 700, margin: '0 0 0.5rem 0' }}>
            Queue Created Successfully!
          </h2>
          <p style={{ fontSize: '0.875rem', color: 'var(--color-text-muted)', marginBottom: '1.5rem' }}>
            Queue ID: <span style={{ fontWeight: 700, color: 'var(--color-primary)' }}>{successData.queueId || successData.id}</span>
          </p>

          <div style={{ background: 'var(--color-bg)', padding: '1.25rem', borderRadius: '8px', border: '1px solid var(--color-border)', marginBottom: '1.5rem' }}>
            <div className="d-flex justify-content-center mb-3">
              <QRCodeSVG value={queueLink} size={180} level="H" includeMargin={true} />
            </div>
            <div className="input-group">
              <input type="text" className="form-control form-control-sm" value={queueLink} readOnly />
              <button className="btn btn-sm btn-secondary" onClick={handleCopyLink}>
                Copy Link
              </button>
            </div>
          </div>

          <div className="d-grid gap-2">
            <button className="btn btn-primary btn-lg" onClick={handleOpenQueue}>
              <i className="bi bi-play-circle me-1"></i> Open Queue Now
            </button>
            <button className="btn btn-secondary" onClick={() => navigate(`/admin/queues/${successData.id}/manage`)}>
              Go to Management Dashboard
            </button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div style={{ maxWidth: '640px', margin: '0 auto' }}>
      <div className="mb-4">
        <h1 style={{ fontSize: '1.5rem', fontWeight: 700, margin: 0, letterSpacing: '-0.025em' }}>
          Create New Queue
        </h1>
        <p style={{ fontSize: '0.875rem', color: 'var(--color-text-muted)', margin: '0.25rem 0 0 0' }}>
          Configure queue settings, operating capacity, and average service parameters
        </p>
      </div>

      {error && (
        <div style={{ padding: '0.75rem 1rem', background: '#FEF2F2', border: '1px solid #FECACA', borderRadius: '6px', color: '#991B1B', fontSize: '0.875rem', marginBottom: '1.5rem' }}>
          {error}
        </div>
      )}

      <div className="card" style={{ padding: '1.75rem' }}>
        <form onSubmit={handleSubmit}>
          <div className="row g-3 mb-3">
            <div className="col-md-8">
              <label className="form-label">Queue Name *</label>
              <input 
                type="text" 
                name="name" 
                className="form-control" 
                value={formData.name} 
                onChange={handleChange}
                placeholder="e.g. General Consultation / Counter 1" 
                required 
              />
            </div>
            <div className="col-md-4">
              <label className="form-label">Service Type</label>
              <select name="serviceType" className="form-select" value={formData.serviceType} onChange={handleChange}>
                <option value="Clinic">Clinic</option>
                <option value="Hospital">Hospital</option>
                <option value="Bank">Bank</option>
                <option value="Service Center">Service Center</option>
                <option value="Government">Government Office</option>
                <option value="General">General</option>
              </select>
            </div>
          </div>

          <div className="mb-3">
            <label className="form-label">Description</label>
            <textarea 
              name="description" 
              className="form-control" 
              rows="2" 
              value={formData.description} 
              onChange={handleChange}
              placeholder="Brief description for customers joining line..."
            ></textarea>
          </div>

          <div className="row g-3 mb-3">
            <div className="col-md-4">
              <label className="form-label">Token Prefix</label>
              <input 
                type="text" 
                name="prefix" 
                className="form-control" 
                value={formData.prefix} 
                onChange={handleChange}
                placeholder="e.g. A, CL, B1" 
              />
            </div>
            <div className="col-md-4">
              <label className="form-label">Max Capacity</label>
              <input 
                type="number" 
                name="maxCapacity" 
                className="form-control" 
                value={formData.maxCapacity} 
                onChange={handleChange}
                min="1" 
                required 
              />
            </div>
            <div className="col-md-4">
              <label className="form-label">Avg Time (min)</label>
              <input 
                type="number" 
                name="avgTimePerPerson" 
                className="form-control" 
                value={formData.avgTimePerPerson} 
                onChange={handleChange}
                min="1" 
                required 
              />
            </div>
          </div>

          <div className="row g-3 mb-4">
            <div className="col-md-4">
              <label className="form-label">Hold Limit (min)</label>
              <input 
                type="number" 
                name="holdTimeMinutes" 
                className="form-control" 
                value={formData.holdTimeMinutes} 
                onChange={handleChange}
                min="1" 
                required 
              />
            </div>
            <div className="col-md-4">
              <label className="form-label">Start Time</label>
              <input 
                type="time" 
                name="workingHoursStart" 
                className="form-control" 
                value={formData.workingHoursStart} 
                onChange={handleChange}
              />
            </div>
            <div className="col-md-4">
              <label className="form-label">End Time</label>
              <input 
                type="time" 
                name="workingHoursEnd" 
                className="form-control" 
                value={formData.workingHoursEnd} 
                onChange={handleChange}
              />
            </div>
          </div>

          <div className="d-flex justify-content-end gap-2">
            <button type="button" className="btn btn-secondary" onClick={() => navigate('/admin/dashboard')}>
              Cancel
            </button>
            <button type="submit" className="btn btn-primary px-4" disabled={loading}>
              {loading ? <span className="spinner-border spinner-border-sm me-1"></span> : null}
              Create Queue
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
