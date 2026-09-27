import { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import { useParams, useNavigate, Link } from 'react-router-dom';
import api from '../../services/api';
import { useAuth } from '../../context/AuthContext';

export default function QueueDetails() {
  const { t } = useTranslation();
  const { queueId } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  
  const [queue, setQueue] = useState(null);
  const [loading, setLoading] = useState(true);
  const [joining, setJoining] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchQueue = async () => {
      try {
        const response = await api.get(`/api/queues/${queueId}`);
        setQueue(response.data);
      } catch (err) {
        setError(err.response?.data?.message || t('queue_not_found', 'Queue not found'));
      } finally {
        setLoading(false);
      }
    };
    fetchQueue();
  }, [queueId, t]);

  const handleJoin = async () => {
    if (!user) {
      // Must be logged in to join
      navigate('/login', { state: { returnTo: `/queue/${queueId}` } });
      return;
    }
    
    setJoining(true);
    setError(null);
    try {
      await api.post(`/api/queues/${queueId}/join`);
      navigate('/active-queue');
    } catch (err) {
      setError(err.response?.data?.message || t('join_failed', 'Failed to join the queue'));
      setJoining(false);
    }
  };

  if (loading) {
    return (
      <div className="container py-5 text-center">
        <div className="spinner-border text-primary" role="status"></div>
      </div>
    );
  }

  if (error && !queue) {
    return (
      <div className="container py-5 text-center">
        <div className="alert alert-danger max-w-md mx-auto">{error}</div>
        <Link to="/" className="btn btn-primary mt-3">{t('go_home', 'Go Home')}</Link>
      </div>
    );
  }

  const isOpen = queue?.status === 'OPEN';

  return (
    <div className="container py-5">
      <div className="row justify-content-center">
        <div className="col-12 col-md-8 col-lg-6">
          <div className="card shadow-sm border-0">
            <div className="card-body p-4 p-md-5">
              <div className="text-center mb-4">
                <span className={`badge bg-${isOpen ? 'success' : 'danger'} mb-2`}>
                  {isOpen ? t('open', 'OPEN') : t('closed', 'CLOSED')}
                </span>
                <h2 className="fw-bold text-primary mb-2">{queue.name}</h2>
                <p className="text-muted">{queue.description || t('no_description', 'No description available')}</p>
              </div>

              {error && <div className="alert alert-danger">{error}</div>}

              <div className="row g-3 mb-4 text-center">
                <div className="col-6">
                  <div className="bg-light rounded p-3 h-100">
                    <div className="text-muted small">{t('current_token', 'Current Token')}</div>
                    <div className="fw-bold fs-4">{queue.currentTokenNumber || queue.currentToken || '-'}</div>
                  </div>
                </div>
                <div className="col-6">
                  <div className="bg-light rounded p-3 h-100">
                    <div className="text-muted small">{t('people_waiting', 'People Waiting')}</div>
                    <div className="fw-bold fs-4">{queue.peopleWaiting || 0}</div>
                  </div>
                </div>
                <div className="col-6">
                  <div className="bg-light rounded p-3 h-100">
                    <div className="text-muted small">{t('est_wait', 'Est. Wait Time')}</div>
                    <div className="fw-bold fs-4">{queue.estimatedWaitMinutes || queue.estimatedWait || 0} <span className="fs-6 fw-normal">min</span></div>
                  </div>
                </div>
                <div className="col-6">
                  <div className="bg-light rounded p-3 h-100">
                    <div className="text-muted small">{t('max_capacity', 'Capacity')}</div>
                    <div className="fw-bold fs-4">{queue.maxCapacity || '∞'}</div>
                  </div>
                </div>
              </div>

              <div className="text-center">
                {isOpen ? (
                  <button 
                    onClick={handleJoin} 
                    className="btn btn-primary btn-lg w-100"
                    disabled={joining}
                  >
                    {joining ? (
                      <span className="spinner-border spinner-border-sm me-2"></span>
                    ) : null}
                    {t('join_this_queue', 'Join this Queue')}
                  </button>
                ) : (
                  <button className="btn btn-secondary btn-lg w-100" disabled>
                    {t('queue_closed_msg', 'This queue is currently closed')}
                  </button>
                )}
                
                {!user && (
                  <p className="mt-3 text-muted small">
                    {t('login_required_join', 'You will be asked to login or register first.')}
                  </p>
                )}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
