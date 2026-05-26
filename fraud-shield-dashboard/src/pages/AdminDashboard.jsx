import { useEffect, useState } from 'react';
import { alertService } from '../services/alertService';
import { reportService } from '../services/reportService';
import { transactionService } from '../services/transactionService';
import Layout from '../components/Layout/Layout';
import { 
  Shield, Search, Filter, Users, 
  AlertTriangle, FileText, Activity 
} from 'lucide-react';
import toast from 'react-hot-toast';

const AdminDashboard = () => {
  const [activeTab, setActiveTab] = useState('alerts');
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(false);
  const [searchUserId, setSearchUserId] = useState('');
  const [searchMerchantId, setSearchMerchantId] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');

  useEffect(() => {
    fetchData();
  }, [activeTab]);

  const fetchData = async () => {
    setLoading(true);
    try {
      let result = [];
      if (activeTab === 'alerts') {
        result = await alertService.getMyAlerts();
      } else if (activeTab === 'reports') {
        result = await reportService.getMyReports();
      }
      setData(result);
    } catch (error) {
      toast.error('Failed to fetch data');
    } finally {
      setLoading(false);
    }
  };

  const handleUserSearch = async () => {
    if (!searchUserId.trim()) {
      toast.error('Enter a user ID');
      return;
    }
    setLoading(true);
    try {
      let result = [];
      if (activeTab === 'alerts') {
        result = await alertService
            .getAlertsByUserId(searchUserId);
      } else if (activeTab === 'reports') {
        result = await reportService
            .getReportsByUserId(searchUserId);
      } else if (activeTab === 'transactions') {
        result = await transactionService
            .getTransactionsByUserId(searchUserId);
      }
      setData(result);
      toast.success(`Found ${result.length} records`);
    } catch (error) {
      toast.error('Search failed');
    } finally {
      setLoading(false);
    }
  };

  const handleMerchantSearch = async () => {
    if (!searchMerchantId.trim()) {
      toast.error('Enter a merchant ID');
      return;
    }
    setLoading(true);
    try {
      const result = await reportService
          .getReportsByMerchant(searchMerchantId);
      setData(result);
      toast.success(`Found ${result.length} records`);
    } catch (error) {
      toast.error('Search failed');
    } finally {
      setLoading(false);
    }
  };

  const handleStatusFilter = async (status) => {
    setStatusFilter(status);
    if (status === 'ALL') {
      fetchData();
      return;
    }
    setLoading(true);
    try {
      let result = [];
      if (activeTab === 'alerts') {
        result = await alertService
            .getAlertsByStatus(status);
      } else if (activeTab === 'reports') {
        result = await reportService
            .getReportsByStatus(status);
      }
      setData(result);
    } catch (error) {
      toast.error('Filter failed');
    } finally {
      setLoading(false);
    }
  };

  const clearFilters = () => {
    setSearchUserId('');
    setSearchMerchantId('');
    setStatusFilter('ALL');
    fetchData();
  };

  return (
    <Layout>
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-800 
                       flex items-center">
          <Shield className="w-8 h-8 mr-3 text-purple-600" />
          Admin Dashboard
        </h1>
        <p className="text-gray-600 mt-2">
          System-wide fraud detection management
        </p>
      </div>

      <div className="bg-white rounded-xl shadow-md mb-6">
        <div className="flex border-b">
          {[
            { id: 'alerts', label: 'All Alerts', 
              icon: AlertTriangle },
            { id: 'reports', label: 'All Reports', 
              icon: FileText },
            { id: 'transactions', label: 'Transactions', 
              icon: Activity },
          ].map(tab => (
            <button
              key={tab.id}
              onClick={() => {
                setActiveTab(tab.id);
                clearFilters();
              }}
              className={`flex items-center space-x-2 px-6 
                          py-4 font-medium transition ${
                activeTab === tab.id
                  ? 'text-purple-600 border-b-2 ' +
                    'border-purple-600'
                  : 'text-gray-600 hover:text-gray-800'
              }`}
            >
              <tab.icon className="w-5 h-5" />
              <span>{tab.label}</span>
            </button>
          ))}
        </div>
      </div>

      <div className="bg-white rounded-xl shadow-md p-6 mb-6">
        <h3 className="text-lg font-bold text-gray-800 mb-4 
                       flex items-center">
          <Filter className="w-5 h-5 mr-2" />
          Search & Filters
        </h3>

        <div className="grid grid-cols-1 md:grid-cols-2 
                        gap-4 mb-4">
          
          <div>
            <label className="block text-sm font-medium 
                              text-gray-700 mb-2">
              Search by User ID (Email)
            </label>
            <div className="flex space-x-2">
              <input
                type="text"
                value={searchUserId}
                onChange={(e) => 
                    setSearchUserId(e.target.value)}
                placeholder="user@example.com"
                className="flex-1 px-4 py-2 border 
                           border-gray-300 rounded-lg 
                           focus:ring-2 focus:ring-purple-500"
              />
              <button
                onClick={handleUserSearch}
                className="bg-purple-600 text-white px-4 
                           py-2 rounded-lg hover:bg-purple-700 
                           flex items-center space-x-1"
              >
                <Search className="w-4 h-4" />
                <span>Search</span>
              </button>
            </div>
          </div>

          {activeTab === 'reports' && (
            <div>
              <label className="block text-sm font-medium 
                                text-gray-700 mb-2">
                Search by Merchant ID
              </label>
              <div className="flex space-x-2">
                <input
                  type="text"
                  value={searchMerchantId}
                  onChange={(e) => 
                      setSearchMerchantId(e.target.value)}
                  placeholder="merchant-001"
                  className="flex-1 px-4 py-2 border 
                             border-gray-300 rounded-lg 
                             focus:ring-2 
                             focus:ring-purple-500"
                />
                <button
                  onClick={handleMerchantSearch}
                  className="bg-purple-600 text-white px-4 
                             py-2 rounded-lg 
                             hover:bg-purple-700 
                             flex items-center space-x-1"
                >
                  <Search className="w-4 h-4" />
                  <span>Search</span>
                </button>
              </div>
            </div>
          )}
        </div>

        {(activeTab === 'alerts' || activeTab === 'reports') 
              && (
          <div>
            <label className="block text-sm font-medium 
                              text-gray-700 mb-2">
              Filter by Status
            </label>
            <div className="flex space-x-2">
              {['ALL', 'BLOCKED', 'FLAGGED'].map(status => (
                <button
                  key={status}
                  onClick={() => handleStatusFilter(status)}
                  className={`px-4 py-2 rounded-lg text-sm 
                              font-medium transition ${
                    statusFilter === status
                      ? 'bg-purple-600 text-white'
                      : 'bg-gray-100 text-gray-700 ' +
                        'hover:bg-gray-200'
                  }`}
                >
                  {status}
                </button>
              ))}
            </div>
          </div>
        )}

        <div className="mt-4 pt-4 border-t">
          <button
            onClick={clearFilters}
            className="text-sm text-gray-600 
                       hover:text-gray-800"
          >
            Clear all filters
          </button>
        </div>
      </div>

      <div className="bg-white rounded-xl shadow-md p-6">
        <div className="flex justify-between items-center 
                        mb-4">
          <h3 className="text-lg font-bold text-gray-800">
            Results ({data.length})
          </h3>
        </div>

        {loading ? (
          <div className="text-center py-12 text-gray-500">
            Loading...
          </div>
        ) : data.length === 0 ? (
          <div className="text-center py-12 text-gray-500">
            No data found
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-4 py-3 text-left 
                                 text-xs font-semibold 
                                 text-gray-600 uppercase">
                    Transaction ID
                  </th>
                  <th className="px-4 py-3 text-left 
                                 text-xs font-semibold 
                                 text-gray-600 uppercase">
                    User
                  </th>
                  <th className="px-4 py-3 text-left 
                                 text-xs font-semibold 
                                 text-gray-600 uppercase">
                    Merchant
                  </th>
                  <th className="px-4 py-3 text-left 
                                 text-xs font-semibold 
                                 text-gray-600 uppercase">
                    Amount
                  </th>
                  {(activeTab === 'alerts' || 
                    activeTab === 'reports') && (
                    <th className="px-4 py-3 text-left 
                                   text-xs font-semibold 
                                   text-gray-600 uppercase">
                      Risk Score
                    </th>
                  )}
                  <th className="px-4 py-3 text-left 
                                 text-xs font-semibold 
                                 text-gray-600 uppercase">
                    Status
                  </th>
                  <th className="px-4 py-3 text-left 
                                 text-xs font-semibold 
                                 text-gray-600 uppercase">
                    Date
                  </th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-200">
                {data.map((item, idx) => (
                  <tr key={idx} className="hover:bg-gray-50">
                    <td className="px-4 py-3 font-mono 
                                   text-xs">
                      {item.transactionId}
                    </td>
                    <td className="px-4 py-3">
                      {item.userId}
                    </td>
                    <td className="px-4 py-3">
                      {item.merchantId}
                    </td>
                    <td className="px-4 py-3 font-semibold">
                      ₹{item.amount}
                    </td>
                    {(activeTab === 'alerts' || 
                      activeTab === 'reports') && (
                      <td className="px-4 py-3">
                        <span className={`font-bold ${
                          item.riskScore >= 70 
                            ? 'text-red-600' 
                            : 'text-orange-600'
                        }`}>
                          {item.riskScore}
                        </span>
                      </td>
                    )}
                    <td className="px-4 py-3">
                      <span className={`inline-block px-2 
                                        py-1 rounded-full 
                                        text-xs 
                                        font-semibold ${
                        (item.fraudStatus || item.status) 
                            === 'BLOCKED' 
                          ? 'bg-red-100 text-red-700'
                          : (item.fraudStatus || item.status) 
                              === 'FLAGGED'
                            ? 'bg-orange-100 text-orange-700'
                            : 'bg-green-100 text-green-700'
                      }`}>
                        {item.fraudStatus || item.status}
                      </span>
                    </td>
                    <td className="px-4 py-3 text-xs 
                                   text-gray-500">
                      {new Date(item.createdAt)
                          .toLocaleString()}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </Layout>
  );
};

export default AdminDashboard;