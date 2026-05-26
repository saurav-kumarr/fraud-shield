import Sidebar from './Sidebar';
import { Toaster } from 'react-hot-toast';

const Layout = ({ children }) => {
  return (
    <div className="min-h-screen bg-gray-100">
      <Toaster position="top-right" />
      <Sidebar />
      <main className="ml-64 p-8">
        {children}
      </main>
    </div>
  );
};

export default Layout;