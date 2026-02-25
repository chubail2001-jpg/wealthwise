import { useState, useEffect } from 'react';
import { getGoals, createGoal, updateGoal, depositGoal, deleteGoal, getDashboardSummary } from '../api/client';
import GoalModal from '../components/GoalModal';

const fmt  = n  => '$' + Number(n).toLocaleString('en-US', { minimumFractionDigits: 0, maximumFractionDigits: 0 });
const fmtD = d  => d ? new Date(d + 'T00:00:00').toLocaleDateString('en-US', { month: 'short', year: 'numeric' }) : '—';

function GoalCard({ goal, onEdit, onDeposit, onDelete }) {
  const pct  = Math.min(goal.progressPercent, 100);
  const done = pct >= 100;

  const barColor = done
    ? 'var(--income)'
    : pct >= 60
    ? 'var(--saving)'
    : pct >= 30
    ? 'var(--accent)'
    : 'var(--expense)';

  return (
    <div className={`goal-card${done ? ' goal-done' : ''}`}>
      <div className="goal-card-top">
        <span className="goal-icon">{goal.icon || '🎯'}</span>
        <div className="goal-meta">
          <div className="goal-name">{goal.name}</div>
          {done
            ? <span className="goal-status-badge done">Completed ✓</span>
            : goal.estimatedCompletionDate
            ? <span className="goal-status-badge">Est. {fmtD(goal.estimatedCompletionDate)}</span>
            : null}
        </div>
        <div className="goal-actions">
          {!done && (
            <button className="goal-btn deposit-btn" title="Add deposit" onClick={() => onDeposit(goal)}>+$</button>
          )}
          <button className="goal-btn edit-btn" title="Edit" onClick={() => onEdit(goal)}>✎</button>
          <button className="goal-btn del-btn"  title="Delete" onClick={() => onDelete(goal)}>✕</button>
        </div>
      </div>

      {/* Progress bar */}
      <div className="goal-progress-track">
        <div
          className="goal-progress-fill"
          style={{ width: `${pct}%`, background: barColor }}
        />
      </div>

      <div className="goal-stats">
        <div className="goal-stat">
          <span className="goal-stat-label">Saved</span>
          <span className="goal-stat-value" style={{ color: 'var(--income)' }}>{fmt(goal.savedAmount)}</span>
        </div>
        <div className="goal-stat center">
          <span className="goal-pct" style={{ color: barColor }}>{pct.toFixed(0)}%</span>
        </div>
        <div className="goal-stat right">
          <span className="goal-stat-label">Target</span>
          <span className="goal-stat-value">{fmt(goal.targetAmount)}</span>
        </div>
      </div>

      <div className="goal-footer">
        <span>Monthly: <strong style={{ color: 'var(--saving)' }}>{fmt(goal.monthlyContribution)}</strong></span>
        {goal.remainingAmount > 0 && (
          <span>Remaining: <strong style={{ color: 'var(--accent)' }}>{fmt(goal.remainingAmount)}</strong></span>
        )}
        {goal.monthsToCompletion > 0 && (
          <span>{goal.monthsToCompletion} month{goal.monthsToCompletion !== 1 ? 's' : ''} to go</span>
        )}
        {goal.deadline && (
          <span>Deadline: <strong>{fmtD(goal.deadline)}</strong></span>
        )}
      </div>
    </div>
  );
}

function DepositModal({ goal, availableSavings, onClose, onConfirm }) {
  const [amount, setAmount] = useState('');
  const [error,  setError]  = useState('');
  const [busy,   setBusy]   = useState(false);

  async function submit(e) {
    e.preventDefault();
    const val = parseFloat(amount);
    if (!val || val <= 0) return setError('Enter a positive amount');
    setBusy(true);
    try { await onConfirm(val); }
    catch (err) { setError(err.message); setBusy(false); }
  }

  const remaining = Number(goal.targetAmount) - Number(goal.savedAmount);

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-box deposit-box" onClick={e => e.stopPropagation()}>
        <div className="modal-header">
          <h3 className="modal-title">{goal.icon} Fund Goal — {goal.name}</h3>
          <button className="modal-close" onClick={onClose}>✕</button>
        </div>
        <form onSubmit={submit} className="modal-form">

          {/* Available savings banner */}
          {availableSavings > 0 && (
            <div className="deposit-savings-banner">
              <div>
                <span className="deposit-savings-label">Net savings available this month</span>
                <span className="deposit-savings-amount">
                  ${availableSavings.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                </span>
              </div>
              <div className="deposit-quick-btns">
                <button type="button" className="deposit-quick-btn"
                  onClick={() => setAmount((availableSavings * 0.25).toFixed(2))}>25%</button>
                <button type="button" className="deposit-quick-btn"
                  onClick={() => setAmount((availableSavings * 0.5).toFixed(2))}>50%</button>
                <button type="button" className="deposit-quick-btn"
                  onClick={() => setAmount(Math.min(availableSavings, remaining).toFixed(2))}>All →</button>
              </div>
            </div>
          )}

          <div className="form-row">
            <label className="form-label">Amount to deposit ($)</label>
            <input
              className="form-input"
              type="number" min="0.01" step="0.01" autoFocus
              placeholder="e.g. 500"
              value={amount}
              onChange={e => setAmount(e.target.value)}
            />
          </div>
          <p className="deposit-hint">
            Saved: <strong style={{ color: 'var(--income)' }}>${Number(goal.savedAmount).toLocaleString()}</strong>
            {' '}/ Target: <strong>${Number(goal.targetAmount).toLocaleString()}</strong>
            {' '}· Remaining: <strong style={{ color: 'var(--accent)' }}>${remaining.toLocaleString()}</strong>
          </p>

          {error && <p className="form-error">{error}</p>}
          <div className="modal-actions">
            <button type="button" className="btn-cancel" onClick={onClose}>Cancel</button>
            <button type="submit" className="btn-add" disabled={busy}>{busy ? 'Funding…' : 'Fund Goal'}</button>
          </div>
        </form>
      </div>
    </div>
  );
}

