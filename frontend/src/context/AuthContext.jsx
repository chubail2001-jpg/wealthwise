import { createContext, useContext, useState } from 'react';
import { login as apiLogin, register as apiRegister } from '../api/client';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [token, setToken] = useState(() => localStorage.getItem('ww_token'));
  const [user, setUser]   = useState(() => {
    try { return JSON.parse(localStorage.getItem('ww_user') || 'null'); }
    catch { return null; }
  });

  const login = async (username, password) => {
    const data = await apiLogin(username, password);
    setToken(data.token);
    setUser(data.user);
    localStorage.setItem('ww_token', data.token);
    localStorage.setItem('ww_user', JSON.stringify(data.user));
    return data;
  };

  const register = async (fullName, username, email, password) => {
    await apiRegister(fullName, username, email, password);
    return login(username, password);
  };

  const logout = () => {
    setToken(null);
    setUser(null);
    localStorage.removeItem('ww_token');
    localStorage.removeItem('ww_user');
  };

  return (
    <AuthContext.Provider value={{ token, user, login, register, logout, isAuthenticated: !!token }}>
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => useContext(AuthContext);
