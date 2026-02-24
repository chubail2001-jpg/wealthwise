const fmt     = n  => '$' + Number(n).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
const fmtDate = s  => new Date(s).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
const ICON    = { INCOME: '↑', EXPENSE: '↓', SAVING: '◎', INVESTMENT: '◈' };

export default function RecentTransactions({ transactions }) {
  const sorted = [...transactions]
    .sort((a, b) => new Date(b.date) - new Date(a.date))
    .slice(0, 8);

  if (!sorted.length) {
    return <div className="empty-state">No transactions yet. Add your first one!</div>;
  }

  return (
    <div className="tx-list">
      {sorted.map(tx => (
        <div key={tx.id} className="tx-item">
          <div className={`tx-dot ${tx.type}`}>{ICON[tx.type]}</div>
          <div className="tx-info">
            <div className="tx-desc">{tx.description}</div>
            <div className="tx-cat">{tx.category || '—'}</div>
          </div>
          <div className="tx-date-col">{fmtDate(tx.date)}</div>
          <div className={`tx-amount ${tx.type}`}>
            {tx.type === 'EXPENSE' ? '-' : '+'} {fmt(tx.amount)}
          </div>
        </div>
      ))}
    </div>
  );
}
