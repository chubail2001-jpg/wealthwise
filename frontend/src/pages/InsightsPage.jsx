import { useState, useEffect } from 'react';
import { getInsights } from '../api/client';

const TYPE_META = {
  POSITIVE: { color: 'var(--income)',   bar: '#4ecb8d', label: 'Positive' },
  WARNING:  { color: 'var(--accent)',   bar: '#c8a96e', label: 'Warning'  },
  DANGER:   { color: 'var(--expense)',  bar: '#e05c6b', label: 'Alert'    },
  INFO:     { color: 'var(--saving)',   bar: '#5b9cf6', label: 'Info'     },
};

function InsightCard({ insight }) {
  const meta = TYPE_META[insight.type] || TYPE_META.INFO;
  return (
    <div className="insight-card" style={{ '--ins-color': meta.color, '--ins-bar': meta.bar }}>
      <div className="insight-card-bar" />
      <div className="insight-card-body">
        <span className="insight-badge" style={{ color: meta.color, borderColor: meta.color }}>
          {meta.label}
        </span>
        <h3 className="insight-card-title">{insight.title}</h3>
        <p className="insight-card-msg">{insight.message}</p>
        {insight.value != null && Math.abs(insight.value) > 0 && (
          <div className="insight-card-value" style={{ color: meta.color }}>
            {insight.type === 'POSITIVE' && insight.value > 0 ? '+' : ''}
            {Number.isInteger(insight.value)
              ? insight.value.toLocaleString()
              : insight.value.toFixed(1)}
            {Math.abs(insight.value) < 500 && insight.value !== Math.round(insight.value / 10) * 10
              ? '%'
              : ''}
          </div>
        )}
      </div>
    </div>
  );
}

export default function InsightsPage() {
  const [data,    setData]    = useState(null);
  const [loading, setLoading] = useState(true);
  const [error,   setError]   = useState('');

  useEffect(() => {
    getInsights()
      .then(d  => { setData(d);       setLoading(false); })
      .catch(e => { setError(e.message); setLoading(false); });
  }, []);

  if (loading) {
    return (
      <div className="page active">
        <div className="empty-state" style={{ paddingTop: '80px' }}>Analysing your finances…</div>
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

  const insights = data?.insights ?? [];
  const positives = insights.filter(i => i.type === 'POSITIVE').length;
  const warnings  = insights.filter(i => i.type === 'WARNING' || i.type === 'DANGER').length;

  return (
    <div className="page active">
      <header className="page-header">
        <div>
          <h2 className="page-title">Financial Insights</h2>
          <p className="page-date">AI-powered analysis of your spending &amp; saving habits</p>
        </div>
        <div className="insight-summary-pills">
          {positives > 0 && (
            <span className="insight-pill positive">{positives} positive</span>
          )}
          {warnings > 0 && (
            <span className="insight-pill warning">{warnings} {warnings === 1 ? 'alert' : 'alerts'}</span>
          )}
        </div>
      </header>

      {insights.length === 0 ? (
        <div className="empty-state" style={{ paddingTop: '60px' }}>
          Add more transactions to generate insights.
        </div>
      ) : (
        <div className="insights-grid">
          {insights.map((ins, i) => (
            <InsightCard key={i} insight={ins} />
          ))}
        </div>
      )}
    </div>
  );
}
