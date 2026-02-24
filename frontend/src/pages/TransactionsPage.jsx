import { useState, useEffect } from 'react';
import { getTransactions } from '../api/client';
import TransactionModal from '../components/TransactionModal';
import DeleteModal      from '../components/DeleteModal';

const fmt     = n => '$' + Number(n).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
const fmtDate = s => new Date(s).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });

export default function TransactionsPage() {
  const [transactions, setTransactions] = useState([]);
  const [editTx,       setEditTx]       = useState(null);
  const [deleteTxId,   setDeleteTxId]   = useState(null);
  const [showAdd,      setShowAdd]      = useState(false);
  const [filterType,   setFilterType]   = useState('');
  const [filterMonth,  setFilterMonth]  = useState('');

  const load = async () => {
    try { setTransactions(await getTransactions()); }
    catch (e) { console.error(e); }
  };

  useEffect(() => { load(); }, []);

  // Client-side filtering
  let filtered = [...transactions].sort((a, b) => new Date(b.date) - new Date(a.date));
  if (filterType) filtered = filtered.filter(t => t.type === filterType);
  if (filterMonth) {
    const [y, m] = filterMonth.split('-').map(Number);
    filtered = filtered.filter(t => {
      const d = new Date(t.date);
      return d.getFullYear() === y && d.getMonth() + 1 === m;
    });
  }

  return (
    <div className="page active">
      <header className="page-header">
        <div>
          <h2 className="page-title">Transactions</h2>
          <p className="page-date">All your financial moves</p>
        </div>
        <button className="btn-add" onClick={() => setShowAdd(true)}>+ Add Transaction</button>
      </header>

      <div className="filter-bar">
        <select className="filter-select" value={filterType} onChange={e => setFilterType(e.target.value)}>
          <option value="">All Types</option>
          <option value="INCOME">Income</option>
          <option value="EXPENSE">Expense</option>
          <option value="SAVING">Saving</option>
          <option value="INVESTMENT">Investment</option>
        </select>
        <input
          type="month"
          className="filter-select"
          value={filterMonth}
          onChange={e => setFilterMonth(e.target.value)}
        />
      </div>

      <div className="tx-table-wrap">
        <table className="tx-table">
          <thead>
            <tr>
              <th>Date</th>
              <th>Description</th>
              <th>Category</th>
              <th>Type</th>
              <th>Amount</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {!filtered.length ? (
              <tr><td colSpan={6} className="empty-state">No transactions found.</td></tr>
            ) : (
              filtered.map(tx => (
                <tr key={tx.id}>
                  <td>{fmtDate(tx.date)}</td>
                  <td>{tx.description}</td>
                  <td>{tx.category || '—'}</td>
                  <td><span className={`type-badge ${tx.type}`}>{tx.type}</span></td>
                  <td className={`tx-amount ${tx.type}`}>
                    {tx.type === 'EXPENSE' ? '-' : '+'} {fmt(tx.amount)}
                  </td>
                  <td>
                    <div className="action-btns">
                      <button className="action-btn" onClick={() => setEditTx(tx)}>Edit</button>
                      <button className="action-btn del" onClick={() => setDeleteTxId(tx.id)}>Delete</button>
                    </div>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {(showAdd || editTx) && (
        <TransactionModal
          tx={editTx}
          onClose={() => { setShowAdd(false); setEditTx(null); }}
          onSaved={() => { setShowAdd(false); setEditTx(null); load(); }}
        />
      )}

      {deleteTxId && (
        <DeleteModal
          txId={deleteTxId}
          onClose={() => setDeleteTxId(null)}
          onDeleted={() => { setDeleteTxId(null); load(); }}
        />
      )}
    </div>
  );
}
