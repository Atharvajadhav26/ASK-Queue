import { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import api from '../../services/api';
import { Chart as ChartJS, CategoryScale, LinearScale, BarElement, PointElement, LineElement, ArcElement, Title, Tooltip, Legend } from 'chart.js';
import { Bar, Doughnut } from 'react-chartjs-2';

ChartJS.register(CategoryScale, LinearScale, BarElement, PointElement, LineElement, ArcElement, Title, Tooltip, Legend);

export default function Analytics() {
  const { t } = useTranslation();
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchAnalytics();
  }, []);

  const fetchAnalytics = async () => {
    try {
      const res = await api.get('/api/admin/analytics/charts');
      setData(res.data);
      setError(null);
    } catch (err) {
      setError(err.response?.data?.message || err.message);
    } finally {
      setLoading(false);
    }
  };

  if (loading) return <div className="py-5 text-center"><div className="spinner-border text-primary" role="status"></div></div>;

  const barOptions = {
    responsive: true,
    plugins: { legend: { position: 'top' }, title: { display: false } },
  };

  return (
    <div>
      <div className="mb-4">
        <h1 style={{ fontSize: '1.5rem', fontWeight: 700, margin: 0, letterSpacing: '-0.025em' }}>
          {t('admin.analytics.title', 'Analytics & Metrics')}
        </h1>
        <p style={{ fontSize: '0.875rem', color: 'var(--color-text-muted)', margin: '0.25rem 0 0 0' }}>
          Live throughput, wait-time distributions, and queue statistics
        </p>
      </div>

      {error && (
        <div style={{ padding: '0.75rem 1rem', background: '#FEF2F2', border: '1px solid #FECACA', borderRadius: '6px', color: '#991B1B', fontSize: '0.875rem', marginBottom: '1.5rem' }}>
          {error}
        </div>
      )}

      {/* Metric Cards Grid */}
      <div className="row g-3 mb-4">
        <div className="col-6 col-md-3">
          <div className="stat-card">
            <div className="stat-card-label">{t('admin.analytics.activeQueues', 'Active Queues')}</div>
            <div className="stat-card-value">{data?.activeQueues || 0}</div>
          </div>
        </div>
        <div className="col-6 col-md-3">
          <div className="stat-card">
            <div className="stat-card-label">{t('admin.analytics.customersServed', 'Served Today')}</div>
            <div className="stat-card-value">{data?.customersServed || 0}</div>
          </div>
        </div>
        <div className="col-6 col-md-3">
          <div className="stat-card">
            <div className="stat-card-label">{t('admin.analytics.avgWaitTime', 'Avg Wait Time')}</div>
            <div className="stat-card-value">{data?.avgWaitTime || '0 min'}</div>
          </div>
        </div>
        <div className="col-6 col-md-3">
          <div className="stat-card">
            <div className="stat-card-label">{t('admin.analytics.peakHour', 'Peak Traffic Hour')}</div>
            <div className="stat-card-value" style={{ color: 'var(--color-primary)' }}>{data?.peakHour || '-'}</div>
          </div>
        </div>
      </div>

      {/* Chart Widgets */}
      <div className="row g-4">
        <div className="col-lg-7">
          <div className="card h-100">
            <div className="card-header">
              <h2 className="card-title">{t('admin.analytics.customersServedPerHour', 'Hourly Customer Traffic')}</h2>
            </div>
            <div className="card-body">
              {data?.customersServedPerHour ? (
                <Bar options={barOptions} data={data.customersServedPerHour} />
              ) : (
                <div style={{ textAlign: 'center', padding: '3rem 1rem', color: 'var(--color-text-muted)' }}>
                  No traffic chart data available
                </div>
              )}
            </div>
          </div>
        </div>

        <div className="col-lg-5">
          <div className="card h-100">
            <div className="card-header">
              <h2 className="card-title">{t('admin.analytics.tokenStatusDist', 'Token Status Breakdown')}</h2>
            </div>
            <div className="card-body d-flex align-items-center justify-content-center">
              {data?.tokenStatusDistribution ? (
                <div style={{ maxWidth: '280px', width: '100%' }}>
                  <Doughnut data={data.tokenStatusDistribution} />
                </div>
              ) : (
                <div style={{ textAlign: 'center', padding: '3rem 1rem', color: 'var(--color-text-muted)' }}>
                  No status distribution data available
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
