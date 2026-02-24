/* ===== WEALTHWISE — FRONTEND APP ===== */

const API = 'http://localhost:8080/api';
let token = localStorage.getItem('ww_token');
let currentUser = JSON.parse(localStorage.getItem('ww_user') || 'null');
let allTransactions = [];
let editingTxId = null;
let deletingTxId = null;

// ─── Charts ──────────────────────────────────────────────
let breakdownChart = null, donutChart = null, forecastChart = null;

// ─── Utility ─────────────────────────────────────────────
const fmt = n => '$' + Number(n).toLocaleString('en-US', {minimumFractionDigits: 2, maximumFractionDigits: 2});
const fmtDate = s => new Date(s).toLocaleDateString('en-US', {month:'short', day:'numeric', year:'numeric'});
const typeIcon = { INCOME: '↑', EXPENSE: '↓', SAVING: '◎', INVESTMENT: '◈' };
const months = ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec'];

function showError(id, msg) {
  const el = document.getElementById(id);
  el.textContent = msg;
  el.classList.remove('hidden');
}
function clearError(id) {
  document.getElementById(id).classList.add('hidden');
}

// ─── Auth ─────────────────────────────────────────────────
async function apiCall(endpoint, method='GET', body=null, auth=true) {
  const headers = { 'Content-Type': 'application/json' };
  if (auth && token) headers['Authorization'] = `Bearer ${token}`;
  const res = await fetch(API + endpoint, {
    method, headers, body: body ? JSON.stringify(body) : null
  });
  const data = await res.json().catch(() => ({}));
  if (!res.ok) throw new Error(data.message || data.error || 'Request failed');
  return data;
}

document.getElementById('go-register').addEventListener('click', e => {
  e.preventDefault();
  document.getElementById('login-panel').classList.remove('active');
  document.getElementById('register-panel').classList.add('active');
});

document.getElementById('go-login').addEventListener('click', e => {
  e.preventDefault();
  document.getElementById('register-panel').classList.remove('active');
  document.getElementById('login-panel').classList.add('active');
});

document.getElementById('login-btn').addEventListener('click', async () => {
  clearError('login-error');
  const username = document.getElementById('login-username').value.trim();
  const password = document.getElementById('login-password').value;
  if (!username || !password) return showError('login-error', 'Please fill all fields');
  try {
    const data = await apiCall('/auth/login', 'POST', { username, password }, false);
    token = data.token;
    currentUser = data.user;
    localStorage.setItem('ww_token', token);
    localStorage.setItem('ww_user', JSON.stringify(currentUser));
    enterApp();
  } catch(e) { showError('login-error', e.message); }
});

document.getElementById('register-btn').addEventListener('click', async () => {
  clearError('register-error');
  const name = document.getElementById('reg-name').value.trim();
  const username = document.getElementById('reg-username').value.trim();
  const email = document.getElementById('reg-email').value.trim();
  const password = document.getElementById('reg-password').value;
  if (!name || !username || !email || !password) return showError('register-error', 'Please fill all fields');
  try {
    await apiCall('/auth/register', 'POST', { fullName: name, username, email, password }, false);
    // Auto login after register
    const data = await apiCall('/auth/login', 'POST', { username, password }, false);
    token = data.token;
    currentUser = data.user;
    localStorage.setItem('ww_token', token);
    localStorage.setItem('ww_user', JSON.stringify(currentUser));
    enterApp();
  } catch(e) { showError('register-error', e.message); }
});

document.getElementById('logout-btn').addEventListener('click', () => {
  token = null; currentUser = null;
  localStorage.removeItem('ww_token');
  localStorage.removeItem('ww_user');
  document.getElementById('app').classList.add('hidden');
  document.getElementById('auth-screen').classList.remove('hidden');
  document.getElementById('login-panel').classList.add('active');
  document.getElementById('register-panel').classList.remove('active');
});

// ─── App Init ─────────────────────────────────────────────
function enterApp() {
  document.getElementById('auth-screen').classList.add('hidden');
  document.getElementById('app').classList.remove('hidden');
  const name = currentUser?.fullName || currentUser?.username || 'User';
  document.getElementById('user-name-sidebar').textContent = name.split(' ')[0];
  document.getElementById('user-avatar').textContent = name[0].toUpperCase();
  document.getElementById('today-date').textContent = new Date().toLocaleDateString('en-US',{weekday:'long',month:'long',day:'numeric',year:'numeric'});
  document.getElementById('tx-date').valueAsDate = new Date();
  loadTransactions();
}

