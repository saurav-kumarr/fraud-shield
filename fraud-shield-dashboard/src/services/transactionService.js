import api from './api';

export const transactionService = {

  createTransaction: async (data) => {
    const response = await api.post(
        '/api/transactions', data);
    return response.data;
  },

  getMyTransactions: async () => {
    const response = await api.get(
        '/api/transactions/my-transactions');
    return response.data;
  },

  getTransactionById: async (id) => {
    const response = await api.get(`/api/transactions/${id}`);
    return response.data;
  },
};