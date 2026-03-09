import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import {
  login,
  register,
  getTransactions,
  createTransaction,
  updateTransaction,
  deleteTransaction,
  getGoals,
  createGoal,
  depositGoal,
  deleteGoal,
  getDashboardSummary,
  getNotifications,
  getUnreadCount,
} from './client';

// ── Helpers ────────────────────────────────────────────────────────────────

function mockFetch(status, body) {
  global.fetch = vi.fn().mockResolvedValue({
    status,
    ok: status >= 200 && status < 300,
    json: () => Promise.resolve(body),
  });
}

// ── Setup ──────────────────────────────────────────────────────────────────

beforeEach(() => {
  localStorage.clear();
  window.location.href = '';
});

afterEach(() => {
  vi.restoreAllMocks();
});

// ── Authentication ─────────────────────────────────────────────────────────

describe('login', () => {
  it('POSTs to /api/auth/login with credentials', async () => {
    mockFetch(200, { token: 'abc', user: { id: 1 } });

    await login('alice', 'pass');

    expect(fetch).toHaveBeenCalledWith(
      '/api/auth/login',
      expect.objectContaining({
        method: 'POST',
        body: JSON.stringify({ username: 'alice', password: 'pass' }),
      })
    );
  });

  it('does not include Authorization header for login', async () => {
    mockFetch(200, { token: 'abc' });

    await login('alice', 'pass');

    const headers = fetch.mock.calls[0][1].headers;
    expect(headers['Authorization']).toBeUndefined();
  });

  it('returns response data on success', async () => {
    const data = { token: 'mytoken', user: { id: 1, username: 'alice' } };
    mockFetch(200, data);

    const result = await login('alice', 'pass');

    expect(result).toEqual(data);
  });
});

describe('register', () => {
  it('POSTs to /api/auth/register with all fields', async () => {
    mockFetch(200, { message: 'ok' });

    await register('Alice Smith', 'alice', 'alice@test.com', 'pass123');

    expect(fetch).toHaveBeenCalledWith(
      '/api/auth/register',
      expect.objectContaining({
        method: 'POST',
        body: JSON.stringify({
          fullName: 'Alice Smith',
          username: 'alice',
          email:    'alice@test.com',
          password: 'pass123',
        }),
      })
    );
  });
});

// ── Transactions ───────────────────────────────────────────────────────────

describe('getTransactions', () => {
  it('sends Authorization header when token is in localStorage', async () => {
    localStorage.setItem('ww_token', 'my-jwt');
    mockFetch(200, []);

    await getTransactions();

    const headers = fetch.mock.calls[0][1].headers;
    expect(headers['Authorization']).toBe('Bearer my-jwt');
  });

  it('GETs /api/transactions', async () => {
    localStorage.setItem('ww_token', 'tok');
    mockFetch(200, []);

    await getTransactions();

    expect(fetch).toHaveBeenCalledWith(
      '/api/transactions',
      expect.objectContaining({ method: 'GET' })
    );
  });
});

describe('createTransaction', () => {
  it('POSTs to /api/transactions with data', async () => {
    localStorage.setItem('ww_token', 'tok');
    const data = { type: 'INCOME', amount: 3000 };
    mockFetch(200, { id: 1, ...data });

    await createTransaction(data);

    expect(fetch).toHaveBeenCalledWith(
      '/api/transactions',
      expect.objectContaining({
        method: 'POST',
        body: JSON.stringify(data),
      })
    );
  });
});

describe('updateTransaction', () => {
  it('PUTs to /api/transactions/{id}', async () => {
    localStorage.setItem('ww_token', 'tok');
    const data = { type: 'EXPENSE', amount: 50 };
    mockFetch(200, { id: 5, ...data });

    await updateTransaction(5, data);

    expect(fetch).toHaveBeenCalledWith(
      '/api/transactions/5',
      expect.objectContaining({ method: 'PUT' })
    );
  });
});

describe('deleteTransaction', () => {
  it('DELETEs /api/transactions/{id}', async () => {
    localStorage.setItem('ww_token', 'tok');
    mockFetch(200, {});

    await deleteTransaction(7);

    expect(fetch).toHaveBeenCalledWith(
      '/api/transactions/7',
      expect.objectContaining({ method: 'DELETE' })
    );
  });
});

// ── Goals ──────────────────────────────────────────────────────────────────

describe('getGoals', () => {
  it('GETs /api/goals', async () => {
    localStorage.setItem('ww_token', 'tok');
    mockFetch(200, []);

    await getGoals();

    expect(fetch).toHaveBeenCalledWith(
      '/api/goals',
      expect.objectContaining({ method: 'GET' })
    );
  });
});

describe('createGoal', () => {
  it('POSTs to /api/goals with data', async () => {
    localStorage.setItem('ww_token', 'tok');
    const data = { name: 'Vacation', targetAmount: 2000 };
    mockFetch(200, { id: 1, ...data });

    await createGoal(data);

    expect(fetch).toHaveBeenCalledWith(
      '/api/goals',
      expect.objectContaining({ method: 'POST' })
    );
  });
});

describe('depositGoal', () => {
  it('PATCHes /api/goals/{id}/deposit with amount', async () => {
    localStorage.setItem('ww_token', 'tok');
    mockFetch(200, { id: 1, savedAmount: 500 });

    await depositGoal(1, 500);

    expect(fetch).toHaveBeenCalledWith(
      '/api/goals/1/deposit',
      expect.objectContaining({
        method: 'PATCH',
        body: JSON.stringify({ amount: 500 }),
      })
    );
  });
});

describe('deleteGoal', () => {
  it('DELETEs /api/goals/{id}', async () => {
    localStorage.setItem('ww_token', 'tok');
    mockFetch(200, {});

    await deleteGoal(3);

    expect(fetch).toHaveBeenCalledWith(
      '/api/goals/3',
      expect.objectContaining({ method: 'DELETE' })
    );
  });
});

// ── Dashboard ──────────────────────────────────────────────────────────────

describe('getDashboardSummary', () => {
  it('GETs /api/dashboard/summary', async () => {
    localStorage.setItem('ww_token', 'tok');
    mockFetch(200, {});

    await getDashboardSummary();

    expect(fetch).toHaveBeenCalledWith(
      '/api/dashboard/summary',
      expect.objectContaining({ method: 'GET' })
    );
  });
});

// ── 401 Auto-logout ───────────────────────────────────────────────────────

describe('401 handling', () => {
  it('clears localStorage and redirects to /auth on 401', async () => {
    localStorage.setItem('ww_token', 'expired');
    localStorage.setItem('ww_user', '{"id":1}');

    global.fetch = vi.fn().mockResolvedValue({
      status: 401,
      ok: false,
      json: () => Promise.resolve({}),
    });

    await getTransactions();

    expect(localStorage.getItem('ww_token')).toBeNull();
    expect(localStorage.getItem('ww_user')).toBeNull();
    expect(window.location.href).toBe('/auth');
  });
});

// ── Error handling ────────────────────────────────────────────────────────

describe('error handling', () => {
  it('throws when server returns non-OK status', async () => {
    localStorage.setItem('ww_token', 'tok');
    mockFetch(400, { message: 'Bad request' });

    await expect(getTransactions()).rejects.toThrow('Bad request');
  });
});
