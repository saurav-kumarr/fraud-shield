import api from './api';

export const reportService = {

  getMyReports: async () => {
    const response = await api.get('/api/reports/my-reports');
    return response.data;
  },

  getTotalFraudCount: async () => {
    const response = await api.get(
        '/api/reports/stats/total-fraud');
    return response.data;
  },

  getTodayFraudCount: async () => {
    const response = await api.get(
        '/api/reports/stats/today-fraud');
    return response.data;
  },
};