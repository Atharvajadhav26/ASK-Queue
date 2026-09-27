import { Outlet, NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { useTranslation } from 'react-i18next';

export default function CustomerLayout() {
  const { user, logout } = useAuth();
  const { t } = useTranslation();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <div className="app-layout">
      <nav className="navbar navbar-expand-lg" style={{ background: 'var(--color-surface)', borderBottom: '1px solid var(--color-border)', padding: '0.625rem 0' }}>
        <div className="container">
          <NavLink className="navbar-brand navbar-brand-text me-4" to="/dashboard">
            <span>{t('app.name')}</span>
          </NavLink>

          <button className="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#customerNav" style={{ border: 'none' }}>
            <span className="navbar-toggler-icon"></span>
          </button>

          <div className="collapse navbar-collapse" id="customerNav">
            <ul className="navbar-nav me-auto mb-2 mb-lg-0">
              <li className="nav-item">
                <NavLink className="nav-link" to="/dashboard">{t('nav.dashboard')}</NavLink>
              </li>
              <li className="nav-item">
                <NavLink className="nav-link" to="/join-queue">{t('nav.joinQueue')}</NavLink>
              </li>
              <li className="nav-item">
                <NavLink className="nav-link" to="/active-queue">{t('nav.activeQueue')}</NavLink>
              </li>
              <li className="nav-item">
                <NavLink className="nav-link" to="/notifications">{t('nav.notifications')}</NavLink>
              </li>
              <li className="nav-item">
                <NavLink className="nav-link" to="/history">{t('nav.history')}</NavLink>
              </li>
              <li className="nav-item">
                <NavLink className="nav-link" to="/profile">{t('nav.profile')}</NavLink>
              </li>
            </ul>

            <div className="d-flex align-items-center gap-3">
              {user?.role === 'ADMIN' && (
                <NavLink className="btn btn-sm btn-outline-primary" to="/admin/dashboard">
                  <i className="bi bi-speedometer2 me-1"></i> Admin Panel
                </NavLink>
              )}
              <span style={{ fontSize: '0.8125rem', fontWeight: 500, color: 'var(--color-text-main)' }}>
                {user?.name}
              </span>
              <button 
                className="btn btn-sm btn-secondary" 
                onClick={handleLogout}
              >
                {t('nav.logout')}
              </button>
            </div>
          </div>
        </div>
      </nav>

      <main className="app-main">
        <div className="container">
          <Outlet />
        </div>
      </main>
    </div>
  );
}
