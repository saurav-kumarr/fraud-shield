import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { transactionService } from '../services/transactionService';
import Layout from '../components/Layout/Layout';
import { Plus, Send } from 'lucide-react';
import toast from 'react-hot-toast';

const CreateTransaction = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  
  const [formData, setFormData] = useState({
    merchantId: '',
    amount: '',
    currency: 'INR',
    deviceId: '',
    ipAddress: '',
    location: '',
    type: 'PAYMENT',
  });

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);

    try {
      const response = await transactionService
          .createTransaction({
            ...formData,
            amount: parseFloat(formData.amount),
          });
      
      toast.success('Transaction created successfully!');
      navigate('/transactions');
    } catch (error) {
      toast.error(
          error.response?.data?.message || 
          'Failed to create transaction');
    } finally {
      setLoading(false);
    }
  };

  const fillTestData = (scenario) => {
    const scenarios = {
      normal: {
        merchantId: 'merchant-001',
        amount: '5000',
        currency: 'INR',
        deviceId: 'device-001',
        ipAddress: '192.168.1.1',
        location: 'Delhi, India',
        type: 'PAYMENT',
      },
      highAmount: {
        merchantId: 'merchant-002',
        amount: '500000',
        currency: 'INR',
        deviceId: 'device-001',
        ipAddress: '192.168.1.1',
        location: 'Delhi, India',
        type: 'PAYMENT',
      },
      locationChange: {
        merchantId: 'merchant-001',
        amount: '5000',
        currency: 'INR',
        deviceId: 'device-001',
        ipAddress: '192.168.1.1',
        location: 'Mumbai, India',
        type: 'PAYMENT',
      },
    };
    setFormData(scenarios[scenario]);
  };

  return (
    <Layout>
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-800 
                       flex items-center">
          <Plus className="w-8 h-8 mr-3 text-blue-600" />
          Create Transaction
        </h1>
        <p className="text-gray-600 mt-2">
          Submit a new transaction for fraud analysis
        </p>
      </div>

      <div className="bg-blue-50 border-l-4 border-blue-500 
                      p-4 mb-6 rounded">
        <p className="text-sm font-semibold text-blue-800 
                       mb-2">
          Quick Test Scenarios:
        </p>
        <div className="flex flex-wrap gap-2">
          <button
            onClick={() => fillTestData('normal')}
            className="px-3 py-1 bg-green-100 text-green-700 
                       rounded-full text-sm hover:bg-green-200"
          >
            ✓ Normal Transaction
          </button>
          <button
            onClick={() => fillTestData('highAmount')}
            className="px-3 py-1 bg-red-100 text-red-700 
                       rounded-full text-sm hover:bg-red-200"
          >
            ⚠ High Amount (BLOCKED)
          </button>
          <button
            onClick={() => fillTestData('locationChange')}
            className="px-3 py-1 bg-orange-100 text-orange-700 
                       rounded-full text-sm 
                       hover:bg-orange-200"
          >
            📍 Location Change (BLOCKED)
          </button>
        </div>
      </div>

      <div className="bg-white rounded-xl shadow-md p-6">
        <form onSubmit={handleSubmit} className="space-y-4">
          
          <div className="grid grid-cols-1 md:grid-cols-2 
                          gap-4">
            
            <div>
              <label className="block text-sm font-medium 
                                text-gray-700 mb-2">
                Merchant ID *
              </label>
              <input
                type="text"
                name="merchantId"
                value={formData.merchantId}
                onChange={handleChange}
                required
                placeholder="merchant-001"
                className="w-full px-4 py-3 border 
                           border-gray-300 rounded-lg 
                           focus:ring-2 focus:ring-blue-500"
              />
            </div>

            <div>
              <label className="block text-sm font-medium 
                                text-gray-700 mb-2">
                Amount *
              </label>
              <input
                type="number"
                name="amount"
                value={formData.amount}
                onChange={handleChange}
                required
                step="0.01"
                placeholder="5000.00"
                className="w-full px-4 py-3 border 
                           border-gray-300 rounded-lg 
                           focus:ring-2 focus:ring-blue-500"
              />
            </div>

            <div>
              <label className="block text-sm font-medium 
                                text-gray-700 mb-2">
                Currency *
              </label>
              <select
                name="currency"
                value={formData.currency}
                onChange={handleChange}
                className="w-full px-4 py-3 border 
                           border-gray-300 rounded-lg 
                           focus:ring-2 focus:ring-blue-500"
              >
                <option value="INR">INR (₹)</option>
                <option value="USD">USD ($)</option>
                <option value="EUR">EUR (€)</option>
              </select>
            </div>

            <div>
              <label className="block text-sm font-medium 
                                text-gray-700 mb-2">
                Type *
              </label>
              <select
                name="type"
                value={formData.type}
                onChange={handleChange}
                className="w-full px-4 py-3 border 
                           border-gray-300 rounded-lg 
                           focus:ring-2 focus:ring-blue-500"
              >
                <option value="PAYMENT">Payment</option>
                <option value="WITHDRAWAL">Withdrawal</option>
                <option value="TRANSFER">Transfer</option>
                <option value="REFUND">Refund</option>
              </select>
            </div>

            <div>
              <label className="block text-sm font-medium 
                                text-gray-700 mb-2">
                Device ID *
              </label>
              <input
                type="text"
                name="deviceId"
                value={formData.deviceId}
                onChange={handleChange}
                required
                placeholder="device-001"
                className="w-full px-4 py-3 border 
                           border-gray-300 rounded-lg 
                           focus:ring-2 focus:ring-blue-500"
              />
            </div>

            <div>
              <label className="block text-sm font-medium 
                                text-gray-700 mb-2">
                IP Address *
              </label>
              <input
                type="text"
                name="ipAddress"
                value={formData.ipAddress}
                onChange={handleChange}
                required
                placeholder="192.168.1.1"
                className="w-full px-4 py-3 border 
                           border-gray-300 rounded-lg 
                           focus:ring-2 focus:ring-blue-500"
              />
            </div>

            <div className="md:col-span-2">
              <label className="block text-sm font-medium 
                                text-gray-700 mb-2">
                Location *
              </label>
              <input
                type="text"
                name="location"
                value={formData.location}
                onChange={handleChange}
                required
                placeholder="Delhi, India"
                className="w-full px-4 py-3 border 
                           border-gray-300 rounded-lg 
                           focus:ring-2 focus:ring-blue-500"
              />
            </div>
          </div>

          <div className="flex space-x-3 pt-4">
            <button
              type="button"
              onClick={() => navigate('/transactions')}
              className="px-6 py-3 border border-gray-300 
                         rounded-lg hover:bg-gray-50"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={loading}
              className="flex-1 bg-blue-600 text-white py-3 
                         rounded-lg font-semibold 
                         hover:bg-blue-700 transition 
                         disabled:bg-gray-400 flex items-center 
                         justify-center space-x-2"
            >
              <Send className="w-5 h-5" />
              <span>
                {loading ? 'Submitting...' : 'Submit Transaction'}
              </span>
            </button>
          </div>
        </form>
      </div>
    </Layout>
  );
};

export default CreateTransaction;