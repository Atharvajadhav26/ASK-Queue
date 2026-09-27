import { Routes, Route, Navigate } from 'react-router-dom';
import { useAuth } from './context/AuthContext';

// Auth Pages
import Login from './pages/auth/Login';
import Register from './pages/auth/Register';

// Customer Pages
import CustomerDashboard from './pages/customer/Dashboard';
import JoinQueue from './pages/customer/JoinQueue';
import QueueDetails from './pages/public/QueueDetails';
import ActiveQueue from './pages/customer/ActiveQueue';
import History from './pages/customer/History';
import Notifications from './pages/customer/Notifications';
import Profile from './pages/customer/Profile';

// Admin Pages
import AdminDashboard from './pages/admin/Dashboard';
import CreateQueue from './pages/admin/CreateQueue';
import EditQueue from './pages/admin/EditQueue';
import QueueManagement from './pages/admin/QueueManagement';
import Analytics from './pages/admin/Analytics';
import Reports from './pages/admin/Reports';

// Layouts
import CustomerLayout from './components/layout/CustomerLayout';
import AdminLayout from './components/layout/AdminLayout';

function ProtectedRoute({ children, adminOnly = false }) {
  const { isAuthenticated, isAdmin, loading } = useAuth();

  if (loading) {
    return (
      <div className="d-flex justify-content-center align-items-center" style={{ minHeight: '100vh' }}>
        <div className="spinner-border text-primary" role="status">
          <span className="visually-hidden">Loading...</span>
        </div>
      </div>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (adminOnly && !isAdmin) {
    return <Navigate to="/dashboard" replace />;
  }

  return children;
}

export default function App() {
  return (
    <Routes>
      {/* Public Routes */}
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />
      <Route path="/queue/:queueId" element={<QueueDetails />} />

      {/* Customer Routes */}
      <Route path="/" element={
        <ProtectedRoute>
          <CustomerLayout />
        </ProtectedRoute>
      }>
        <Route index element={<Navigate to="/dashboard" replace />} />
        <Route path="dashboard" element={<CustomerDashboard />} />
        <Route path="join-queue" element={<JoinQueue />} />
        <Route path="active-queue" element={<ActiveQueue />} />
        <Route path="history" element={<History />} />
        <Route path="notifications" element={<Notifications />} />
        <Route path="profile" element={<Profile />} />
      </Route>

      {/* Admin Routes */}
      <Route path="/admin" element={
        <ProtectedRoute adminOnly>
          <AdminLayout />
        </ProtectedRoute>
      }>
        <Route index element={<Navigate to="/admin/dashboard" replace />} />
        <Route path="dashboard" element={<AdminDashboard />} />
        <Route path="create-queue" element={<CreateQueue />} />
        <Route path="queues/:id/edit" element={<EditQueue />} />
        <Route path="queues/:id/manage" element={<QueueManagement />} />
        <Route path="analytics" element={<Analytics />} />
        <Route path="reports" element={<Reports />} />
      </Route>

      {/* Catch all */}
      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  );
}
