import { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import api from '../../services/api';
import { useAuth } from '../../context/AuthContext';

export default function Profile() {
  const { t, i18n } = useTranslation();
  const { user } = useAuth();
  
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    mobile: '',
    language: 'en',
    notifications: {
      email: true,
      sms: true,
      push: true
    }
  });
  
  const [passwordData, setPasswordData] = useState({
    currentPassword: '',
    newPassword: '',
    confirmPassword: ''
  });

  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState(null);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchProfile = async () => {
      try {
        const response = await api.get('/api/profile');
        const data = response.data;
        setFormData({
          name: data.name || '',
          email: data.email || '',
          mobile: data.mobile || '',
          language: data.language || 'en',
          notifications: data.notifications || { email: true, sms: true, push: true }
        });
        if (data.language) {
          i18n.changeLanguage(data.language);
        }
      } catch (err) {
        setError(t('error_fetch_profile', 'Failed to load profile'));
      } finally {
        setLoading(false);
      }
    };
    fetchProfile();
  }, [t, i18n]);

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    if (name.startsWith('notify_')) {
      const notifyType = name.replace('notify_', '');
      setFormData({
        ...formData,
        notifications: {
          ...formData.notifications,
          [notifyType]: checked
        }
      });
    } else {
      setFormData({ ...formData, [name]: value });
    }
  };

  const handlePasswordChange = (e) => {
    setPasswordData({ ...passwordData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    setMessage(null);
    setError(null);
    
    try {
      await api.put('/api/profile', formData);
      i18n.changeLanguage(formData.language);
      setMessage(t('profile_saved', 'Profile updated successfully'));
    } catch (err) {
      setError(err.response?.data?.message || t('error_save_profile', 'Failed to save profile'));
    } finally {
      setSaving(false);
    }
  };

  const handlePasswordSubmit = async (e) => {
    e.preventDefault();
    if (passwordData.newPassword !== passwordData.confirmPassword) {
      setError(t('passwords_not_match', 'New passwords do not match'));
      return;
    }
    
    setSaving(true);
    setMessage(null);
    setError(null);
    
    try {
      await api.put('/api/profile/password', {
        currentPassword: passwordData.currentPassword,
        newPassword: passwordData.newPassword
      });
      setMessage(t('password_updated', 'Password updated successfully'));
      setPasswordData({ currentPassword: '', newPassword: '', confirmPassword: '' });
    } catch (err) {
      setError(err.response?.data?.message || t('error_update_password', 'Failed to update password'));
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <div className="container py-5 text-center">
        <div className="spinner-border text-primary" role="status"></div>
      </div>
    );
  }

  return (
    <div className="container py-4">
      <h2 className="page-title fw-bold mb-4">{t('my_profile', 'My Profile')}</h2>

      {message && <div className="alert alert-success">{message}</div>}
      {error && <div className="alert alert-danger">{error}</div>}

      <div className="row g-4">
        <div className="col-12 col-lg-8">
          <div className="card shadow-sm border-0 mb-4">
            <div className="card-header bg-white border-bottom py-3">
              <h5 className="mb-0 fw-bold">{t('personal_info', 'Personal Information')}</h5>
            </div>
            <div className="card-body p-4">
              <form onSubmit={handleSubmit}>
                <div className="row g-3">
                  <div className="col-md-6">
                    <label className="form-label">{t('full_name', 'Full Name')}</label>
                    <input type="text" className="form-control" name="name" value={formData.name} onChange={handleChange} required />
                  </div>
                  <div className="col-md-6">
                    <label className="form-label">{t('email', 'Email Address')}</label>
                    <input type="email" className="form-control bg-light" name="email" value={formData.email} readOnly />
                    <small className="text-muted">{t('email_readonly', 'Email cannot be changed')}</small>
                  </div>
                  <div className="col-md-6">
                    <label className="form-label">{t('mobile', 'Mobile Number')}</label>
                    <input type="tel" className="form-control" name="mobile" value={formData.mobile} onChange={handleChange} required />
                  </div>
                  <div className="col-md-6">
                    <label className="form-label">{t('language', 'Preferred Language')}</label>
                    <select className="form-select" name="language" value={formData.language} onChange={handleChange}>
                      <option value="en">English</option>
                      <option value="hi">हिंदी (Hindi)</option>
                      <option value="mr">मराठी (Marathi)</option>
                    </select>
                  </div>
                </div>

                <hr className="my-4" />

                <h6 className="fw-bold mb-3">{t('notification_prefs', 'Notification Preferences')}</h6>
                <div className="mb-2 form-check">
                  <input type="checkbox" className="form-check-input" id="notify_email" name="notify_email" checked={formData.notifications.email} onChange={handleChange} />
                  <label className="form-check-label" htmlFor="notify_email">{t('email_notifications', 'Email Notifications')}</label>
                </div>
                <div className="mb-2 form-check">
                  <input type="checkbox" className="form-check-input" id="notify_sms" name="notify_sms" checked={formData.notifications.sms} onChange={handleChange} />
                  <label className="form-check-label" htmlFor="notify_sms">{t('sms_notifications', 'SMS Notifications')}</label>
                </div>
                <div className="mb-4 form-check">
                  <input type="checkbox" className="form-check-input" id="notify_push" name="notify_push" checked={formData.notifications.push} onChange={handleChange} />
                  <label className="form-check-label" htmlFor="notify_push">{t('push_notifications', 'Push Notifications')}</label>
                </div>

                <button type="submit" className="btn btn-primary" disabled={saving}>
                  {saving ? <span className="spinner-border spinner-border-sm me-2"></span> : null}
                  {t('save_changes', 'Save Changes')}
                </button>
              </form>
            </div>
          </div>
        </div>

        <div className="col-12 col-lg-4">
          <div className="card shadow-sm border-0">
            <div className="card-header bg-white border-bottom py-3">
              <h5 className="mb-0 fw-bold">{t('change_password', 'Change Password')}</h5>
            </div>
            <div className="card-body p-4">
              <form onSubmit={handlePasswordSubmit}>
                <div className="mb-3">
                  <label className="form-label">{t('current_password', 'Current Password')}</label>
                  <input type="password" className="form-control" name="currentPassword" value={passwordData.currentPassword} onChange={handlePasswordChange} required />
                </div>
                <div className="mb-3">
                  <label className="form-label">{t('new_password', 'New Password')}</label>
                  <input type="password" className="form-control" name="newPassword" value={passwordData.newPassword} onChange={handlePasswordChange} required minLength={6} />
                </div>
                <div className="mb-4">
                  <label className="form-label">{t('confirm_password', 'Confirm Password')}</label>
                  <input type="password" className="form-control" name="confirmPassword" value={passwordData.confirmPassword} onChange={handlePasswordChange} required minLength={6} />
                </div>
                <button type="submit" className="btn btn-outline-primary w-100" disabled={saving}>
                  {t('update_password', 'Update Password')}
                </button>
              </form>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
