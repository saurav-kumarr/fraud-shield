import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { Shield, LogOut, User } from 'lucide-react';

const Navbar = () => {
  const navigate = useNavigate();
  const { user, logout } = useAuth();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <nav className="bg-white shadow-md">
      <div className="max-w-7xl mx-auto px-4">
        <div className="flex justify-between items-center 
                        h-16">
          
          <div className="flex items-center space-x-2">
            <Shield className="w-8 h-8 text-blue-600" />
            <span className="text-xl font-bold 
                             text-gray-800">
              Fraud Shield
            </span>
          </div>

          <div className="flex items-center space-x-4">
            <div className="flex items-center space-x-2 
                            text-gray-700">
              <User className="w-5 h-5" />
              <span className="font-medium">
                {user?.firstName} {user?.lastName}
              </span>
              <span className="text-xs bg-blue-100 
                               text-blue-700 px-2 py-1 
                               rounded-full">
                {user?.role}
              </span>
            </div>

            <button
              onClick={handleLogout}
              className="flex items-center space-x-2 
                         text-red-600 hover:bg-red-50 
                         px-4 py-2 rounded-lg transition"
            >
              <LogOut className="w-5 h-5" />
              <span>Logout</span>
            </button>
          </div>
        </div>
      </div>
    </nav>
  );
};

export default Navbar;