// Auto-login if token saved
if (token && currentUser) enterApp();

// ─── Navigation ───────────────────────────────────────────
document.querySelectorAll('[data-page]').forEach(el => {
  el.addEventListener('click', e => {
    e.preventDefault();
    navigateTo(el.dataset.page);
  });
});

function navigateTo(page) {
  document.querySelectorAll('.nav-item').forEach(n => n.classList.toggle('active', n.dataset.page === page));
  document.querySelectorAll('.page').forEach(p => p.classList.toggle('active', p.id === `page-${page}`));
  if (page === 'forecast') renderForecast();
  if (page === 'transactions') renderTxTable();
}

// Inline view-all link
document.querySelector('.nav-link-inline').addEventListener('click', e => {
  e.preventDefault();
  navigateTo('transactions');
});

// ─── Transactions CRUD ────────────────────────────────────
async function loadTransactions() {
  try {
    allTransactions = await apiCall('/transactions');
    renderDashboard();
    renderTxTable();
  } catch(e) { console.error(e); }
}

function renderDashboard() {
  const now = new Date();
  const thisMonth = allTransactions.filter(t => {
    const d = new Date(t.date);
    return d.getMonth() === now.getMonth() && d.getFullYear() === now.getFullYear();
  });

  const sum = (type) => thisMonth.filter(t => t.type === type).reduce((a,b) => a + b.amount, 0);
  document.getElementById('stat-income').textContent  = fmt(sum('INCOME'));
  document.getElementById('stat-expense').textContent = fmt(sum('EXPENSE'));
  document.getElementById('stat-saving').textContent  = fmt(sum('SAVING'));
  document.getElementById('stat-invest').textContent  = fmt(sum('INVESTMENT'));

  renderBreakdownChart();
  renderDonutChart(sum);
  renderRecentTx();
}

function renderRecentTx() {
  const sorted = [...allTransactions].sort((a,b) => new Date(b.date) - new Date(a.date)).slice(0, 8);
  const el = document.getElementById('recent-tx-list');
  if (!sorted.length) { el.innerHTML = '<div class="empty-state">No transactions yet. Add your first one!</div>'; return; }
  el.innerHTML = sorted.map(tx => `
    <div class="tx-item">
      <div class="tx-dot ${tx.type}">${typeIcon[tx.type]}</div>
      <div class="tx-info">
        <div class="tx-desc">${esc(tx.description)}</div>
        <div class="tx-cat">${esc(tx.category || '—')}</div>
      </div>
      <div class="tx-date-col">${fmtDate(tx.date)}</div>
      <div class="tx-amount ${tx.type}">${tx.type==='EXPENSE'?'-':'+'} ${fmt(tx.amount)}</div>
    </div>
  `).join('');
}

function renderBreakdownChart() {
  const ctx = document.getElementById('breakdown-chart').getContext('2d');
  const last6 = [];
  const now = new Date();
  for (let i = 5; i >= 0; i--) {
    const d = new Date(now.getFullYear(), now.getMonth() - i, 1);
    last6.push({ label: months[d.getMonth()] + ' ' + d.getFullYear().toString().slice(2), month: d.getMonth(), year: d.getFullYear() });
  }

  const getData = type => last6.map(m =>
    allTransactions.filter(t => {
      const d = new Date(t.date);
      return t.type === type && d.getMonth() === m.month && d.getFullYear() === m.year;
    }).reduce((a,b) => a + b.amount, 0)
  );

  if (breakdownChart) breakdownChart.destroy();
  breakdownChart = new Chart(ctx, {
    type: 'bar',
    data: {
      labels: last6.map(m => m.label),
      datasets: [
        { label: 'Income',     data: getData('INCOME'),     backgroundColor: 'rgba(78,203,141,0.7)',  borderRadius: 4 },
        { label: 'Expense',    data: getData('EXPENSE'),    backgroundColor: 'rgba(224,92,107,0.7)',  borderRadius: 4 },
        { label: 'Saving',     data: getData('SAVING'),     backgroundColor: 'rgba(91,156,246,0.7)', borderRadius: 4 },
        { label: 'Investment', data: getData('INVESTMENT'), backgroundColor: 'rgba(181,123,238,0.7)',borderRadius: 4 },
      ]
    },
    options: {
      responsive: true, maintainAspectRatio: true,
      plugins: { legend: { labels: { color: '#6b6b8a', font: { family: 'DM Mono', size: 11 } } } },
      scales: {
        x: { ticks: { color: '#6b6b8a', font: { family: 'DM Mono', size: 11 } }, grid: { color: '#2a2a38' } },
        y: { ticks: { color: '#6b6b8a', font: { family: 'DM Mono', size: 11 }, callback: v => '$'+v.toLocaleString() }, grid: { color: '#2a2a38' } }
      }
    }
  });
}

