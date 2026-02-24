import { useState } from 'react';
import { useAuth } from '../context/AuthContext';

export default function AuthPage() {
  const { login, register } = useAuth();
  const [view, setView]     = useState('login');
  const [error, setError]   = useState('');
  const [loading, setLoading] = useState(false);

  const [loginData, setLoginData] = useState({ username: '', password: '' });
  const [regData,   setRegData]   = useState({ fullName: '', username: '', email: '', password: '' });

  const setLogin = (key, val) => setLoginData(p => ({ ...p, [key]: val }));
  const setReg   = (key, val) => setRegData(p  => ({ ...p, [key]: val }));

  const handleLogin = async () => {
    setError('');
    if (!loginData.username || !loginData.password)
      return setError('Please fill all fields');
    setLoading(true);
    try {
      await login(loginData.username, loginData.password);
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  };

  const handleRegister = async () => {
    setError('');
    const { fullName, username, email, password } = regData;
    if (!fullName || !username || !email || !password)
      return setError('Please fill all fields');
    setLoading(true);
    try {
      await register(fullName, username, email, password);
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  };

  const switchTo = (v) => { setView(v); setError(''); };

  return (
    <div className="auth-screen">
      <div className="auth-bg">
        <div className="orb orb1" />
        <div className="orb orb2" />
        <div className="orb orb3" />
      </div>

      <div className="auth-container">
        <div className="auth-brand">
          <span className="brand-icon">◈</span>
          <span className="brand-name">WealthWise</span>
        </div>

        {view === 'login' ? (
          <div className="auth-panel active">
            <h1 className="auth-title">Welcome back.</h1>
            <p className="auth-subtitle">Your finances await.</p>
            {error && <div className="error-msg">{error}</div>}

            <div className="field">
              <label>Username</label>
              <input
                type="text"
                placeholder="your_username"
                autoComplete="username"
                value={loginData.username}
                onChange={e => setLogin('username', e.target.value)}
                onKeyDown={e => e.key === 'Enter' && handleLogin()}
              />
            </div>
            <div className="field">
              <label>Password</label>
              <input
                type="password"
                placeholder="••••••••"
                autoComplete="current-password"
                value={loginData.password}
                onChange={e => setLogin('password', e.target.value)}
                onKeyDown={e => e.key === 'Enter' && handleLogin()}
              />
            </div>
            <button className="btn-primary" onClick={handleLogin} disabled={loading}>
              {loading ? 'Signing in…' : 'Sign In →'}
            </button>
            <p className="auth-switch">
              No account?{' '}
              <a href="#" onClick={e => { e.preventDefault(); switchTo('register'); }}>
                Create one
              </a>
            </p>
          </div>
        ) : (
          <div className="auth-panel active">
            <h1 className="auth-title">Get started.</h1>
            <p className="auth-subtitle">Build your financial future.</p>
            {error && <div className="error-msg">{error}</div>}

            <div className="field">
              <label>Full Name</label>
              <input type="text" placeholder="Jane Doe"
                value={regData.fullName} onChange={e => setReg('fullName', e.target.value)} />
            </div>
            <div className="field">
              <label>Username</label>
              <input type="text" placeholder="jane_doe"
                value={regData.username} onChange={e => setReg('username', e.target.value)} />
            </div>
            <div className="field">
              <label>Email</label>
              <input type="email" placeholder="jane@example.com"
                value={regData.email} onChange={e => setReg('email', e.target.value)} />
            </div>
            <div className="field">
              <label>Password</label>
              <input type="password" placeholder="••••••••"
                value={regData.password}
                onChange={e => setReg('password', e.target.value)}
                onKeyDown={e => e.key === 'Enter' && handleRegister()} />
            </div>
            <button className="btn-primary" onClick={handleRegister} disabled={loading}>
              {loading ? 'Creating account…' : 'Create Account →'}
            </button>
            <p className="auth-switch">
              Have an account?{' '}
              <a href="#" onClick={e => { e.preventDefault(); switchTo('login'); }}>
                Sign in
              </a>
            </p>
          </div>
        )}
      </div>
    </div>
  );
}
