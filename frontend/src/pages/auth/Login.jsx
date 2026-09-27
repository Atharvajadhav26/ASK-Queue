import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useAuth } from '../../context/AuthContext';
import { useNavigate, Link, useLocation } from 'react-router-dom';

export default function Login() {
  const { t } = useTranslation();
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    try {
      const user = await login(email, password);
      // If user came from a queue page (QR scan), return there
      const returnTo = location.state?.returnTo;
      if (returnTo) {
        navigate(returnTo);
      } else if (user.role === 'ADMIN') {
        navigate('/admin/dashboard');
      } else {
        navigate('/dashboard');
      }
    } catch (err) {
      setError(err.response?.data?.message || t('login_failed', 'Login failed. Please check your credentials.'));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', background: 'var(--color-bg)', padding: '1rem' }}>
      <div className="card" style={{ width: '100%', maxWidth: '400px', padding: '2rem' }}>
        <div style={{ textAlign: 'center', marginBottom: '1.5rem' }}>
          <h1 style={{ fontSize: '1.5rem', fontWeight: 700, color: 'var(--color-text-main)', margin: '0 0 0.25rem 0', letterSpacing: '-0.025em' }}>
            {t('smart_queue', 'Smart Queue')}
          </h1>
          <p style={{ fontSize: '0.875rem', color: 'var(--color-text-muted)', margin: 0 }}>
            {t('login_to_account', 'Sign in to access your queues')}
          </p>
        </div>
        
        {error && (
          <div style={{ padding: '0.625rem 0.875rem', background: '#FEF2F2', border: '1px solid #FECACA', borderRadius: '6px', color: '#991B1B', fontSize: '0.8125rem', marginBottom: '1.25rem' }}>
            {error}
          </div>
        )}
        
        <form onSubmit={handleSubmit}>
          <div className="mb-3">
            <label className="form-label">{t('email', 'Email Address')}</label>
            <input 
              type="email" 
              className="form-control" 
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="admin@smartqueue.com"
              required 
            />
          </div>
          
          <div className="mb-4">
            <label className="form-label">{t('password', 'Password')}</label>
            <input 
              type="password" 
              className="form-control" 
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="••••••••"
              required 
            />
          </div>
          
          <button 
            type="submit" 
            className="btn btn-primary w-100 mb-3 btn-lg" 
            disabled={loading}
          >
            {loading ? (
              <span className="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
            ) : null}
            {t('login', 'Sign In')}
          </button>
        </form>
        
        <div style={{ textAlign: 'center', fontSize: '0.8125rem', color: 'var(--color-text-muted)' }}>
          <span>{t('no_account', 'Don\'t have an account?')} </span>
          <Link to="/register" state={{ returnTo: location.state?.returnTo }} style={{ color: 'var(--color-primary)', textDecoration: 'none', fontWeight: 500 }}>
            {t('register_here', 'Create an account')}
          </Link>
        </div>
      </div>
    </div>
  );
}
