import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import api from '../../services/api';

export default function Reports() {
  const { t } = useTranslation();
  const [period, setPeriod] = useState('daily');
  const [loadingPdf, setLoadingPdf] = useState(false);
  const [loadingExcel, setLoadingExcel] = useState(false);
  const [error, setError] = useState(null);

  const handleDownload = async (type) => {
    const isPdf = type === 'pdf';
    if (isPdf) setLoadingPdf(true);
    else setLoadingExcel(true);
    setError(null);

    try {
      const response = await api.get(`/api/admin/reports/${type}`, {
        params: { period },
        responseType: 'blob'
      });
      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `smart_queue_report_${period}.${type === 'pdf' ? 'pdf' : 'xlsx'}`);
      document.body.appendChild(link);
      link.click();
      link.remove();
    } catch (err) {
      setError('Failed to download report: ' + (err.message || 'Unknown error'));
    } finally {
      if (isPdf) setLoadingPdf(false);
      else setLoadingExcel(false);
    }
  };

  return (
    <div style={{ maxWidth: '600px', margin: '0 auto' }}>
      <div className="mb-4 text-center">
        <h1 style={{ fontSize: '1.5rem', fontWeight: 700, margin: 0, letterSpacing: '-0.025em' }}>
          {t('admin.reports.title', 'Export Queue Reports')}
        </h1>
        <p style={{ fontSize: '0.875rem', color: 'var(--color-text-muted)', margin: '0.25rem 0 0 0' }}>
          Generate formatted PDF analytics or Excel data sheets
        </p>
      </div>

      {error && (
        <div style={{ padding: '0.75rem 1rem', background: '#FEF2F2', border: '1px solid #FECACA', borderRadius: '6px', color: '#991B1B', fontSize: '0.875rem', marginBottom: '1.5rem' }}>
          {error}
        </div>
      )}

      <div className="card" style={{ padding: '1.75rem' }}>
        <div style={{ fontSize: '0.8125rem', fontWeight: 600, textTransform: 'uppercase', color: 'var(--color-text-muted)', letterSpacing: '0.05em', marginBottom: '0.75rem' }}>
          {t('admin.reports.selectPeriod', 'Select Period')}
        </div>

        <div className="d-flex gap-2 mb-4">
          {['daily', 'weekly', 'monthly'].map(p => (
            <button
              key={p}
              type="button"
              className={`btn flex-fill text-capitalize ${period === p ? 'btn-primary' : 'btn-secondary'}`}
              onClick={() => setPeriod(p)}
            >
              {t(`admin.reports.${p}`, p)}
            </button>
          ))}
        </div>

        <div className="d-grid gap-3">
          <button 
            className="btn btn-outline-primary btn-lg"
            onClick={() => handleDownload('pdf')}
            disabled={loadingPdf || loadingExcel}
          >
            {loadingPdf ? (
              <span className="spinner-border spinner-border-sm me-2"></span>
            ) : (
              <i className="bi bi-file-earmark-pdf me-2"></i>
            )}
            Download PDF Report
          </button>
          
          <button 
            className="btn btn-secondary btn-lg"
            onClick={() => handleDownload('excel')}
            disabled={loadingExcel || loadingPdf}
          >
            {loadingExcel ? (
              <span className="spinner-border spinner-border-sm me-2"></span>
            ) : (
              <i className="bi bi-file-earmark-spreadsheet me-2"></i>
            )}
            Download Excel Spreadsheet
          </button>
        </div>
      </div>
    </div>
  );
}
