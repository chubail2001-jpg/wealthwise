import { useState, useEffect } from 'react';
import { createTransaction, updateTransaction, getGoals, depositGoal } from '../api/client';

export default function TransactionModal({ tx = null, onClose, onSaved, initialType = 'INCOME', balanceHint = null }) {
  const [form, setForm] = useState({
    type:        tx?.type        || initialType,
    amount:      tx?.amount      || '',
    description: tx?.description || '',
    category:    tx?.category    || '',
    date:        tx?.date        ? String(tx.date).slice(0, 10)
                                 : new Date().toISOString().slice(0, 10),
  });
  const [error,          setError]          = useState('');
  const [loading,        setLoading]        = useState(false);
  const [goals,          setGoals]          = useState([]);
  const [linkedGoalId,   setLinkedGoalId]   = useState('');

  // Load goals once so we can show the "link to goal" dropdown for SAVING type
  useEffect(() => {
    getGoals()
      .then(data => setGoals(data.filter(g => g.progressPercent < 100)))
      .catch(() => {});
  }, []);

  const set = (key, val) => setForm(f => ({ ...f, [key]: val }));

  const handleSave = async () => {
    setError('');
    if (!form.description)                        return setError('Description is required');
    if (!form.amount || Number(form.amount) <= 0) return setError('Please enter a valid amount');
    if (!form.date)                               return setError('Date is required');

    setLoading(true);
    try {
      const payload = { ...form, amount: Number(form.amount) };
      if (tx) {
        await updateTransaction(tx.id, payload);
      } else {
        await createTransaction(payload);
        // Auto-deposit into the linked goal when saving type is SAVING
        if (form.type === 'SAVING' && linkedGoalId) {
          await depositGoal(Number(linkedGoalId), Number(form.amount));
        }
      }
      onSaved();
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  };

  const isSaving = form.type === 'SAVING';

  return (
    <div className="modal-overlay" onClick={e => e.target === e.currentTarget && onClose()}>
      <div className="modal">
        <div className="modal-header">
          <span className="modal-title">{tx ? 'Edit Transaction' : 'New Transaction'}</span>
          <button className="modal-close" onClick={onClose}>✕</button>
        </div>

        {balanceHint !== null && (
          <div className="modal-balance-hint">
            Available to invest: <strong style={{ color: 'var(--saving)' }}>
              ${Number(balanceHint).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
            </strong>
          </div>
        )}

        {error && <div className="error-msg" style={{ margin: '0 28px' }}>{error}</div>}

        <div className="modal-body">
          <div className="field-row">
            <div className="field">
              <label>Type</label>
              <select value={form.type} onChange={e => { set('type', e.target.value); setLinkedGoalId(''); }}>
                <option value="INCOME">Income</option>
                <option value="EXPENSE">Expense</option>
                <option value="SAVING">Saving</option>
                <option value="INVESTMENT">Investment</option>
              </select>
            </div>
            <div className="field">
              <label>Amount ($)</label>
              <input type="number" placeholder="0.00" min="0.01" step="0.01"
                value={form.amount} onChange={e => set('amount', e.target.value)} />
            </div>
          </div>

          {/* Goal link — only shown for SAVING type on new transactions */}
          {isSaving && !tx && goals.length > 0 && (
            <div className="field goal-link-field">
              <label>
                Fund a goal
                <span className="field-hint"> (optional — auto-deposits into the goal)</span>
              </label>
              <select value={linkedGoalId} onChange={e => setLinkedGoalId(e.target.value)}>
                <option value="">— No goal —</option>
                {goals.map(g => (
                  <option key={g.id} value={g.id}>
                    {g.icon} {g.name}  (saved {Math.round(g.progressPercent)}% · remaining ${Number(g.remainingAmount).toLocaleString()})
                  </option>
                ))}
              </select>
              {linkedGoalId && (
                <p className="goal-link-hint">
                  This saving will also be deposited into <strong style={{ color: 'var(--saving)' }}>
                    {goals.find(g => String(g.id) === linkedGoalId)?.name}
                  </strong>.
                </p>
              )}
            </div>
          )}

          <div className="field">
            <label>Description</label>
            <input type="text" placeholder="e.g. Monthly savings transfer"
              value={form.description} onChange={e => set('description', e.target.value)} />
          </div>

          <div className="field-row">
            <div className="field">
              <label>Category</label>
              <input type="text" placeholder="e.g. Work, Food, Rent"
                value={form.category} onChange={e => set('category', e.target.value)} />
            </div>
            <div className="field">
              <label>Date</label>
              <input type="date" value={form.date} onChange={e => set('date', e.target.value)} />
            </div>
          </div>
        </div>

        <div className="modal-footer">
          <button className="btn-secondary" onClick={onClose}>Cancel</button>
          <button className="btn-primary" onClick={handleSave} disabled={loading}>
            {loading ? 'Saving…' : (isSaving && linkedGoalId ? 'Save & Fund Goal →' : 'Save Transaction →')}
          </button>
        </div>
      </div>
    </div>
  );
}
