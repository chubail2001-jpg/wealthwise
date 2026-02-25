import { Link, useLocation } from 'react-router-dom';
import { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { getUnreadCount } from '../api/client';

export default function Sidebar() {
  const { user, logout }   = useAuth();
  const { pathname }       = useLocation();
  const [unread, setUnread] = useState(0);

  const name = user?.fullName || user?.username || 'User';

  // Poll unread count every 30 seconds
  useEffect(() => {
    function fetchCount() {
      getUnreadCount()
        .then(d => setUnread(d?.count ?? 0))
        .catch(() => {});
    }
    fetchCount();
    const timer = setInterval(fetchCount, 30000);
    return () => clearInterval(timer);
  }, []);

  // Reset badge when user navigates to notifications page
  useEffect(() => {
    if (pathname === '/notifications') setUnread(0);
  }, [pathname]);

  const navItem = (to, icon, label, badge) => (
    <Link to={to} className={`nav-item${pathname === to ? ' active' : ''}`}>
      <span className="nav-icon">{icon}</span>
      <span>{label}</span>
      {badge > 0 && <span className="nav-badge">{badge > 99 ? '99+' : badge}</span>}
    </Link>
  );

  return (
    <aside className="sidebar">
      <div className="sidebar-brand">
        <span className="brand-icon">◈</span>
        <span className="brand-name">WealthWise</span>
      </div>

      <nav className="sidebar-nav">
        {navItem('/',               '▣', 'Dashboard')}
        {navItem('/transactions',   '↕', 'Transactions')}
        {navItem('/forecast',       '◎', 'Forecast')}
        {navItem('/insights',       '✦', 'Insights')}
        {navItem('/goals',          '◉', 'Goals')}
        {navItem('/notifications',  '🔔', 'Notifications', unread)}
        {navItem('/reports',        '◫',  'Reports')}
      </nav>

      <div className="sidebar-user">
        <div className="user-avatar">{name[0].toUpperCase()}</div>
        <div className="user-info">
          <div className="user-name">{name.split(' ')[0]}</div>
          <div className="user-role">Member</div>
        </div>
        <button className="logout-btn" onClick={logout} title="Logout">⏻</button>
      </div>
    </aside>
  );
}
