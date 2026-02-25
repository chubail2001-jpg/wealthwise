import { useState, useEffect } from 'react';

const ICONS = ['🎯','🚗','🏠','✈️','💍','🎓','🏖️','💻','🏋️','🛡️','💰','📈'];

export default function GoalModal({ goal, onSave, onClose }) {
  const editing = !!goal;

  const [form, setForm] = useState({
    name:                goal?.name                || '',
    targetAmount:        goal?.targetAmount        || '',
    savedAmount:         goal?.savedAmount         || '',
    monthlyContribution: goal?.monthlyContribution || '',
    deadline:            goal?.deadline            || '',
    icon:                goal?.icon                || '🎯',
  });
  const [error, setError] = useState('');
  const [saving, setSaving] = useState(false);

  const set = (field, val) => setForm(f => ({ ...f, [field]: val }));

  async function handleSubmit(e) {
    e.preventDefault();
    if (!form.name.trim())           return setError('Goal name is required');
    if (!form.targetAmount)          return setError('Target amount is required');
    if (!form.monthlyContribution)   return setError('Monthly contribution is required');

    setSaving(true);
    setError('');
    try {
      const payload = {
        name:                form.name.trim(),
        targetAmount:        parseFloat(form.targetAmount),
        savedAmount:         parseFloat(form.savedAmount)  || 0,
        monthlyContribution: parseFloat(form.monthlyContribution),
        deadline:            form.deadline || null,
        icon:                form.icon,
      };
      await onSave(payload);
    } catch (err) {
      setError(err.message);
      setSaving(false);
    }
  }

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-box" onClick={e => e.stopPropagation()}>
        <div className="modal-header">
          <h3 className="modal-title">{editing ? 'Edit Goal' : 'New Goal'}</h3>
          <button className="modal-close" onClick={onClose}>✕</button>
        </div>

        {/* Icon picker */}
        <div className="goal-icon-picker">
          {ICONS.map(ic => (
            <button
              key={ic}
              type="button"
              className={`goal-icon-btn${form.icon === ic ? ' selected' : ''}`}
              onClick={() => set('icon', ic)}
            >{ic}</button>
          ))}
        </div>

        <form onSubmit={handleSubmit} className="modal-form">
          <div className="form-row">
            <label className="form-label">Goal Name</label>
            <input
              className="form-input"
              placeholder="e.g. Emergency Fund"
              value={form.name}
              onChange={e => set('name', e.target.value)}
            />
          </div>

          <div className="form-grid-2">
            <div className="form-row">
              <label className="form-label">Target Amount ($)</label>
              <input
                className="form-input"
                type="number" min="1" step="0.01"
                placeholder="10000"
                value={form.targetAmount}
                onChange={e => set('targetAmount', e.target.value)}
              />
            </div>
            <div className="form-row">
              <label className="form-label">Already Saved ($)</label>
              <input
                className="form-input"
                type="number" min="0" step="0.01"
                placeholder="0"
                value={form.savedAmount}
                onChange={e => set('savedAmount', e.target.value)}
              />
            </div>
          </div>

          <div className="form-grid-2">
            <div className="form-row">
              <label className="form-label">Monthly Contribution ($)</label>
              <input
                className="form-input"
                type="number" min="1" step="0.01"
                placeholder="500"
                value={form.monthlyContribution}
                onChange={e => set('monthlyContribution', e.target.value)}
              />
            </div>
            <div className="form-row">
              <label className="form-label">Target Deadline (optional)</label>
              <input
                className="form-input"
                type="date"
                value={form.deadline}
                onChange={e => set('deadline', e.target.value)}
              />
            </div>
          </div>

          {error && <p className="form-error">{error}</p>}

          <div className="modal-actions">
            <button type="button" className="btn-cancel" onClick={onClose}>Cancel</button>
            <button type="submit" className="btn-add" disabled={saving}>
              {saving ? 'Saving…' : editing ? 'Save Changes' : 'Create Goal'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