function renderDonutChart(sum) {
  const ctx = document.getElementById('donut-chart').getContext('2d');
  const data = [sum('INCOME'), sum('EXPENSE'), sum('SAVING'), sum('INVESTMENT')];
  const labels = ['Income','Expense','Saving','Investment'];
  const colors = ['#4ecb8d','#e05c6b','#5b9cf6','#b57bee'];

  if (donutChart) donutChart.destroy();
  donutChart = new Chart(ctx, {
    type: 'doughnut',
    data: { labels, datasets: [{ data, backgroundColor: colors, borderWidth: 0, hoverOffset: 8 }] },
    options: {
      responsive: false, cutout: '68%',
      plugins: { legend: { display: false }, tooltip: { callbacks: { label: ctx => ` ${ctx.label}: ${fmt(ctx.raw)}` } } }
    }
  });

  document.getElementById('donut-legend').innerHTML = labels.map((l,i) => `
    <div class="legend-item">
      <div class="legend-dot" style="background:${colors[i]}"></div>
      <span>${l}</span>
    </div>
  `).join('');
}

// ─── Transactions Table ───────────────────────────────────
function renderTxTable() {
  const typeFilter = document.getElementById('filter-type').value;
  const monthFilter = document.getElementById('filter-month').value;
  let txs = [...allTransactions].sort((a,b) => new Date(b.date) - new Date(a.date));
  if (typeFilter) txs = txs.filter(t => t.type === typeFilter);
  if (monthFilter) {
    const [y, m] = monthFilter.split('-').map(Number);
    txs = txs.filter(t => { const d = new Date(t.date); return d.getFullYear()===y && d.getMonth()+1===m; });
  }
  const tbody = document.getElementById('tx-table-body');
  if (!txs.length) { tbody.innerHTML = '<tr><td colspan="6" class="empty-state">No transactions found.</td></tr>'; return; }
  tbody.innerHTML = txs.map(tx => `
    <tr>
      <td>${fmtDate(tx.date)}</td>
      <td>${esc(tx.description)}</td>
      <td>${esc(tx.category || '—')}</td>
      <td><span class="type-badge ${tx.type}">${tx.type}</span></td>
      <td class="tx-amount ${tx.type}">${tx.type==='EXPENSE'?'-':'+'} ${fmt(tx.amount)}</td>
      <td>
        <div class="action-btns">
          <button class="action-btn" onclick="editTx(${tx.id})">Edit</button>
          <button class="action-btn del" onclick="confirmDelete(${tx.id})">Delete</button>
        </div>
      </td>
    </tr>
  `).join('');
}

document.getElementById('filter-type').addEventListener('change', renderTxTable);
document.getElementById('filter-month').addEventListener('change', renderTxTable);

