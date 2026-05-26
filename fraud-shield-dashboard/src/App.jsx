import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { AuthProvider } from "./context/AuthContext";
import Login from "./pages/Login.jsx";
import Register from "./pages/Register";
import Dashboard from "./pages/Dashboard";
import ProtectedRoute from "./components/Auth/ProtectedRoute";
import Transactions from "./pages/Transactions";
import CreateTransaction from "./pages/CreateTransaction";
import Reports from "./pages/Reports";
import Alerts from "./pages/Alerts";
import AdminDashboard from "./pages/AdminDashboard";
 import MerchantRegister from './pages/MerchantRegister';
import ApiDocs from './pages/ApiDocs';

function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<Navigate to="/login" />} />
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route
            path="/dashboard"
            element={
              <ProtectedRoute>
                <Dashboard />
              </ProtectedRoute>
            }
          />
          <Route
            path="/transactions"
            element={
              <ProtectedRoute>
                <Transactions />
              </ProtectedRoute>
            }
          />
          <Route
            path="/create-transaction"
            element={
              <ProtectedRoute>
                <CreateTransaction />
              </ProtectedRoute>
            }
          />
          <Route
            path="/reports"
            element={
              <ProtectedRoute>
                <Reports />
              </ProtectedRoute>
            }
          />
          <Route
            path="/alerts"
            element={
              <ProtectedRoute>
                <Alerts />
              </ProtectedRoute>
            }
          />
          <Route
            path="/admin"
            element={
              <ProtectedRoute>
                <AdminDashboard />
              </ProtectedRoute>
            }
          />

          <Route path="/merchant-register" element={<MerchantRegister />} />
        <Route path="/api-docs" element={<ApiDocs />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}

export default App;
