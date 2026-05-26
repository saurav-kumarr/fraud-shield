import api from './api';

export const reportService = {

  getMyReports: async () => {
    const response = await api.get('/api/reports/my-reports');
    return response.data;
  },

  getReportsByUserId: async (userId) => {
    const response = await api.get(
        `/api/reports/user/${userId}`);
    return response.data;
  },

  getReportsByStatus: async (status) => {
    const response = await api.get(
        `/api/reports/status/${status}`);
    return response.data;
  },

  getReportsByMerchant: async (merchantId) => {
    const response = await api.get(
        `/api/reports/merchant/${merchantId}`);
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