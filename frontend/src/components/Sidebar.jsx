import { Link, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Sidebar() {
  const { user, logout } = useAuth();
  const { pathname }     = useLocation();

  const name = user?.fullName || user?.username || 'User';

  const navItem = (to, icon, label) => (
    <Link to={to} className={`nav-item${pathname === to ? ' active' : ''}`}>
      <span className="nav-icon">{icon}</span> {label}
    </Link>
  );

  return (
    <aside className="sidebar">
      <div className="sidebar-brand">
        <span className="brand-icon">◈</span>
        <span className="brand-name">WealthWise</span>
      </div>

      <nav className="sidebar-nav">
        {navItem('/',             '▣', 'Dashboard')}
        {navItem('/transactions', '↕', 'Transactions')}
        {navItem('/forecast',     '◎', 'Forecast')}
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
