import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useAuth } from '../../context/AuthContext';
import { useNavigate, Link, useLocation } from 'react-router-dom';

export default function Register() {
  const { t } = useTranslation();
  const { register } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    mobile: '',
    password: ''
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    try {
      await register(formData.name, formData.email, formData.mobile, formData.password);
      const returnTo = location.state?.returnTo;
      navigate(returnTo || '/dashboard');
    } catch (err) {
      setError(err.response?.data?.message || t('registration_failed', 'Registration failed. Please try again.'));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', background: 'var(--color-bg)', padding: '1rem' }}>
      <div className="card" style={{ width: '100%', maxWidth: '420px', padding: '2rem' }}>
        <div style={{ textAlign: 'center', marginBottom: '1.5rem' }}>
          <h1 style={{ fontSize: '1.5rem', fontWeight: 700, color: 'var(--color-text-main)', margin: '0 0 0.25rem 0', letterSpacing: '-0.025em' }}>
            {t('smart_queue', 'Smart Queue')}
          </h1>
          <p style={{ fontSize: '0.875rem', color: 'var(--color-text-muted)', margin: 0 }}>
            {t('create_account', 'Create a new account')}
          </p>
        </div>
        
        {error && (
          <div style={{ padding: '0.625rem 0.875rem', background: '#FEF2F2', border: '1px solid #FECACA', borderRadius: '6px', color: '#991B1B', fontSize: '0.8125rem', marginBottom: '1.25rem' }}>
            {error}
          </div>
        )}
        
        <form onSubmit={handleSubmit}>
          <div className="mb-3">
            <label className="form-label">{t('full_name', 'Full Name')}</label>
            <input 
              type="text" 
              name="name"
              className="form-control" 
              value={formData.name}
              onChange={handleChange}
              placeholder="John Doe"
              required 
            />
          </div>
          
          <div className="mb-3">
            <label className="form-label">{t('email', 'Email Address')}</label>
            <input 
              type="email" 
              name="email"
              className="form-control" 
              value={formData.email}
              onChange={handleChange}
              placeholder="name@example.com"
              required 
            />
          </div>
          
          <div className="mb-3">
            <label className="form-label">{t('mobile', 'Mobile Number')}</label>
            <input 
              type="tel" 
              name="mobile"
              className="form-control" 
              value={formData.mobile}
              onChange={handleChange}
              placeholder="9876543210"
              required 
            />
          </div>
          
          <div className="mb-4">
            <label className="form-label">{t('password', 'Password')}</label>
            <input 
              type="password" 
              name="password"
              className="form-control" 
              value={formData.password}
              onChange={handleChange}
              placeholder="••••••••"
              required 
              minLength={6}
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
            {t('register', 'Create Account')}
          </button>
        </form>
        
        <div style={{ textAlign: 'center', fontSize: '0.8125rem', color: 'var(--color-text-muted)' }}>
          <span>{t('have_account', 'Already have an account?')} </span>
          <Link to="/login" style={{ color: 'var(--color-primary)', textDecoration: 'none', fontWeight: 500 }}>
            {t('login_here', 'Sign in')}
          </Link>
        </div>
      </div>
    </div>
  );
}
