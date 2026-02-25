import { useState, useEffect } from 'react';
import { getReportSummary, downloadReport } from '../api/client';

function getLastMonths(n) {
  const months = [];
  const now = new Date();
  for (let i = 0; i < n; i++) {
    const d   = new Date(now.getFullYear(), now.getMonth() - i, 1);
    const val = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`;
    const label = d.toLocaleDateString('en-US', { month: 'long', year: 'numeric' });
    months.push({ val, label });
  }
  return months;
}

function fmt(n) {
  return '$' + n.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

export default function ReportsPage() {
  const months = getLastMonths(12);
  const [month,       setMonth]       = useState(months[0].val);
  const [summary,     setSummary]     = useState(null);
  const [loading,     setLoading]     = useState(true);
  const [downloading, setDownloading] = useState(''); // 'pdf' | 'csv' | ''
  const [error,       setError]       = useState('');

  useEffect(() => { loadSummary(); }, [month]);

  async function loadSummary() {
    setLoading(true);
    setError('');
    try {
      const data = await getReportSummary(month);
      setSummary(data);
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  }

  async function handleDownload(type) {
    setDownloading(type);
    setError('');
    try {
      await downloadReport(type, month);
    } catch (e) {
      setError('Download failed: ' + e.message);
    } finally {
      setDownloading('');
    }
  }

  const rateColor = summary
    ? summary.savingsRate >= 50 ? 'var(--income)'
    : summary.savingsRate >= 20 ? 'var(--accent)'
    : 'var(--expense)'
    : 'var(--muted)';

  const selectedLabel = months.find(m => m.val === month)?.label ?? month;

  return (
    <div className="page active">
      <header className="page-header">
        <div>
          <h2 className="page-title">Export & Reports</h2>
          <p className="page-date">{selectedLabel} financial summary</p>
        </div>
        <div className="report-dl-group">
          <button
            className="report-dl-btn pdf-btn"
            onClick={() => handleDownload('pdf')}
            disabled={!!downloading || loading}
          >
            {downloading === 'pdf' ? 'Generating…' : '⬇ PDF Report'}
          </button>
          <button
            className="report-dl-btn csv-btn"
            onClick={() => handleDownload('csv')}
            disabled={!!downloading || loading}
          >
            {downloading === 'csv' ? 'Exporting…' : '⬇ CSV Export'}
          </button>
        </div>
      </header>

      {error && <p className="form-error" style={{ marginBottom: 16 }}>{error}</p>}

      {/* Month selector strip */}
      <div className="report-months">
        {months.map(m => (
          <button
            key={m.val}
            className={`report-month-btn${m.val === month ? ' active' : ''}`}
            onClick={() => setMonth(m.val)}
          >
            {m.label}
          </button>
        ))}
      </div>

      {loading ? (
        <div className="empty-state" style={{ paddingTop: 60 }}>Loading report…</div>
      ) : summary && (
        <>
          {/* Summary stat grid */}
          <div className="report-stat-grid">
            <div className="report-stat-card">
              <span className="report-stat-label">Total Income</span>
              <span className="report-stat-value" style={{ color: 'var(--income)' }}>
                {fmt(summary.totalIncome)}
              </span>
            </div>
            <div className="report-stat-card">
              <span className="report-stat-label">Total Expenses</span>
              <span className="report-stat-value" style={{ color: 'var(--expense)' }}>
                {fmt(summary.totalExpenses)}
              </span>
            </div>
            <div className="report-stat-card">
              <span className="report-stat-label">Net Savings</span>
              <span className="report-stat-value"
                style={{ color: summary.netSavings >= 0 ? 'var(--income)' : 'var(--expense)' }}>
                {summary.netSavings >= 0 ? '+' : ''}{fmt(summary.netSavings)}
              </span>
            </div>
            <div className="report-stat-card">
              <span className="report-stat-label">Savings Rate</span>
              <span className="report-stat-value" style={{ color: rateColor }}>
                {summary.savingsRate.toFixed(1)}%
              </span>
            </div>
            <div className="report-stat-card">
              <span className="report-stat-label">Savings & Transfers</span>
              <span className="report-stat-value" style={{ color: 'var(--saving)' }}>
                {fmt(summary.totalSavings)}
              </span>
            </div>
            <div className="report-stat-card">
              <span className="report-stat-label">Investments</span>
              <span className="report-stat-value" style={{ color: 'var(--invest)' }}>
                {fmt(summary.totalInvestments)}
              </span>
            </div>
          </div>

          {/* Category breakdown */}
          {summary.topCategories.length > 0 && (
            <div className="report-section">
              <h3 className="report-section-title">Top Spending Categories</h3>
              <div className="report-cat-list">
                {summary.topCategories.map((c, i) => (
                  <div key={i} className="report-cat-row">
                    <span className="report-cat-name">{c.category}</span>
                    <div className="report-cat-track">
                      <div className="report-cat-fill" style={{ width: `${Math.min(c.pct, 100)}%` }} />
                    </div>
                    <span className="report-cat-pct">{c.pct.toFixed(1)}%</span>
                    <span className="report-cat-amt">{fmt(c.amount)}</span>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* Transactions count hint */}
          <div className="report-section">
            <h3 className="report-section-title">
              {summary.transactionCount} Transaction{summary.transactionCount !== 1 ? 's' : ''} this month
            </h3>
            <p style={{ fontSize: '0.82rem', color: 'var(--muted)', marginTop: 6, lineHeight: 1.6 }}>
              The PDF report includes a full transaction table with dates, categories,
              descriptions and amounts. The CSV export is compatible with Excel, Google Sheets,
              and any spreadsheet tool.
            </p>
          </div>
        </>
      )}
    </div>
  );
}
