import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useNavigate } from 'react-router-dom';
import { Scanner } from '@yudiel/react-qr-scanner';

export default function JoinQueue() {
  const { t } = useTranslation();
  const navigate = useNavigate();
  
  const [queueId, setQueueId] = useState('');
  const [activeTab, setActiveTab] = useState('code'); // 'code' or 'scan'
  const [error, setError] = useState('');

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!queueId.trim()) {
      setError(t('queue_id_required', 'Queue ID is required'));
      return;
    }
    navigate(`/queue/${queueId.trim()}`);
  };

  const handleScan = (result) => {
    if (result && result.length > 0) {
      const scannedText = result[0].rawValue;
      // Extract queue ID if it's a full URL, or use directly if it's just ID
      try {
        const url = new URL(scannedText);
        const pathParts = url.pathname.split('/');
        const id = pathParts[pathParts.length - 1];
        if (id) {
          navigate(`/queue/${id}`);
        } else {
          setError(t('invalid_qr', 'Invalid QR Code format'));
        }
      } catch (err) {
        // If not a URL, assume it's directly the queue ID
        navigate(`/queue/${scannedText}`);
      }
    }
  };

  return (
    <div className="container py-5">
      <div className="row justify-content-center">
        <div className="col-12 col-md-8 col-lg-6">
          <div className="text-center mb-4">
            <h2 className="page-title fw-bold">{t('join_queue', 'Join a Queue')}</h2>
            <p className="page-subtitle text-muted">{t('join_queue_subtitle', 'Enter queue code or scan QR code')}</p>
          </div>

          <div className="card shadow-sm border-0">
            <div className="card-header bg-white p-0 border-bottom">
              <ul className="nav nav-tabs nav-fill w-100 border-0">
                <li className="nav-item">
                  <button 
                    className={`nav-link border-0 py-3 ${activeTab === 'code' ? 'active fw-bold text-primary border-bottom border-primary border-3' : 'text-muted'}`}
                    onClick={() => setActiveTab('code')}
                  >
                    <i className="bi bi-keyboard me-2"></i>
                    {t('enter_code', 'Enter Code')}
                  </button>
                </li>
                <li className="nav-item">
                  <button 
                    className={`nav-link border-0 py-3 ${activeTab === 'scan' ? 'active fw-bold text-primary border-bottom border-primary border-3' : 'text-muted'}`}
                    onClick={() => setActiveTab('scan')}
                  >
                    <i className="bi bi-qr-code-scan me-2"></i>
                    {t('scan_qr', 'Scan QR')}
                  </button>
                </li>
              </ul>
            </div>
            
            <div className="card-body p-4 p-md-5">
              {error && <div className="alert alert-danger">{error}</div>}
              
              {activeTab === 'code' ? (
                <form onSubmit={handleSubmit}>
                  <div className="mb-4">
                    <label className="form-label">{t('queue_id', 'Queue ID / Code')}</label>
                    <input 
                      type="text" 
                      className="form-control form-control-lg text-center fw-bold letter-spacing-1" 
                      value={queueId}
                      onChange={(e) => setQueueId(e.target.value)}
                      placeholder="e.g. Q-12345"
                    />
                  </div>
                  <button type="submit" className="btn btn-primary btn-lg w-100">
                    {t('continue', 'Continue')}
                  </button>
                </form>
              ) : (
                <div className="text-center">
                  <div className="qr-scanner-container bg-light rounded overflow-hidden mx-auto mb-3" style={{ maxWidth: '300px', aspectRatio: '1/1' }}>
                    <Scanner 
                      onScan={handleScan}
                      onError={(err) => setError(t('camera_error', 'Camera access failed'))}
                    />
                  </div>
                  <p className="text-muted small">
                    {t('point_camera', 'Point your camera at the queue QR code')}
                  </p>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
