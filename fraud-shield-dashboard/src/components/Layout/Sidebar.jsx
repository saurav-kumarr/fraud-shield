import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { 
  Shield, LayoutDashboard, CreditCard, 
  Bell, FileText, LogOut, User, Plus 
} from 'lucide-react';

const Sidebar = () => {
  const navigate = useNavigate();
  const { user, logout } = useAuth();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const navItems = [
    { path: '/dashboard', icon: LayoutDashboard, 
      label: 'Dashboard' },
    { path: '/transactions', icon: CreditCard, 
      label: 'Transactions' },
    { path: '/create-transaction', icon: Plus, 
      label: 'New Transaction' },
    { path: '/alerts', icon: Bell, label: 'Alerts' },
    { path: '/reports', icon: FileText, label: 'Reports' },
  ];

  if (user?.role === 'ADMIN' || user?.role === 'ANALYST') {
  navItems.push({
    path: '/admin',
    icon: Shield,
    label: 'Admin Panel'
  });
}

  return (
    <div className="bg-gray-900 text-white w-64 min-h-screen 
                    fixed left-0 top-0 flex flex-col">
      
      <div className="p-6 border-b border-gray-700">
        <div className="flex items-center space-x-2">
          <Shield className="w-8 h-8 text-blue-400" />
          <div>
            <h1 className="text-xl font-bold">Fraud Shield</h1>
            <p className="text-xs text-gray-400">
              Detection System
            </p>
          </div>
        </div>
      </div>

      <div className="p-4 border-b border-gray-700">
        <div className="flex items-center space-x-3">
          <div className="bg-blue-500 p-2 rounded-full">
            <User className="w-5 h-5" />
          </div>
          <div className="flex-1 min-w-0">
            <p className="font-medium truncate">
              {user?.firstName} {user?.lastName}
            </p>
            <p className="text-xs text-gray-400 truncate">
              {user?.email}
            </p>
            <span className="inline-block mt-1 text-xs 
                             bg-blue-500/20 text-blue-400 
                             px-2 py-0.5 rounded">
              {user?.role}
            </span>
          </div>
        </div>
      </div>

      <nav className="flex-1 p-4 space-y-1">
        {navItems.map((item) => (
          <NavLink
            key={item.path}
            to={item.path}
            className={({ isActive }) => 
              `flex items-center space-x-3 px-4 py-3 
               rounded-lg transition ${
                 isActive 
                   ? 'bg-blue-600 text-white' 
                   : 'text-gray-300 hover:bg-gray-800'
               }`
            }
          >
            <item.icon className="w-5 h-5" />
            <span>{item.label}</span>
          </NavLink>
        ))}
      </nav>

      <div className="p-4 border-t border-gray-700">
        <button
          onClick={handleLogout}
          className="flex items-center space-x-3 px-4 py-3 
                     w-full text-red-400 hover:bg-red-500/10 
                     rounded-lg transition"
        >
          <LogOut className="w-5 h-5" />
          <span>Logout</span>
        </button>
      </div>
    </div>
  );
};

export default Sidebar;