// ─── Forecast ─────────────────────────────────────────────
function renderForecast() {
  const now = new Date();
  // Calculate avg monthly net for last 3 months
  const getTotals = (mOffset) => {
    const d = new Date(now.getFullYear(), now.getMonth() - mOffset, 1);
    const txs = allTransactions.filter(t => {
      const td = new Date(t.date);
      return td.getMonth() === d.getMonth() && td.getFullYear() === d.getFullYear();
    });
    const income  = txs.filter(t=>t.type==='INCOME').reduce((a,b)=>a+b.amount,0);
    const expense = txs.filter(t=>t.type==='EXPENSE').reduce((a,b)=>a+b.amount,0);
    const saving  = txs.filter(t=>t.type==='SAVING').reduce((a,b)=>a+b.amount,0);
    const invest  = txs.filter(t=>t.type==='INVESTMENT').reduce((a,b)=>a+b.amount,0);
    return { income, expense, saving, invest, net: income - expense };
  };

  const hist = [getTotals(2), getTotals(1), getTotals(0)];
  const avgNet = hist.reduce((a,b)=>a+b.net,0) / 3;
  const currentBalance = allTransactions.reduce((acc, t) => {
    if (t.type==='INCOME') return acc + t.amount;
    if (t.type==='EXPENSE') return acc - t.amount;
    return acc;
  }, 0);

  const projected = [1,2,3].map(i => {
    const d = new Date(now.getFullYear(), now.getMonth() + i, 1);
    return { label: months[d.getMonth()] + ' ' + d.getFullYear(), value: currentBalance + avgNet * i };
  });

  projected.forEach((p,i) => {
    document.getElementById(`f-month${i+1}`).textContent = p.label;
    document.getElementById(`f-val${i+1}`).textContent = fmt(p.value);
    document.getElementById(`f-val${i+1}`).style.color = p.value >= 0 ? '#4ecb8d' : '#e05c6b';
  });

  // Forecast chart
  const histLabels = [2,1,0].map(o => {
    const d = new Date(now.getFullYear(), now.getMonth()-o, 1);
    return months[d.getMonth()];
  });
  const allLabels = [...histLabels, ...projected.map(p=>p.label)];
  const histBalances = hist.map((_,i) => {
    return allTransactions.filter(t => {
      const d = new Date(t.date);
      const ref = new Date(now.getFullYear(), now.getMonth()-(2-i), 1);
      return d <= new Date(ref.getFullYear(), ref.getMonth()+1, 0);
    }).reduce((acc,t) => {
      if (t.type==='INCOME') return acc+t.amount;
      if (t.type==='EXPENSE') return acc-t.amount;
      return acc;
    },0);
  });
  const allValues = [...histBalances, ...projected.map(p=>p.value)];

  const ctx = document.getElementById('forecast-chart').getContext('2d');
  if (forecastChart) forecastChart.destroy();
  forecastChart = new Chart(ctx, {
    type: 'line',
    data: {
      labels: allLabels,
      datasets: [
        {
          label: 'Balance', data: allValues,
          borderColor: '#c8a96e', backgroundColor: 'rgba(200,169,110,0.1)',
          pointBackgroundColor: (ctx) => ctx.dataIndex >= 3 ? '#b57bee' : '#c8a96e',
          pointRadius: 5, tension: 0.4, fill: true,
          segment: {
            borderColor: ctx => ctx.p0DataIndex >= 2 ? '#b57bee' : '#c8a96e',
            borderDash: ctx => ctx.p0DataIndex >= 2 ? [6,3] : [],
          }
        }
      ]
    },
    options: {
      responsive: true, maintainAspectRatio: true,
      plugins: {
        legend: { display: false },
        tooltip: { callbacks: { label: ctx => ' Balance: ' + fmt(ctx.raw) } }
      },
      scales: {
        x: { ticks: { color: '#6b6b8a', font: { family: 'DM Mono', size: 11 } }, grid: { color: '#2a2a38' } },
        y: { ticks: { color: '#6b6b8a', font: { family: 'DM Mono', size: 11 }, callback: v => '$'+v.toLocaleString() }, grid: { color: '#2a2a38' } }
      }
    }
  });

  // Insights
  const trend = avgNet > 0 ? 'positive' : 'negative';
  const savingsRate = hist[2].income > 0 ? ((hist[2].saving / hist[2].income)*100).toFixed(1) : 0;
  const expenseRatio = hist[2].income > 0 ? ((hist[2].expense / hist[2].income)*100).toFixed(1) : 0;
  document.getElementById('insight-text').innerHTML = `
    Your average monthly net cashflow is <strong style="color:${avgNet>=0?'#4ecb8d':'#e05c6b'}">${fmt(avgNet)}</strong>. 
    Based on this ${trend} trend, your projected balance in 3 months is <strong style="color:${projected[2].value>=0?'#4ecb8d':'#e05c6b'}">${fmt(projected[2].value)}</strong>.
    ${hist[2].income > 0 
      ? `This month, you saved <strong style="color:#5b9cf6">${savingsRate}%</strong> of your income and spent <strong style="color:#e05c6b">${expenseRatio}%</strong> on expenses.` 
      : 'Add transactions this month to see detailed insights.'}
    ${avgNet < 0 ? ' ⚠️ Consider reducing expenses to improve your financial trajectory.' : ' ✓ Keep it up — your wealth is growing steadily.'}
  `;
}

