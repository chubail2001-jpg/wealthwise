import { useState, useEffect } from 'react';
import { getForecast } from '../api/client';
import ForecastChart   from '../components/ForecastChart';

const fmt = n => '$' + Number(n).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 });

export default function ForecastPage() {
  const [forecast, setForecast] = useState(null);
  const [loading,  setLoading]  = useState(true);
  const [error,    setError]    = useState('');

  useEffect(() => {
    getForecast()
      .then(data => { setForecast(data); setLoading(false); })
      .catch(e   => { setError(e.message); setLoading(false); });
  }, []);

  if (loading) {
    return (
      <div className="page active">
        <div className="empty-state" style={{ paddingTop: '80px' }}>Loading forecast…</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="page active">
        <div className="empty-state" style={{ color: 'var(--expense)', paddingTop: '80px' }}>{error}</div>
      </div>
    );
  }

  const { historical = [], projected = [], averageMonthlyNet = 0 } = forecast || {};
  const lastProjected = projected[projected.length - 1];

  return (
    <div className="page active">
      <header className="page-header">
        <div>
          <h2 className="page-title">3-Month Forecast</h2>
          <p className="page-date">Wealth projection based on your history</p>
        </div>
      </header>

      <div className="forecast-summary">
        {projected.map((p, i) => (
          <div key={i} className="forecast-card">
            <div className="f-month">{p.label}</div>
            <div className="f-label">Projected Balance</div>
            <div className="f-value" style={{ color: p.balance >= 0 ? 'var(--income)' : 'var(--expense)' }}>
              {fmt(p.balance)}
            </div>
          </div>
        ))}
      </div>

      <div className="chart-card forecast-chart-card">
        <div className="chart-title">Wealth Trajectory</div>
        <ForecastChart historical={historical} projected={projected} />
      </div>

      <div className="forecast-insights">
        <div className="insight-title">Insights</div>
        <div className="insight-body">
          {historical.length === 0 ? (
            'Add transactions to generate your forecast.'
          ) : (
            <>
              Your average monthly net cashflow is{' '}
              <strong style={{ color: averageMonthlyNet >= 0 ? 'var(--income)' : 'var(--expense)' }}>
                {fmt(averageMonthlyNet)}
              </strong>.
              {' '}Based on this {averageMonthlyNet >= 0 ? 'positive' : 'negative'} trend, your projected balance
              in 3 months is{' '}
              <strong style={{ color: lastProjected?.balance >= 0 ? 'var(--income)' : 'var(--expense)' }}>
                {fmt(lastProjected?.balance ?? 0)}
              </strong>.
              {' '}{averageMonthlyNet < 0
                ? '⚠️ Consider reducing expenses to improve your financial trajectory.'
                : '✓ Keep it up — your wealth is growing steadily.'}
            </>
          )}
        </div>
      </div>
    </div>
  );
}
