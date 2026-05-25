import api from './api';

export const alertService = {

  getMyAlerts: async () => {
    const response = await api.get('/api/alerts/my-alerts');
    return response.data;
  },

  getAlertsByStatus: async (status) => {
    const response = await api.get(
        `/api/alerts/status/${status}`);
    return response.data;
  },
};