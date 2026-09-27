import { Outlet, NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { useTranslation } from 'react-i18next';

export default function AdminLayout() {
  const { user, logout } = useAuth();
  const { t } = useTranslation();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <div className="admin-layout">
      {/* Sidebar */}
      <aside className="admin-sidebar">
        <div style={{ padding: '0.5rem 1.25rem 1.25rem 1.25rem' }}>
          <NavLink to="/admin/dashboard" style={{ textDecoration: 'none' }}>
            <div className="navbar-brand-text">
              <span>{t('app.name')}</span>
              <span className="navbar-brand-badge">ADMIN</span>
            </div>
          </NavLink>
        </div>

        <nav className="sidebar-nav">
          <NavLink className="nav-link" to="/admin/dashboard" end>
            <i className="bi bi-grid me-2"></i>
            {t('nav.dashboard')}
          </NavLink>
          <NavLink className="nav-link" to="/admin/create-queue">
            <i className="bi bi-plus-circle me-2"></i>
            {t('queue.createQueue')}
          </NavLink>
          <NavLink className="nav-link" to="/admin/analytics">
            <i className="bi bi-graph-up me-2"></i>
            {t('nav.analytics')}
          </NavLink>
          <NavLink className="nav-link" to="/admin/reports">
            <i className="bi bi-file-earmark-text me-2"></i>
            {t('nav.reports')}
          </NavLink>
        </nav>

        <div style={{ marginTop: 'auto', padding: '1rem 1.25rem', borderTop: '1px solid var(--color-border)' }}>
          <div style={{ fontSize: '0.8125rem', fontWeight: 600, color: 'var(--color-text-main)', marginBottom: '0.25rem', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
            {user?.name || 'Administrator'}
          </div>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', fontSize: '0.75rem' }}>
            <NavLink to="/dashboard" style={{ color: 'var(--color-primary)', textDecoration: 'none', fontWeight: 500 }}>
              Customer View
            </NavLink>
            <button onClick={handleLogout} style={{ background: 'none', border: 'none', color: '#DC2626', cursor: 'pointer', padding: 0, fontWeight: 500 }}>
              {t('nav.logout')}
            </button>
          </div>
        </div>
      </aside>

      {/* Mobile Header Bar */}
      <nav className="navbar d-lg-none" style={{ background: 'var(--color-surface)', borderBottom: '1px solid var(--color-border)', position: 'fixed', top: 0, left: 0, right: 0, zIndex: 200, padding: '0.75rem 1rem' }}>
        <div className="container-fluid p-0">
          <span className="navbar-brand-text">
            <span>{t('app.name')}</span>
            <span className="navbar-brand-badge">ADMIN</span>
          </span>
          <button className="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#adminMobileNav" style={{ border: 'none', padding: '4px 8px' }}>
            <span className="navbar-toggler-icon"></span>
          </button>
          <div className="collapse navbar-collapse mt-2" id="adminMobileNav">
            <ul className="navbar-nav">
              <li className="nav-item"><NavLink className="nav-link" to="/admin/dashboard">{t('nav.dashboard')}</NavLink></li>
              <li className="nav-item"><NavLink className="nav-link" to="/admin/create-queue">{t('queue.createQueue')}</NavLink></li>
              <li className="nav-item"><NavLink className="nav-link" to="/admin/analytics">{t('nav.analytics')}</NavLink></li>
              <li className="nav-item"><NavLink className="nav-link" to="/admin/reports">{t('nav.reports')}</NavLink></li>
              <li className="nav-item"><NavLink className="nav-link" to="/dashboard">Customer View</NavLink></li>
              <li className="nav-item"><button className="nav-link text-danger btn btn-link text-start text-decoration-none" onClick={handleLogout}>{t('nav.logout')}</button></li>
            </ul>
          </div>
        </div>
      </nav>

      {/* Content Area */}
      <div className="admin-content">
        <Outlet />
      </div>
    </div>
  );
}
