import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import AuthPage from './pages/AuthPage';
import DashboardPage from './pages/DashboardPage';
import TransactionsPage from './pages/TransactionsPage';
import ForecastPage from './pages/ForecastPage';
import InsightsPage from './pages/InsightsPage';
import GoalsPage from './pages/GoalsPage';
import NotificationsPage from './pages/NotificationsPage';
import ReportsPage from './pages/ReportsPage';
import Sidebar from './components/Sidebar';

function PrivateLayout({ children }) {
  const { isAuthenticated } = useAuth();
  if (!isAuthenticated) return <Navigate to="/auth" replace />;
  return (
    <div className="app">
      <Sidebar />
      <main className="main">{children}</main>
    </div>
  );
}

function AppRoutes() {
  const { isAuthenticated } = useAuth();
  return (
    <BrowserRouter>
      <Routes>
        <Route
          path="/auth"
          element={isAuthenticated ? <Navigate to="/" replace /> : <AuthPage />}
        />
        <Route
          path="/"
          element={<PrivateLayout><DashboardPage /></PrivateLayout>}
        />
        <Route
          path="/transactions"
          element={<PrivateLayout><TransactionsPage /></PrivateLayout>}
        />
        <Route
          path="/forecast"
          element={<PrivateLayout><ForecastPage /></PrivateLayout>}
        />
        <Route
          path="/insights"
          element={<PrivateLayout><InsightsPage /></PrivateLayout>}
        />
        <Route
          path="/goals"
          element={<PrivateLayout><GoalsPage /></PrivateLayout>}
        />
        <Route
          path="/notifications"
          element={<PrivateLayout><NotificationsPage /></PrivateLayout>}
        />
        <Route
          path="/reports"
          element={<PrivateLayout><ReportsPage /></PrivateLayout>}
        />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  );
}

export default function App() {
  return (
    <AuthProvider>
      <AppRoutes />
    </AuthProvider>
  );
}
