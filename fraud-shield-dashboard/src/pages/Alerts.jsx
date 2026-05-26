import { useEffect, useState } from "react";
import { alertService } from "../services/alertService";
import websocketService from "../services/websocketService";
import Layout from "../components/Layout/Layout";
import {
  Bell,
  AlertTriangle,
  CheckCircle,
  XCircle,
  Filter,
} from "lucide-react";
import toast from "react-hot-toast";

const Alerts = () => {
  const [alerts, setAlerts] = useState([]);
  const [filteredAlerts, setFilteredAlerts] = useState([]);
  const [statusFilter, setStatusFilter] = useState("ALL");
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchAlerts();

    websocketService.connect((newAlert) => {
      toast.success("New fraud alert received!", {
        icon: "🚨",
      });
      setAlerts((prev) => [newAlert, ...prev]);
    });

    return () => {
      websocketService.disconnect();
    };
  }, []);

  useEffect(() => {
    if (statusFilter === "ALL") {
      setFilteredAlerts(alerts);
    } else {
      setFilteredAlerts(alerts.filter((a) => a.fraudStatus === statusFilter));
    }
  }, [statusFilter, alerts]);

  const fetchAlerts = async () => {
    try {
      const data = await alertService.getMyAlerts();
      setAlerts(data);
      setFilteredAlerts(data);
    } catch (error) {
      toast.error("Failed to fetch alerts");
    } finally {
      setLoading(false);
    }
  };

  const getStatusColor = (status) => {
    if (status === "BLOCKED") return "bg-red-100 text-red-700 border-red-300";
    if (status === "FLAGGED")
      return "bg-orange-100 text-orange-700 border-orange-300";
    return "bg-yellow-100 text-yellow-700 border-yellow-300";
  };

  const getStatusIcon = (status) => {
    if (status === "BLOCKED")
      return <XCircle className="w-6 h-6 text-red-600" />;
    if (status === "FLAGGED")
      return (
        <AlertTriangle
          className="w-6 h-6 
                                       text-orange-600"
        />
      );
    return <CheckCircle className="w-6 h-6 text-yellow-600" />;
  };

  const blockedCount = alerts.filter((a) => a.fraudStatus === "BLOCKED").length;
  const flaggedCount = alerts.filter((a) => a.fraudStatus === "FLAGGED").length;

  return (
    <Layout>
      <div className="mb-8">
        <h1
          className="text-3xl font-bold text-gray-800 
                       flex items-center"
        >
          <Bell className="w-8 h-8 mr-3 text-blue-600" />
          Fraud Alerts
        </h1>
        <p className="text-gray-600 mt-2">
          Real-time fraud detection notifications
        </p>
      </div>

      <div
        className="grid grid-cols-1 md:grid-cols-3 
                      gap-6 mb-6"
      >
        <div
          className="bg-white rounded-xl shadow-md p-6 
                        border-l-4 border-red-500"
        >
          <div className="flex items-center justify-between">
            <div>
              <p className="text-gray-500 text-sm">Blocked</p>
              <p
                className="text-3xl font-bold text-red-600 
                            mt-2"
              >
                {blockedCount}
              </p>
            </div>
            <XCircle className="w-12 h-12 text-red-200" />
          </div>
        </div>

        <div
          className="bg-white rounded-xl shadow-md p-6 
                        border-l-4 border-orange-500"
        >
          <div className="flex items-center justify-between">
            <div>
              <p className="text-gray-500 text-sm">Flagged</p>
              <p
                className="text-3xl font-bold text-orange-600 
                            mt-2"
              >
                {flaggedCount}
              </p>
            </div>
            <AlertTriangle
              className="w-12 h-12 
                                       text-orange-200"
            />
          </div>
        </div>

        <div
          className="bg-white rounded-xl shadow-md p-6 
                        border-l-4 border-blue-500"
        >
          <div className="flex items-center justify-between">
            <div>
              <p className="text-gray-500 text-sm">Total Alerts</p>
              <p
                className="text-3xl font-bold text-blue-600 
                            mt-2"
              >
                {alerts.length}
              </p>
            </div>
            <Bell className="w-12 h-12 text-blue-200" />
          </div>
        </div>
      </div>

      <div className="bg-white rounded-xl shadow-md p-6 mb-6">
        <div className="flex items-center space-x-3">
          <Filter className="w-5 h-5 text-gray-500" />
          <span className="text-sm font-medium text-gray-700">
            Filter by Status:
          </span>
          <div className="flex space-x-2">
            {["ALL", "BLOCKED", "FLAGGED"].map((status) => (
              <button
                key={status}
                onClick={() => setStatusFilter(status)}
                className={`px-4 py-2 rounded-lg text-sm 
                            font-medium transition ${
                              statusFilter === status
                                ? "bg-blue-600 text-white"
                                : "bg-gray-100 text-gray-700 hover:bg-gray-200"
                            }`}
              >
                {status}
              </button>
            ))}
          </div>
        </div>
      </div>

      <div className="space-y-4">
        {loading ? (
          <div
            className="bg-white rounded-xl shadow-md p-12 
                          text-center text-gray-500"
          >
            Loading alerts...
          </div>
        ) : filteredAlerts.length === 0 ? (
          <div
            className="bg-white rounded-xl shadow-md p-12 
                          text-center"
          >
            <CheckCircle
              className="w-16 h-16 mx-auto mb-4 
                                     text-green-500"
            />
            <p className="text-gray-600 text-lg">No alerts found</p>
            <p className="text-gray-400 text-sm mt-2">
              All transactions are safe!
            </p>
          </div>
        ) : (
          filteredAlerts.map((alert, idx) => (
            <div
              key={idx}
              className={`bg-white rounded-xl shadow-md p-6 
                          border-l-4 ${
                            alert.fraudStatus === "BLOCKED"
                              ? "border-red-500"
                              : "border-orange-500"
                          }`}
            >
              <div className="flex items-start justify-between">
                <div
                  className="flex items-start space-x-4 
                                flex-1"
                >
                  <div
                    className={`p-3 rounded-full ${
                      alert.fraudStatus === "BLOCKED"
                        ? "bg-red-100"
                        : "bg-orange-100"
                    }`}
                  >
                    {getStatusIcon(alert.fraudStatus)}
                  </div>

                  <div className="flex-1">
                    <div className="mb-3">
                      <p className="text-xs text-gray-500 mb-1">
                        Transaction ID
                      </p>
                      <div className="flex items-center space-x-2">
                        <h3
                          className="font-mono text-sm text-gray-800 
                   break-all"
                        >
                          {alert.transactionId}
                        </h3>
                        <button
                          onClick={() => {
                            navigator.clipboard.writeText(alert.transactionId);
                            toast.success("Transaction ID copied!");
                          }}
                          className="text-blue-600 hover:text-blue-800 
                 text-xs flex-shrink-0"
                          title="Copy to clipboard"
                        >
                          📋 Copy
                        </button>
                      </div>
                    </div>
                    <div
                      className="grid grid-cols-2 md:grid-cols-4 
                                    gap-4 mt-3"
                    >
                      <div>
                        <p className="text-xs text-gray-500">Amount</p>
                        <p className="font-semibold">₹{alert.amount}</p>
                      </div>
                      <div>
                        <p className="text-xs text-gray-500">Risk Score</p>
                        <p
                          className={`font-bold ${
                            alert.riskScore >= 70
                              ? "text-red-600"
                              : "text-orange-600"
                          }`}
                        >
                          {alert.riskScore}
                        </p>
                      </div>
                      <div>
                        <p className="text-xs text-gray-500">Merchant</p>
                        <p className="font-semibold">{alert.merchantId}</p>
                      </div>
                      <div>
                        <p className="text-xs text-gray-500">Date</p>
                        <p className="font-semibold text-sm">
                          {new Date(alert.createdAt).toLocaleString()}
                        </p>
                      </div>
                    </div>
                  </div>
                </div>

                <span
                  className={`px-3 py-1 rounded-full 
                                  text-xs font-bold border 
                                  ${getStatusColor(alert.fraudStatus)}`}
                >
                  {alert.fraudStatus}
                </span>
              </div>
            </div>
          ))
        )}
      </div>
    </Layout>
  );
};

export default Alerts;
