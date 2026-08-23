import api from './api';

export const transactionService = {
  // Add idempotencyKey as a second parameter
  createTransaction: async (data, idempotencyKey) => {
    const response = await api.post(
        '/api/transactions', data, {
          // Inject the header explicitly for this POST request
          headers: {
            'Idempotency-Key': idempotencyKey
          }

        });
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

  getTransactionsByUserId: async (userId) => {
    const response = await api.get(
        `/api/transactions/user/${userId}`);
    return response.data;
  },
};