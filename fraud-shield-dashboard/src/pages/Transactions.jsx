import { useEffect, useState } from 'react';
import { transactionService } from '../services/transactionService';
import Layout from '../components/Layout/Layout';
import { 
  CreditCard, Search, Eye, AlertCircle, 
  CheckCircle, Clock 
} from 'lucide-react';
import toast from 'react-hot-toast';

const Transactions = () => {
  const [transactions, setTransactions] = useState([]);
  const [filteredTransactions, setFilteredTransactions] = 
          useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [selectedTransaction, setSelectedTransaction] = 
          useState(null);

  useEffect(() => {
    fetchTransactions();
  }, []);

  useEffect(() => {
    filterTransactions();
  }, [search, statusFilter, transactions]);

  const fetchTransactions = async () => {
    try {
      const data = await transactionService.getMyTransactions();
      setTransactions(data);
      setFilteredTransactions(data);
    } catch (error) {
      toast.error('Failed to fetch transactions');
    } finally {
      setLoading(false);
    }
  };

  const filterTransactions = () => {
    let filtered = transactions;

    if (statusFilter !== 'ALL') {
      filtered = filtered.filter(t => 
          t.status === statusFilter);
    }

    if (search) {
      filtered = filtered.filter(t =>
          t.transactionId.toLowerCase()
              .includes(search.toLowerCase()) ||
          t.merchantId.toLowerCase()
              .includes(search.toLowerCase())
      );
    }

    setFilteredTransactions(filtered);
  };

  const getStatusBadge = (status) => {
    const styles = {
      APPROVED: 'bg-green-100 text-green-700',
      PENDING: 'bg-yellow-100 text-yellow-700',
      BLOCKED: 'bg-red-100 text-red-700',
      FLAGGED: 'bg-orange-100 text-orange-700',
    };
    return styles[status] || 'bg-gray-100 text-gray-700';
  };

  const getStatusIcon = (status) => {
    if (status === 'APPROVED') 
      return <CheckCircle className="w-4 h-4" />;
    if (status === 'PENDING') 
      return <Clock className="w-4 h-4" />;
    return <AlertCircle className="w-4 h-4" />;
  };

  return (
    <Layout>
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-800 
                       flex items-center">
          <CreditCard className="w-8 h-8 mr-3 
                                 text-blue-600" />
          Transactions
        </h1>
        <p className="text-gray-600 mt-2">
          View and manage all your transactions
        </p>
      </div>

      <div className="bg-white rounded-xl shadow-md p-6 mb-6">
        <div className="grid grid-cols-1 md:grid-cols-2 
                        gap-4">
          <div className="relative">
            <Search className="absolute left-3 top-3.5 
                               w-5 h-5 text-gray-400" />
            <input
              type="text"
              placeholder="Search by transaction ID 
                           or merchant..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              className="w-full pl-10 pr-4 py-3 border 
                         border-gray-300 rounded-lg 
                         focus:ring-2 focus:ring-blue-500"
            />
          </div>

          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            className="px-4 py-3 border border-gray-300 
                       rounded-lg focus:ring-2 
                       focus:ring-blue-500"
          >
            <option value="ALL">All Status</option>
            <option value="APPROVED">Approved</option>
            <option value="PENDING">Pending</option>
            <option value="FLAGGED">Flagged</option>
            <option value="BLOCKED">Blocked</option>
          </select>
        </div>
      </div>

      <div className="bg-white rounded-xl shadow-md 
                      overflow-hidden">
        {loading ? (
          <div className="p-12 text-center text-gray-500">
            Loading transactions...
          </div>
        ) : filteredTransactions.length === 0 ? (
          <div className="p-12 text-center text-gray-500">
            No transactions found
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-6 py-4 text-left 
                                 text-xs font-semibold 
                                 text-gray-600 uppercase">
                    Transaction ID
                  </th>
                  <th className="px-6 py-4 text-left 
                                 text-xs font-semibold 
                                 text-gray-600 uppercase">
                    Merchant
                  </th>
                  <th className="px-6 py-4 text-left 
                                 text-xs font-semibold 
                                 text-gray-600 uppercase">
                    Amount
                  </th>
                  <th className="px-6 py-4 text-left 
                                 text-xs font-semibold 
                                 text-gray-600 uppercase">
                    Status
                  </th>
                  <th className="px-6 py-4 text-left 
                                 text-xs font-semibold 
                                 text-gray-600 uppercase">
                    Date
                  </th>
                  <th className="px-6 py-4 text-right 
                                 text-xs font-semibold 
                                 text-gray-600 uppercase">
                    Action
                  </th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-200">
                {filteredTransactions.map((t) => (
                  <tr key={t.transactionId} 
                      className="hover:bg-gray-50">
                    <td className="px-6 py-4 font-mono 
                                   text-sm">
                      {t.transactionId.substring(0, 12)}...
                    </td>
                    <td className="px-6 py-4 text-sm">
                      {t.merchantId}
                    </td>
                    <td className="px-6 py-4 font-semibold">
                      ₹{t.amount}
                    </td>
                    <td className="px-6 py-4">
                      <span className={`inline-flex 
                                        items-center 
                                        space-x-1 px-3 py-1 
                                        rounded-full text-xs 
                                        font-semibold 
                                        ${getStatusBadge(
                                            t.status)}`}>
                        {getStatusIcon(t.status)}
                        <span>{t.status}</span>
                      </span>
                    </td>
                    <td className="px-6 py-4 text-sm 
                                   text-gray-500">
                      {new Date(t.createdAt)
                          .toLocaleString()}
                    </td>
                    <td className="px-6 py-4 text-right">
                      <button
                        onClick={() => 
                            setSelectedTransaction(t)}
                        className="text-blue-600 
                                   hover:text-blue-800 
                                   flex items-center 
                                   space-x-1 ml-auto"
                      >
                        <Eye className="w-4 h-4" />
                        <span>View</span>
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {selectedTransaction && (
        <TransactionDetailModal
          transaction={selectedTransaction}
          onClose={() => setSelectedTransaction(null)}
        />
      )}
    </Layout>
  );
};

const TransactionDetailModal = ({ transaction, onClose }) => {
  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 
                    flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-xl shadow-2xl 
                      max-w-2xl w-full max-h-[90vh] 
                      overflow-y-auto">
        
        <div className="p-6 border-b">
          <div className="flex justify-between items-center">
            <h2 className="text-2xl font-bold text-gray-800">
              Transaction Details
            </h2>
            <button
              onClick={onClose}
              className="text-gray-400 hover:text-gray-600 
                         text-3xl"
            >
              ×
            </button>
          </div>
        </div>

        <div className="p-6 space-y-4">
          <div className="grid grid-cols-1 md:grid-cols-2 
                          gap-4">
            <DetailItem 
              label="Transaction ID" 
              value={transaction.transactionId} />
            <DetailItem 
              label="Status" 
              value={transaction.status}
              isStatus />
            <DetailItem 
              label="Amount" 
              value={`₹${transaction.amount}`} />
            <DetailItem 
              label="Currency" 
              value={transaction.currency} />
            <DetailItem 
              label="Merchant ID" 
              value={transaction.merchantId} />
            <DetailItem 
              label="Type" 
              value={transaction.type} />
            <DetailItem 
              label="Device ID" 
              value={transaction.deviceId} />
            <DetailItem 
              label="IP Address" 
              value={transaction.ipAddress} />
            <DetailItem 
              label="Location" 
              value={transaction.location} />
            <DetailItem 
              label="Created At" 
              value={new Date(transaction.createdAt)
                  .toLocaleString()} />
          </div>
        </div>

        <div className="p-6 border-t bg-gray-50">
          <button
            onClick={onClose}
            className="w-full bg-blue-600 text-white py-3 
                       rounded-lg font-semibold 
                       hover:bg-blue-700"
          >
            Close
          </button>
        </div>
      </div>
    </div>
  );
};

const DetailItem = ({ label, value, isStatus }) => (
  <div>
    <p className="text-sm text-gray-500 mb-1">{label}</p>
    {isStatus ? (
      <span className={`inline-block px-3 py-1 rounded-full 
                        text-sm font-semibold ${
                          value === 'APPROVED' 
                            ? 'bg-green-100 text-green-700'
                            : value === 'BLOCKED'
                              ? 'bg-red-100 text-red-700'
                              : 'bg-yellow-100 text-yellow-700'
                        }`}>
        {value}
      </span>
    ) : (
      <p className="font-semibold text-gray-800 break-all">
        {value}
      </p>
    )}
  </div>
);

export default Transactions;