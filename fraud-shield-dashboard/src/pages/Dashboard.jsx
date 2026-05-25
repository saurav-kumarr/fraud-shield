import { useEffect, useState } from 'react';
import { reportService } from '../services/reportService';
import { transactionService } from '../services/transactionService';
import { alertService } from '../services/alertService';
import websocketService from '../services/websocketService';
import Navbar from '../components/Layout/Navbar';
import StatsCard from '../components/Dashboard/StatsCard';
import { 
  Shield, AlertTriangle, CheckCircle, 
  Activity, Bell, Wifi 
} from 'lucide-react';

const Dashboard = () => {
  const [stats, setStats] = useState({
    totalFraud: 0,
    todayFraud: 0,
    totalTransactions: 0,
    totalAlerts: 0,
  });
  const [recentAlerts, setRecentAlerts] = useState([]);
  const [liveAlerts, setLiveAlerts] = useState([]);
  const [wsConnected, setWsConnected] = useState(false);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchDashboardData();
    
    // Connect WebSocket
    websocketService.connect((newAlert) => {
      setLiveAlerts(prev => [newAlert, ...prev].slice(0, 5));
      
      // Auto refresh stats when new fraud
      fetchDashboardData();
    });

    setWsConnected(true);

    // Cleanup on unmount
    return () => {
      websocketService.disconnect();
    };
  }, []);

  const fetchDashboardData = async () => {
    try {
      const [totalFraud, todayFraud, transactions, alerts] =
              await Promise.all([
                reportService.getTotalFraudCount(),
                reportService.getTodayFraudCount(),
                transactionService.getMyTransactions(),
                alertService.getMyAlerts(),
              ]);

      setStats({
        totalFraud,
        todayFraud,
        totalTransactions: transactions.length,
        totalAlerts: alerts.length,
      });

      setRecentAlerts(alerts.slice(0, 5));
    } catch (error) {
      console.error('Error fetching data:', error);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center 
                      justify-center bg-gray-100">
        <div className="text-2xl text-gray-600">
          Loading dashboard...
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-100">
      <Navbar />

      <div className="max-w-7xl mx-auto px-4 py-8">
        
        <div className="mb-8 flex justify-between items-center">
          <div>
            <h1 className="text-3xl font-bold text-gray-800">
              Dashboard
            </h1>
            <p className="text-gray-600 mt-2">
              Real-time fraud detection overview
            </p>
          </div>
          
          <div className={`flex items-center space-x-2 px-4 
                           py-2 rounded-full ${
            wsConnected 
              ? 'bg-green-100 text-green-700' 
              : 'bg-red-100 text-red-700'
          }`}>
            <Wifi className="w-4 h-4" />
            <span className="text-sm font-semibold">
              {wsConnected ? 'Live' : 'Offline'}
            </span>
          </div>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 
                        lg:grid-cols-4 gap-6 mb-8">
          <StatsCard
            title="Total Fraud Cases"
            value={stats.totalFraud}
            icon={Shield}
            color="bg-red-500"
          />
          <StatsCard
            title="Today's Fraud"
            value={stats.todayFraud}
            icon={AlertTriangle}
            color="bg-orange-500"
          />
          <StatsCard
            title="My Transactions"
            value={stats.totalTransactions}
            icon={Activity}
            color="bg-blue-500"
          />
          <StatsCard
            title="My Alerts"
            value={stats.totalAlerts}
            icon={Bell}
            color="bg-purple-500"
          />
        </div>

        {/* Live Alerts Section */}
        {liveAlerts.length > 0 && (
          <div className="bg-white rounded-xl shadow-md p-6 
                          mb-6 border-l-4 border-red-500">
            <div className="flex items-center mb-4">
              <div className="w-3 h-3 bg-red-500 rounded-full 
                              animate-pulse mr-2"></div>
              <h2 className="text-xl font-bold text-gray-800">
                Live Fraud Alerts
              </h2>
            </div>
            <div className="space-y-3">
              {liveAlerts.map((alert, idx) => (
                <div 
                  key={idx}
                  className="flex items-center justify-between 
                             p-4 bg-red-50 rounded-lg 
                             animate-fadeIn"
                >
                  <div>
                    <p className="font-semibold text-red-800">
                      🚨 New Fraud Detected
                    </p>
                    <p className="text-sm text-red-600">
                      Transaction: {alert.transactionId
                                    ?.substring(0, 8)}... | 
                      Score: {alert.riskScore}
                    </p>
                  </div>
                  <span className="bg-red-600 text-white px-3 
                                   py-1 rounded-full text-xs 
                                   font-bold">
                    {alert.fraudStatus}
                  </span>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* Recent Alerts */}
        <div className="bg-white rounded-xl shadow-md p-6">
          <h2 className="text-xl font-bold text-gray-800 
                         mb-4">
            Recent Alerts
          </h2>
          
          {recentAlerts.length === 0 ? (
            <div className="text-center py-12 text-gray-500">
              <CheckCircle className="w-16 h-16 mx-auto 
                                     mb-4 text-green-500" />
              <p>No alerts. All transactions safe!</p>
            </div>
          ) : (
            <div className="space-y-3">
              {recentAlerts.map((alert, idx) => (
                <div 
                  key={idx} 
                  className="flex items-center justify-between 
                             p-4 bg-gray-50 rounded-lg 
                             hover:bg-gray-100 transition"
                >
                  <div className="flex items-center space-x-3">
                    <div className={`p-2 rounded-full ${
                      alert.fraudStatus === 'BLOCKED' 
                        ? 'bg-red-100' 
                        : 'bg-yellow-100'
                    }`}>
                      <AlertTriangle className={`w-5 h-5 ${
                        alert.fraudStatus === 'BLOCKED' 
                          ? 'text-red-600' 
                          : 'text-yellow-600'
                      }`} />
                    </div>
                    <div>
                      <p className="font-semibold 
                                    text-gray-800">
                        Transaction: {alert.transactionId
                                      ?.substring(0, 8)}...
                      </p>
                      <p className="text-sm text-gray-500">
                        Amount: ₹{alert.amount} | 
                        Score: {alert.riskScore}
                      </p>
                    </div>
                  </div>
                  <span className={`px-3 py-1 rounded-full 
                                    text-xs font-semibold ${
                    alert.fraudStatus === 'BLOCKED' 
                      ? 'bg-red-100 text-red-700' 
                      : 'bg-yellow-100 text-yellow-700'
                  }`}>
                    {alert.fraudStatus}
                  </span>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default Dashboard;