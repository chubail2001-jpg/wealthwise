import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, act } from '@testing-library/react';
import { AuthProvider, useAuth } from './AuthContext';

// ── Mock the API client ────────────────────────────────────────────────────

vi.mock('../api/client', () => ({
  login:    vi.fn(),
  register: vi.fn(),
}));

import * as client from '../api/client';

// ── Helper: component that exposes AuthContext values ──────────────────────

function TestConsumer() {
  const auth = useAuth();
  return (
    <div>
      <span data-testid="authenticated">{String(auth.isAuthenticated)}</span>
      <span data-testid="username">{auth.user?.username ?? 'null'}</span>
      <span data-testid="token">{auth.token ?? 'null'}</span>
      <button onClick={() => auth.login('alice', 'pass')}>Login</button>
      <button onClick={() => auth.register('Alice', 'alice', 'a@b.com', 'pass')}>Register</button>
      <button onClick={() => auth.logout()}>Logout</button>
    </div>
  );
}

function renderWithAuth() {
  return render(
    <AuthProvider>
      <TestConsumer />
    </AuthProvider>
  );
}

// ── Setup ──────────────────────────────────────────────────────────────────

beforeEach(() => {
  localStorage.clear();
  vi.clearAllMocks();
});

afterEach(() => {
  vi.restoreAllMocks();
});

// ── Tests ──────────────────────────────────────────────────────────────────

describe('AuthContext initial state', () => {
  it('isAuthenticated is false when no token in localStorage', () => {
    renderWithAuth();
    expect(screen.getByTestId('authenticated').textContent).toBe('false');
  });

  it('isAuthenticated is true when token exists in localStorage', () => {
    localStorage.setItem('ww_token', 'existing-token');
    localStorage.setItem('ww_user', JSON.stringify({ id: 1, username: 'alice' }));

    renderWithAuth();

    expect(screen.getByTestId('authenticated').textContent).toBe('true');
    expect(screen.getByTestId('username').textContent).toBe('alice');
  });
});

describe('login', () => {
  it('stores token and user in localStorage on success', async () => {
    client.login.mockResolvedValue({
      token: 'jwt-token',
      user:  { id: 1, username: 'alice', fullName: 'Alice' },
    });

    renderWithAuth();

    await act(async () => {
      screen.getByText('Login').click();
    });

    expect(localStorage.getItem('ww_token')).toBe('jwt-token');
    expect(JSON.parse(localStorage.getItem('ww_user')).username).toBe('alice');
  });

  it('sets isAuthenticated to true after login', async () => {
    client.login.mockResolvedValue({
      token: 'jwt-token',
      user:  { id: 1, username: 'alice' },
    });

    renderWithAuth();

    await act(async () => {
      screen.getByText('Login').click();
    });

    expect(screen.getByTestId('authenticated').textContent).toBe('true');
  });

  it('exposes the token after login', async () => {
    client.login.mockResolvedValue({
      token: 'my-jwt',
      user:  { id: 1, username: 'alice' },
    });

    renderWithAuth();

    await act(async () => {
      screen.getByText('Login').click();
    });

    expect(screen.getByTestId('token').textContent).toBe('my-jwt');
  });
});

describe('logout', () => {
  it('clears token and user from localStorage', async () => {
    localStorage.setItem('ww_token', 'existing-token');
    localStorage.setItem('ww_user', JSON.stringify({ id: 1, username: 'alice' }));

    renderWithAuth();

    await act(async () => {
      screen.getByText('Logout').click();
    });

    expect(localStorage.getItem('ww_token')).toBeNull();
    expect(localStorage.getItem('ww_user')).toBeNull();
  });

  it('sets isAuthenticated to false after logout', async () => {
    localStorage.setItem('ww_token', 'existing-token');
    localStorage.setItem('ww_user', JSON.stringify({ id: 1, username: 'alice' }));

    renderWithAuth();

    expect(screen.getByTestId('authenticated').textContent).toBe('true');

    await act(async () => {
      screen.getByText('Logout').click();
    });

    expect(screen.getByTestId('authenticated').textContent).toBe('false');
  });

  it('clears token state after logout', async () => {
    localStorage.setItem('ww_token', 'tok');
    localStorage.setItem('ww_user', JSON.stringify({ id: 1 }));

    renderWithAuth();

    await act(async () => {
      screen.getByText('Logout').click();
    });

    expect(screen.getByTestId('token').textContent).toBe('null');
  });
});

describe('register', () => {
  it('calls apiRegister then apiLogin', async () => {
    client.register.mockResolvedValue({});
    client.login.mockResolvedValue({
      token: 'new-token',
      user:  { id: 2, username: 'alice' },
    });

    renderWithAuth();

    await act(async () => {
      screen.getByText('Register').click();
    });

    expect(client.register).toHaveBeenCalledWith('Alice', 'alice', 'a@b.com', 'pass');
    expect(client.login).toHaveBeenCalledWith('alice', 'pass');
  });

  it('sets isAuthenticated to true after register', async () => {
    client.register.mockResolvedValue({});
    client.login.mockResolvedValue({
      token: 'new-token',
      user:  { id: 2, username: 'alice' },
    });

    renderWithAuth();

    await act(async () => {
      screen.getByText('Register').click();
    });

    expect(screen.getByTestId('authenticated').textContent).toBe('true');
  });
});
