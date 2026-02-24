import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { getTransactions } from '../api/client';
import StatCard            from '../components/StatCard';
import BreakdownChart      from '../components/BreakdownChart';
import DonutChart          from '../components/DonutChart';
import RecentTransactions  from '../components/RecentTransactions';
import TransactionModal    from '../components/TransactionModal';

const fmt = n => '$' + Number(n).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 });

function sumType(txs, type, monthStart, monthEnd) {
  return txs
    .filter(t => {
      const d = new Date(t.date);
      return t.type === type && d >= monthStart && d <= monthEnd;
    })
    .reduce((a, b) => a + Number(b.amount), 0);
}

export default function DashboardPage() {
  const navigate = useNavigate();
  const [transactions, setTransactions] = useState([]);
  const [showModal,    setShowModal]    = useState(false);
  const [loading,      setLoading]      = useState(true);

  const load = async () => {
    try {
      setTransactions(await getTransactions());
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, []);

  const now        = new Date();
  const monthStart = new Date(now.getFullYear(), now.getMonth(), 1);
  const monthEnd   = new Date(now.getFullYear(), now.getMonth() + 1, 0);
  const dateStr    = now.toLocaleDateString('en-US', { weekday: 'long', month: 'long', day: 'numeric', year: 'numeric' });

  const income     = sumType(transactions, 'INCOME',     monthStart, monthEnd);
  const expense    = sumType(transactions, 'EXPENSE',    monthStart, monthEnd);
  const saving     = sumType(transactions, 'SAVING',     monthStart, monthEnd);
  const investment = sumType(transactions, 'INVESTMENT', monthStart, monthEnd);

  return (
    <div className="page active">
      <header className="page-header">
        <div>
          <h2 className="page-title">Dashboard</h2>
          <p className="page-date">{dateStr}</p>
        </div>
        <button className="btn-add" onClick={() => setShowModal(true)}>+ Add Transaction</button>
      </header>

      <div className="stat-grid">
        <StatCard label="Total Income"  value={fmt(income)}     type="income"  tag="this month" />
        <StatCard label="Expenses"      value={fmt(expense)}    type="expense" tag="this month" />
        <StatCard label="Savings"       value={fmt(saving)}     type="saving"  tag="this month" />
        <StatCard label="Investments"   value={fmt(investment)} type="invest"  tag="this month" />
      </div>

      {!loading && (
        <div className="charts-row">
          <div className="chart-card">
            <div className="chart-title">Monthly Breakdown</div>
            <BreakdownChart transactions={transactions} />
          </div>
          <div className="chart-card donut-card">
            <div className="chart-title">Allocation</div>
            <DonutChart income={income} expense={expense} saving={saving} investment={investment} />
          </div>
        </div>
      )}

      <div className="recent-section">
        <div className="section-header">
          <span>Recent Transactions</span>
          <a href="#" className="view-all" onClick={e => { e.preventDefault(); navigate('/transactions'); }}>
            View all →
          </a>
        </div>
        <RecentTransactions transactions={transactions} />
      </div>

      {showModal && (
        <TransactionModal
          onClose={() => setShowModal(false)}
          onSaved={() => { setShowModal(false); load(); }}
        />
      )}
    </div>
  );
}
