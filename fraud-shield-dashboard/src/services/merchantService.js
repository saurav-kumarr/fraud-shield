import api from './api';

export const merchantService = {

  registerMerchant: async (data) => {
    const response = await api.post(
        '/api/merchant/register', data);
    return response.data;
  },
};