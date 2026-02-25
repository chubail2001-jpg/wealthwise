import { useState, useEffect } from 'react';
import {
  getNotifications,
  markNotificationRead,
  markAllNotificationsRead,
} from '../api/client';

const TYPE_META = {
  BUDGET_EXCEEDED:  { color: 'var(--expense)', bar: '#e05c6b', icon: '⚠',  label: 'Budget Alert'  },
  GOAL_REACHED:     { color: 'var(--income)',  bar: '#4ecb8d', icon: '✓',  label: 'Goal Reached'  },
  INCOME_DETECTED:  { color: 'var(--saving)',  bar: '#5b9cf6', icon: '↑',  label: 'Income'        },
};

function timeAgo(dateStr) {
  const diff = Date.now() - new Date(dateStr).getTime();
  const m = Math.floor(diff / 60000);
  if (m < 1)  return 'just now';
  if (m < 60) return `${m}m ago`;
  const h = Math.floor(m / 60);
  if (h < 24) return `${h}h ago`;
  const d = Math.floor(h / 24);
  return `${d}d ago`;
}

function NotifCard({ notif, onRead }) {
  const meta = TYPE_META[notif.type] || TYPE_META.INCOME_DETECTED;

  return (
    <div
      className={`notif-card${notif.read ? ' notif-read' : ' notif-unread'}`}
      style={{ '--notif-bar': meta.bar }}
    >
      <div className="notif-bar" />
      <div className="notif-body">
        <div className="notif-top">
          <span className="notif-icon" style={{ color: meta.color }}>{meta.icon}</span>
          <span className="notif-badge" style={{ color: meta.color, borderColor: meta.color }}>
            {meta.label}
          </span>
          <span className="notif-time">{timeAgo(notif.createdAt)}</span>
          {!notif.read && (
            <span className="notif-dot" />
          )}
        </div>
        <h4 className="notif-title">{notif.title}</h4>
        <p className="notif-msg">{notif.message}</p>
        {!notif.read && (
          <button className="notif-read-btn" onClick={() => onRead(notif.id)}>
            Mark as read
          </button>
        )}
      </div>
    </div>
  );
}

export default function NotificationsPage() {
  const [notifs,   setNotifs]   = useState([]);
  const [loading,  setLoading]  = useState(true);
  const [filter,   setFilter]   = useState('all'); // 'all' | 'unread'
  const [error,    setError]    = useState('');

  useEffect(() => { load(); }, []);

  async function load() {
    try {
      const data = await getNotifications();
      setNotifs(data);
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  }

  async function handleRead(id) {
    try {
      await markNotificationRead(id);
      const fresh = await getNotifications();
      setNotifs(fresh);
    } catch (e) {
      setError(e.message);
    }
  }

  async function handleReadAll() {
    try {
      await markAllNotificationsRead();
      const fresh = await getNotifications();
      setNotifs(fresh);
    } catch (e) {
      setError(e.message);
    }
  }

  const unreadCount  = notifs.filter(n => !n.read).length;
  const displayed    = filter === 'unread' ? notifs.filter(n => !n.read) : notifs;

  if (loading) return (
    <div className="page active">
      <div className="empty-state" style={{ paddingTop: '80px' }}>Loading notifications…</div>
    </div>
  );

  return (
    <div className="page active">
      <header className="page-header">
        <div>
          <h2 className="page-title">Notifications</h2>
          <p className="page-date">
            {unreadCount > 0
              ? `${unreadCount} unread notification${unreadCount > 1 ? 's' : ''}`
              : 'All caught up'}
          </p>
        </div>
        <div style={{ display: 'flex', gap: 10 }}>
          {unreadCount > 0 && (
            <button className="btn-read-all" onClick={handleReadAll}>
              ✓ Mark all read
            </button>
          )}
        </div>
      </header>

      {error && <p className="form-error" style={{ marginBottom: 20 }}>{error}</p>}

      {/* Filter tabs */}
      <div className="notif-tabs">
        <button
          className={`notif-tab${filter === 'all' ? ' active' : ''}`}
          onClick={() => setFilter('all')}
        >
          All <span className="notif-tab-count">{notifs.length}</span>
        </button>
        <button
          className={`notif-tab${filter === 'unread' ? ' active' : ''}`}
          onClick={() => setFilter('unread')}
        >
          Unread
          {unreadCount > 0 && (
            <span className="notif-tab-count unread">{unreadCount}</span>
          )}
        </button>
      </div>

      {displayed.length === 0 ? (
        <div className="empty-state" style={{ paddingTop: '50px' }}>
          {filter === 'unread' ? 'No unread notifications.' : 'No notifications yet.'}
        </div>
      ) : (
        <div className="notif-list">
          {displayed.map(n => (
            <NotifCard key={n.id} notif={n} onRead={handleRead} />
          ))}
        </div>
      )}
    </div>
  );
}
