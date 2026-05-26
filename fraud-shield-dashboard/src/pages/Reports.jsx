import { useEffect, useState } from "react";
import { reportService } from "../services/reportService";
import Layout from "../components/Layout/Layout";
import {
  FileText,
  TrendingUp,
  AlertTriangle,
  Shield,
  Activity,
} from "lucide-react";
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
  PieChart,
  Pie,
  Cell,
  LineChart,
  Line,
} from "recharts";
import toast from "react-hot-toast";

const Reports = () => {
  const [reports, setReports] = useState([]);
  const [stats, setStats] = useState({
    totalFraud: 0,
    todayFraud: 0,
  });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchReportsData();
  }, []);

  const fetchReportsData = async () => {
    try {
      const [reportsData, totalFraud, todayFraud] = await Promise.all([
        reportService.getMyReports(),
        reportService.getTotalFraudCount(),
        reportService.getTodayFraudCount(),
      ]);

      setReports(reportsData);
      setStats({ totalFraud, todayFraud });
    } catch (error) {
      toast.error("Failed to fetch reports");
    } finally {
      setLoading(false);
    }
  };

  // Group reports by status for pie chart
  const statusData = reports.reduce((acc, report) => {
    const existing = acc.find((item) => item.name === report.fraudStatus);
    if (existing) {
      existing.value += 1;
    } else {
      acc.push({ name: report.fraudStatus, value: 1 });
    }
    return acc;
  }, []);

  // Group by merchant for bar chart
  const merchantData = reports
    .reduce((acc, report) => {
      const existing = acc.find((item) => item.merchant === report.merchantId);
      if (existing) {
        existing.count += 1;
      } else {
        acc.push({
          merchant: report.merchantId,
          count: 1,
        });
      }
      return acc;
    }, [])
    .slice(0, 5);

  // Risk score distribution
  const riskScoreData = reports
    .map((report, idx) => ({
      name: `T${idx + 1}`,
      score: report.riskScore || 0,
    }))
    .slice(0, 10);

  const COLORS = {
    BLOCKED: "#ef4444",
    FLAGGED: "#f97316",
    APPROVED: "#10b981",
    PENDING: "#eab308",
  };

  if (loading) {
    return (
      <Layout>
        <div className="text-center py-12 text-gray-500">
          Loading reports...
        </div>
      </Layout>
    );
  }

  return (
    <Layout>
      <div className="mb-8">
        <h1
          className="text-3xl font-bold text-gray-800 
                       flex items-center"
        >
          <FileText className="w-8 h-8 mr-3 text-blue-600" />
          Reports & Analytics
        </h1>
        <p className="text-gray-600 mt-2">Visualize fraud detection patterns</p>
      </div>

      <div
        className="grid grid-cols-1 md:grid-cols-3 
                      gap-6 mb-8"
      >
        <div className="bg-white rounded-xl shadow-md p-6">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-gray-500 text-sm">Total Fraud</p>
              <p
                className="text-3xl font-bold text-red-600 
                            mt-2"
              >
                {stats.totalFraud}
              </p>
            </div>
            <div className="bg-red-100 p-3 rounded-full">
              <Shield className="w-8 h-8 text-red-600" />
            </div>
          </div>
        </div>

        <div className="bg-white rounded-xl shadow-md p-6">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-gray-500 text-sm">Today's Fraud</p>
              <p
                className="text-3xl font-bold text-orange-600 
                            mt-2"
              >
                {stats.todayFraud}
              </p>
            </div>
            <div className="bg-orange-100 p-3 rounded-full">
              <AlertTriangle
                className="w-8 h-8 
                                       text-orange-600"
              />
            </div>
          </div>
        </div>

        <div className="bg-white rounded-xl shadow-md p-6">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-gray-500 text-sm">Total Reports</p>
              <p
                className="text-3xl font-bold text-blue-600 
                            mt-2"
              >
                {reports.length}
              </p>
            </div>
            <div className="bg-blue-100 p-3 rounded-full">
              <Activity className="w-8 h-8 text-blue-600" />
            </div>
          </div>
        </div>
      </div>

      <div
        className="grid grid-cols-1 lg:grid-cols-2 
                      gap-6 mb-6"
      >
        <div className="bg-white rounded-xl shadow-md p-6">
          <h2
            className="text-xl font-bold text-gray-800 
                         mb-4"
          >
            Fraud Status Distribution
          </h2>
          {statusData.length === 0 ? (
            <div className="text-center py-12 text-gray-500">
              No data available
            </div>
          ) : (
            <ResponsiveContainer width="100%" height={300}>
              <PieChart>
                <Pie
                  data={statusData}
                  cx="50%"
                  cy="50%"
                  labelLine={false}
                  label={({ name, percent }) =>
                    `${name} ${(percent * 100).toFixed(0)}%`
                  }
                  outerRadius={100}
                  fill="#8884d8"
                  dataKey="value"
                >
                  {statusData.map((entry, index) => (
                    <Cell
                      key={`cell-${index}`}
                      fill={COLORS[entry.name] || "#8884d8"}
                    />
                  ))}
                </Pie>
                <Tooltip />
                <Legend />
              </PieChart>
            </ResponsiveContainer>
          )}
        </div>

        <div className="bg-white rounded-xl shadow-md p-6">
          <h2
            className="text-xl font-bold text-gray-800 
                         mb-4"
          >
            Top 5 Merchants by Fraud Count
          </h2>
          {merchantData.length === 0 ? (
            <div className="text-center py-12 text-gray-500">
              No data available
            </div>
          ) : (
            <ResponsiveContainer width="100%" height={300}>
              <BarChart data={merchantData}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="merchant" />
                <YAxis />
                <Tooltip />
                <Legend />
                <Bar dataKey="count" fill="#3b82f6" />
              </BarChart>
            </ResponsiveContainer>
          )}
        </div>
      </div>

      <div className="bg-white rounded-xl shadow-md p-6 mb-6">
        <h2 className="text-xl font-bold text-gray-800 mb-4">
          Risk Score Trend (Last 10 Transactions)
        </h2>
        {riskScoreData.length === 0 ? (
          <div className="text-center py-12 text-gray-500">
            No data available
          </div>
        ) : (
          <ResponsiveContainer width="100%" height={300}>
            <LineChart data={riskScoreData}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="name" />
              <YAxis />
              <Tooltip />
              <Legend />
              <Line
                type="monotone"
                dataKey="score"
                stroke="#ef4444"
                strokeWidth={2}
                dot={{ r: 4 }}
              />
            </LineChart>
          </ResponsiveContainer>
        )}
      </div>

      <div className="bg-white rounded-xl shadow-md p-6">
        <h2 className="text-xl font-bold text-gray-800 mb-4">
          Recent Fraud Reports
        </h2>
        {reports.length === 0 ? (
          <div className="text-center py-12 text-gray-500">
            No reports available
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead className="bg-gray-50">
                <tr>
                  <th
                    className="px-4 py-3 text-left text-xs 
                                 font-semibold text-gray-600 
                                 uppercase"
                  >
                    Transaction ID
                  </th>
                  <th
                    className="px-4 py-3 text-left text-xs 
                                 font-semibold text-gray-600 
                                 uppercase"
                  >
                    Merchant
                  </th>
                  <th
                    className="px-4 py-3 text-left text-xs 
                                 font-semibold text-gray-600 
                                 uppercase"
                  >
                    Amount
                  </th>
                  <th
                    className="px-4 py-3 text-left text-xs 
                                 font-semibold text-gray-600 
                                 uppercase"
                  >
                    Risk Score
                  </th>
                  <th
                    className="px-4 py-3 text-left text-xs 
                                 font-semibold text-gray-600 
                                 uppercase"
                  >
                    Status
                  </th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-200">
                {reports.slice(0, 10).map((report, idx) => (
                  <tr key={idx} className="hover:bg-gray-50">
                    <td
                      className="px-4 py-3 text-xs font-mono 
               max-w-xs truncate"
                      title={report.transactionId}
                    >
                      {report.transactionId}
                    </td>
                    <td className="px-4 py-3 text-sm">{report.merchantId}</td>
                    <td
                      className="px-4 py-3 text-sm 
                                   font-semibold"
                    >
                      ₹{report.amount}
                    </td>
                    <td className="px-4 py-3">
                      <span
                        className={`text-sm font-bold ${
                          report.riskScore >= 70
                            ? "text-red-600"
                            : report.riskScore >= 40
                              ? "text-orange-600"
                              : "text-green-600"
                        }`}
                      >
                        {report.riskScore}
                      </span>
                    </td>
                    <td className="px-4 py-3">
                      <span
                        className={`inline-block px-3 
                                        py-1 rounded-full 
                                        text-xs 
                                        font-semibold ${
                                          report.fraudStatus === "BLOCKED"
                                            ? "bg-red-100 text-red-700"
                                            : report.fraudStatus === "FLAGGED"
                                              ? "bg-orange-100 text-orange-700"
                                              : "bg-green-100 text-green-700"
                                        }`}
                      >
                        {report.fraudStatus}
                      </span>
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

export default Reports;