export default function GoalsPage() {
  const [goals,           setGoals]           = useState([]);
  const [loading,         setLoading]         = useState(true);
  const [error,           setError]           = useState('');
  const [showCreate,      setShowCreate]      = useState(false);
  const [editGoal,        setEditGoal]        = useState(null);
  const [depositGoalData, setDepositGoalData] = useState(null);
  const [deleteTarget,    setDeleteTarget]    = useState(null);
  const [availableSavings, setAvailableSavings] = useState(0);

  useEffect(() => { load(); }, []);

  async function load() {
    try {
      const [data, summary] = await Promise.all([getGoals(), getDashboardSummary()]);
      setGoals(data);
      const net = (summary.monthIncome ?? 0) - (summary.monthExpense ?? 0) - (summary.monthInvestment ?? 0);
      setAvailableSavings(Math.max(0, net));
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  }

  async function handleCreate(payload) {
    const created = await createGoal(payload);
    setGoals(prev => [created, ...prev]);
    setShowCreate(false);
  }

  async function handleEdit(payload) {
    const updated = await updateGoal(editGoal.id, payload);
    setGoals(prev => prev.map(g => g.id === updated.id ? updated : g));
    setEditGoal(null);
  }

  async function handleDeposit(amount) {
    const updated = await depositGoal(depositGoalData.id, amount);
    setGoals(prev => prev.map(g => g.id === updated.id ? updated : g));
    setDepositGoalData(null);
  }

  async function handleDelete() {
    await deleteGoal(deleteTarget.id);
    setGoals(prev => prev.filter(g => g.id !== deleteTarget.id));
    setDeleteTarget(null);
  }

  const totalTarget    = goals.reduce((s, g) => s + Number(g.targetAmount),  0);
  const totalSaved     = goals.reduce((s, g) => s + Number(g.savedAmount),   0);
  const overallPct     = totalTarget > 0 ? (totalSaved / totalTarget) * 100 : 0;
  const completedCount = goals.filter(g => g.progressPercent >= 100).length;

  if (loading) return (
    <div className="page active">
      <div className="empty-state" style={{ paddingTop: '80px' }}>Loading goals…</div>
    </div>
  );

  return (
    <div className="page active">
      <header className="page-header">
        <div>
          <h2 className="page-title">Financial Goals</h2>
          <p className="page-date">Track your savings targets and estimated completion</p>
        </div>
        <button className="btn-add" onClick={() => setShowCreate(true)}>+ New Goal</button>
      </header>

      {error && <p className="form-error" style={{ marginBottom: 20 }}>{error}</p>}

      {/* Summary bar */}
      {goals.length > 0 && (
        <div className="goals-summary">
          <div className="gs-item">
            <span className="gs-label">Total Goals</span>
            <span className="gs-value">{goals.length}</span>
          </div>
          <div className="gs-item">
            <span className="gs-label">Completed</span>
            <span className="gs-value" style={{ color: 'var(--income)' }}>{completedCount}</span>
          </div>
          <div className="gs-item">
            <span className="gs-label">Total Saved</span>
            <span className="gs-value" style={{ color: 'var(--saving)' }}>${Number(totalSaved).toLocaleString()}</span>
          </div>
          <div className="gs-item">
            <span className="gs-label">Total Target</span>
            <span className="gs-value">${Number(totalTarget).toLocaleString()}</span>
          </div>
          <div className="gs-item wide">
            <span className="gs-label">Overall Progress</span>
            <div className="gs-bar-track">
              <div className="gs-bar-fill" style={{ width: `${Math.min(overallPct, 100)}%` }} />
            </div>
            <span className="gs-pct">{overallPct.toFixed(0)}%</span>
          </div>
        </div>
      )}

      {goals.length === 0 ? (
        <div className="empty-state" style={{ paddingTop: '60px' }}>
          No goals yet. Create your first financial goal!
        </div>
      ) : (
        <div className="goals-grid">
          {goals.map(g => (
            <GoalCard
              key={g.id}
              goal={g}
              onEdit={setEditGoal}
              onDeposit={setDepositGoalData}
              onDelete={setDeleteTarget}
            />
          ))}
        </div>
      )}

      {/* Modals */}
      {showCreate && (
        <GoalModal onSave={handleCreate} onClose={() => setShowCreate(false)} />
      )}
      {editGoal && (
        <GoalModal goal={editGoal} onSave={handleEdit} onClose={() => setEditGoal(null)} />
      )}
      {depositGoalData && (
        <DepositModal
          goal={depositGoalData}
          availableSavings={availableSavings}
          onClose={() => setDepositGoalData(null)}
          onConfirm={handleDeposit}
        />
      )}
      {deleteTarget && (
        <div className="modal-overlay" onClick={() => setDeleteTarget(null)}>
          <div className="modal-box delete-box" onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <h3 className="modal-title">Delete Goal</h3>
              <button className="modal-close" onClick={() => setDeleteTarget(null)}>✕</button>
            </div>
            <p style={{ color: 'var(--muted)', marginBottom: 24, fontSize: '0.9rem' }}>
              Delete <strong style={{ color: 'var(--text)' }}>{deleteTarget.icon} {deleteTarget.name}</strong>? This cannot be undone.
            </p>
            <div className="modal-actions">
              <button className="btn-cancel" onClick={() => setDeleteTarget(null)}>Cancel</button>
              <button className="btn-delete" onClick={handleDelete}>Delete</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
