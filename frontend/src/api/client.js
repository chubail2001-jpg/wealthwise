// All requests go through Vite's dev proxy (/api → http://localhost:8080/api)
const BASE = '/api';

async function request(path, method = 'GET', body = null, auth = true) {
  const headers = { 'Content-Type': 'application/json' };
  if (auth) {
    const token = localStorage.getItem('ww_token');
    if (token) headers['Authorization'] = `Bearer ${token}`;
  }

  const res = await fetch(BASE + path, {
    method,
    headers,
    body: body ? JSON.stringify(body) : undefined,
  });

  // Auto-logout on expired / invalid token
  if (res.status === 401) {
    localStorage.removeItem('ww_token');
    localStorage.removeItem('ww_user');
    window.location.href = '/auth';
    return;
  }

  const data = await res.json().catch(() => ({}));
  if (!res.ok) throw new Error(data.message || 'Request failed');
  return data;
}

// Auth
export const login    = (username, password) =>
  request('/auth/login',    'POST', { username, password }, false);

export const register = (fullName, username, email, password) =>
  request('/auth/register', 'POST', { fullName, username, email, password }, false);

// Transactions
export const getTransactions    = ()         => request('/transactions');
export const createTransaction  = (data)     => request('/transactions',       'POST',   data);
export const updateTransaction  = (id, data) => request(`/transactions/${id}`, 'PUT',    data);
export const deleteTransaction  = (id)       => request(`/transactions/${id}`, 'DELETE');

// Dashboard, Forecast & Insights
export const getDashboardSummary = () => request('/dashboard/summary');
export const getForecast         = () => request('/forecast');
export const getInsights         = () => request('/insights');

// Notifications
export const getNotifications    = ()     => request('/notifications');
export const getUnreadCount      = ()     => request('/notifications/unread-count');
export const markNotificationRead = (id)  => request(`/notifications/${id}/read`, 'PATCH');
export const markAllNotificationsRead = () => request('/notifications/read-all', 'PATCH');

// Goals
export const getGoals      = ()              => request('/goals');
export const createGoal    = (data)          => request('/goals',            'POST',  data);
export const updateGoal    = (id, data)      => request(`/goals/${id}`,      'PUT',   data);
export const depositGoal   = (id, amount)    => request(`/goals/${id}/deposit`, 'PATCH', { amount });
export const deleteGoal    = (id)            => request(`/goals/${id}`,      'DELETE');

// Reports
export const getReportSummary = (month) => request(`/reports/summary?month=${month}`);

export async function downloadReport(type, month) {
  const token = localStorage.getItem('ww_token');
  const res = await fetch(`/api/reports/${type}?month=${month}`, {
    headers: { 'Authorization': `Bearer ${token}` },
  });
  if (res.status === 401) {
    localStorage.removeItem('ww_token');
    localStorage.removeItem('ww_user');
    window.location.href = '/auth';
    return;
  }
  if (!res.ok) throw new Error('Download failed');
  const blob = await res.blob();
  const url  = window.URL.createObjectURL(blob);
  const a    = document.createElement('a');
  a.href     = url;
  a.download = `wealthwise-${month}.${type}`;
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
  window.URL.revokeObjectURL(url);
}