// ─── Add / Edit Modal ─────────────────────────────────────
function openModal(tx=null) {
  editingTxId = tx ? tx.id : null;
  document.getElementById('modal-title').textContent = tx ? 'Edit Transaction' : 'New Transaction';
  document.getElementById('tx-type').value     = tx?.type       || 'INCOME';
  document.getElementById('tx-amount').value   = tx?.amount     || '';
  document.getElementById('tx-desc').value     = tx?.description|| '';
  document.getElementById('tx-category').value = tx?.category   || '';
  document.getElementById('tx-date').value     = tx?.date       ? tx.date.slice(0,10) : new Date().toISOString().slice(0,10);
  clearError('modal-error');
  document.getElementById('add-modal').classList.remove('hidden');
}

function editTx(id) {
  const tx = allTransactions.find(t => t.id === id);
  if (tx) openModal(tx);
}

document.getElementById('open-add-modal').addEventListener('click', () => openModal());
document.getElementById('open-add-modal-2').addEventListener('click', () => openModal());
document.getElementById('close-modal').addEventListener('click', () => document.getElementById('add-modal').classList.add('hidden'));
document.getElementById('cancel-modal').addEventListener('click', () => document.getElementById('add-modal').classList.add('hidden'));

document.getElementById('save-tx-btn').addEventListener('click', async () => {
  clearError('modal-error');
  const type     = document.getElementById('tx-type').value;
  const amount   = parseFloat(document.getElementById('tx-amount').value);
  const description = document.getElementById('tx-desc').value.trim();
  const category = document.getElementById('tx-category').value.trim();
  const date     = document.getElementById('tx-date').value;

  if (!description) return showError('modal-error', 'Description is required');
  if (!amount || amount <= 0) return showError('modal-error', 'Please enter a valid amount');
  if (!date) return showError('modal-error', 'Date is required');

  try {
    if (editingTxId) {
      await apiCall(`/transactions/${editingTxId}`, 'PUT', { type, amount, description, category, date });
    } else {
      await apiCall('/transactions', 'POST', { type, amount, description, category, date });
    }
    document.getElementById('add-modal').classList.add('hidden');
    await loadTransactions();
    if (document.getElementById('page-forecast').classList.contains('active')) renderForecast();
  } catch(e) { showError('modal-error', e.message); }
});

// ─── Delete ───────────────────────────────────────────────
function confirmDelete(id) {
  deletingTxId = id;
  document.getElementById('delete-modal').classList.remove('hidden');
}

document.getElementById('close-delete-modal').addEventListener('click', () => document.getElementById('delete-modal').classList.add('hidden'));
document.getElementById('cancel-delete').addEventListener('click', () => document.getElementById('delete-modal').classList.add('hidden'));

document.getElementById('confirm-delete').addEventListener('click', async () => {
  try {
    await apiCall(`/transactions/${deletingTxId}`, 'DELETE');
    document.getElementById('delete-modal').classList.add('hidden');
    await loadTransactions();
  } catch(e) { alert(e.message); }
});

// Close modals on overlay click
document.getElementById('add-modal').addEventListener('click', e => { if (e.target === document.getElementById('add-modal')) document.getElementById('add-modal').classList.add('hidden'); });
document.getElementById('delete-modal').addEventListener('click', e => { if (e.target === document.getElementById('delete-modal')) document.getElementById('delete-modal').classList.add('hidden'); });

// ─── Escape HTML ──────────────────────────────────────────
function esc(s) {
  return String(s).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;');
}
